/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { CancellationToken } from '../../../base/common/cancellation.js';
import { Emitter } from '../../../base/common/event.js';
import { Disposable, IDisposable, toDisposable } from '../../../base/common/lifecycle.js';
import * as nls from '../../../nls.js';
import { IExtensionDescription } from '../../../platform/extensions/common/extensions.js';
import { createDecorator } from '../../../platform/instantiation/common/instantiation.js';
import { ILogService } from '../../../platform/log/common/log.js';
import { DisposableTunnel, ProvidedOnAutoForward, ProvidedPortAttributes, RemoteTunnel, TunnelCreationOptions, TunnelOptions, TunnelPrivacyId } from '../../../platform/tunnel/common/tunnel.js';
import { ExtHostTunnelServiceShape, MainContext, MainThreadTunnelServiceShape, PortAttributesSelector, TunnelDto } from './extHost.protocol.js';
import { IExtHostInitDataService } from './extHostInitDataService.js';
import { IExtHostRpcService } from './extHostRpcService.js';
import * as types from './extHostTypes.js';
import { CandidatePort } from '../../services/remote/common/tunnelModel.js';
import * as vibecoda from 'vibecoda';

class ExtensionTunnel extends DisposableTunnel implements vibecoda.Tunnel { }

export namespace TunnelDtoConverter {
	export function fromApiTunnel(tunnel: vibecoda.Tunnel): TunnelDto {
		return {
			remoteAddress: tunnel.remoteAddress,
			localAddress: tunnel.localAddress,
			public: !!tunnel.public,
			privacy: tunnel.privacy ?? (tunnel.public ? TunnelPrivacyId.Public : TunnelPrivacyId.Private),
			protocol: tunnel.protocol
		};
	}
	export function fromServiceTunnel(tunnel: RemoteTunnel): TunnelDto {
		return {
			remoteAddress: {
				host: tunnel.tunnelRemoteHost,
				port: tunnel.tunnelRemotePort
			},
			localAddress: tunnel.localAddress,
			public: tunnel.privacy !== TunnelPrivacyId.ConstantPrivate && tunnel.privacy !== TunnelPrivacyId.ConstantPrivate,
			privacy: tunnel.privacy,
			protocol: tunnel.protocol
		};
	}
}

export interface Tunnel extends vibecoda.Disposable {
	remote: { port: number; host: string };
	localAddress: string;
}

export interface IExtHostTunnelService extends ExtHostTunnelServiceShape {
	readonly _serviceBrand: undefined;
	openTunnel(extension: IExtensionDescription, forward: TunnelOptions): Promise<vibecoda.Tunnel | undefined>;
	getTunnels(): Promise<vibecoda.TunnelDescription[]>;
	onDidChangeTunnels: vibecoda.Event<void>;
	setTunnelFactory(provider: vibecoda.RemoteAuthorityResolver | undefined, managedRemoteAuthority: vibecoda.ManagedResolvedAuthority | undefined): Promise<IDisposable>;
	registerPortsAttributesProvider(portSelector: PortAttributesSelector, provider: vibecoda.PortAttributesProvider): IDisposable;
	registerTunnelProvider(provider: vibecoda.TunnelProvider, information: vibecoda.TunnelInformation): Promise<IDisposable>;
	hasTunnelProvider(): Promise<boolean>;
}

export const IExtHostTunnelService = createDecorator<IExtHostTunnelService>('IExtHostTunnelService');

export class ExtHostTunnelService extends Disposable implements IExtHostTunnelService {
	readonly _serviceBrand: undefined;
	protected readonly _proxy: MainThreadTunnelServiceShape;
	private _forwardPortProvider: ((tunnelOptions: TunnelOptions, tunnelCreationOptions: TunnelCreationOptions, token?: vibecoda.CancellationToken) => Thenable<vibecoda.Tunnel | undefined> | undefined) | undefined;
	private _showCandidatePort: (host: string, port: number, detail: string) => Thenable<boolean> = () => { return Promise.resolve(true); };
	private _extensionTunnels: Map<string, Map<number, { tunnel: vibecoda.Tunnel; disposeListener: IDisposable }>> = new Map();
	private _onDidChangeTunnels: Emitter<void> = new Emitter<void>();
	onDidChangeTunnels: vibecoda.Event<void> = this._onDidChangeTunnels.event;

	private _providerHandleCounter: number = 0;
	private _portAttributesProviders: Map<number, { provider: vibecoda.PortAttributesProvider; selector: PortAttributesSelector }> = new Map();

	constructor(
		@IExtHostRpcService extHostRpc: IExtHostRpcService,
		@IExtHostInitDataService initData: IExtHostInitDataService,
		@ILogService protected readonly logService: ILogService
	) {
		super();
		this._proxy = extHostRpc.getProxy(MainContext.MainThreadTunnelService);
	}

	async openTunnel(extension: IExtensionDescription, forward: TunnelOptions): Promise<vibecoda.Tunnel | undefined> {
		this.logService.trace(`ForwardedPorts: (ExtHostTunnelService) ${extension.identifier.value} called openTunnel API for ${forward.remoteAddress.host}:${forward.remoteAddress.port}.`);
		const tunnel = await this._proxy.$openTunnel(forward, extension.displayName);
		if (tunnel) {
			const disposableTunnel: vibecoda.Tunnel = new ExtensionTunnel(tunnel.remoteAddress, tunnel.localAddress, () => {
				return this._proxy.$closeTunnel(tunnel.remoteAddress);
			});
			this._register(disposableTunnel);
			return disposableTunnel;
		}
		return undefined;
	}

	async getTunnels(): Promise<vibecoda.TunnelDescription[]> {
		return this._proxy.$getTunnels();
	}
	private nextPortAttributesProviderHandle(): number {
		return this._providerHandleCounter++;
	}

	registerPortsAttributesProvider(portSelector: PortAttributesSelector, provider: vibecoda.PortAttributesProvider): vibecoda.Disposable {
		if (portSelector.portRange === undefined && portSelector.commandPattern === undefined) {
			this.logService.error('PortAttributesProvider must specify either a portRange or a commandPattern');
		}
		const providerHandle = this.nextPortAttributesProviderHandle();
		this._portAttributesProviders.set(providerHandle, { selector: portSelector, provider });

		this._proxy.$registerPortsAttributesProvider(portSelector, providerHandle);
		return new types.Disposable(() => {
			this._portAttributesProviders.delete(providerHandle);
			this._proxy.$unregisterPortsAttributesProvider(providerHandle);
		});
	}

	async $providePortAttributes(handles: number[], ports: number[], pid: number | undefined, commandLine: string | undefined, cancellationToken: vibecoda.CancellationToken): Promise<ProvidedPortAttributes[]> {
		const providedAttributes: { providedAttributes: vibecoda.PortAttributes | null | undefined; port: number }[] = [];
		for (const handle of handles) {
			const provider = this._portAttributesProviders.get(handle);
			if (!provider) {
				return [];
			}
			providedAttributes.push(...(await Promise.all(ports.map(async (port) => {
				let providedAttributes: vibecoda.PortAttributes | null | undefined;
				try {
					providedAttributes = await provider.provider.providePortAttributes({ port, pid, commandLine }, cancellationToken);
				} catch (e) {
					// Call with old signature for breaking API change
					providedAttributes = await (provider.provider.providePortAttributes as unknown as (port: number, pid: number | undefined, commandLine: string | undefined, token: vibecoda.CancellationToken) => vibecoda.ProviderResult<vibecoda.PortAttributes>)(port, pid, commandLine, cancellationToken);
				}
				return { providedAttributes, port };
			}))));
		}

		const allAttributes = <{ providedAttributes: vibecoda.PortAttributes; port: number }[]>providedAttributes.filter(attribute => !!attribute.providedAttributes);

		return (allAttributes.length > 0) ? allAttributes.map(attributes => {
			return {
				autoForwardAction: <ProvidedOnAutoForward><unknown>attributes.providedAttributes.autoForwardAction,
				port: attributes.port
			};
		}) : [];
	}

	async $registerCandidateFinder(_enable: boolean): Promise<void> { }

	registerTunnelProvider(provider: vibecoda.TunnelProvider, information: vibecoda.TunnelInformation): Promise<IDisposable> {
		if (this._forwardPortProvider) {
			throw new Error('A tunnel provider has already been registered. Only the first tunnel provider to be registered will be used.');
		}
		this._forwardPortProvider = async (tunnelOptions: TunnelOptions, tunnelCreationOptions: TunnelCreationOptions) => {
			const result = await provider.provideTunnel(tunnelOptions, tunnelCreationOptions, CancellationToken.None);
			return result ?? undefined;
		};

		const tunnelFeatures = information.tunnelFeatures ? {
			elevation: !!information.tunnelFeatures?.elevation,
			privacyOptions: information.tunnelFeatures?.privacyOptions,
			protocol: information.tunnelFeatures.protocol === undefined ? true : information.tunnelFeatures.protocol,
		} : undefined;

		this._proxy.$setTunnelProvider(tunnelFeatures, true);
		return Promise.resolve(toDisposable(() => {
			this._forwardPortProvider = undefined;
			this._proxy.$setTunnelProvider(undefined, false);
		}));
	}

	hasTunnelProvider(): Promise<boolean> {
		return this._proxy.$hasTunnelProvider();
	}

	/**
	 * Applies the tunnel metadata and factory found in the remote authority
	 * resolver to the tunnel system.
	 *
	 * `managedRemoteAuthority` should be be passed if the resolver returned on.
	 * If this is the case, the tunnel cannot be connected to via a websocket from
	 * the share process, so a synethic tunnel factory is used as a default.
	 */
	async setTunnelFactory(provider: vibecoda.RemoteAuthorityResolver | undefined, managedRemoteAuthority: vibecoda.ManagedResolvedAuthority | undefined): Promise<IDisposable> {
		// Do not wait for any of the proxy promises here.
		// It will delay startup and there is nothing that needs to be waited for.
		if (provider) {
			if (provider.candidatePortSource !== undefined) {
				this._proxy.$setCandidatePortSource(provider.candidatePortSource);
			}
			if (provider.showCandidatePort) {
				this._showCandidatePort = provider.showCandidatePort;
				this._proxy.$setCandidateFilter();
			}
			const tunnelFactory = provider.tunnelFactory ?? (managedRemoteAuthority ? this.makeManagedTunnelFactory(managedRemoteAuthority) : undefined);
			if (tunnelFactory) {
				this._forwardPortProvider = tunnelFactory;
				let privacyOptions = provider.tunnelFeatures?.privacyOptions ?? [];
				if (provider.tunnelFeatures?.public && (privacyOptions.length === 0)) {
					privacyOptions = [
						{
							id: 'private',
							label: nls.localize('tunnelPrivacy.private', "Private"),
							themeIcon: 'lock'
						},
						{
							id: 'public',
							label: nls.localize('tunnelPrivacy.public', "Public"),
							themeIcon: 'eye'
						}
					];
				}

				const tunnelFeatures = provider.tunnelFeatures ? {
					elevation: !!provider.tunnelFeatures?.elevation,
					public: !!provider.tunnelFeatures?.public,
					privacyOptions,
					protocol: true
				} : undefined;

				this._proxy.$setTunnelProvider(tunnelFeatures, !!provider.tunnelFactory);
			}
		} else {
			this._forwardPortProvider = undefined;
		}
		return toDisposable(() => {
			this._forwardPortProvider = undefined;
		});
	}

	protected makeManagedTunnelFactory(_authority: vibecoda.ManagedResolvedAuthority): vibecoda.RemoteAuthorityResolver['tunnelFactory'] {
		return undefined; // may be overridden
	}

	async $closeTunnel(remote: { host: string; port: number }, silent?: boolean): Promise<void> {
		if (this._extensionTunnels.has(remote.host)) {
			const hostMap = this._extensionTunnels.get(remote.host)!;
			if (hostMap.has(remote.port)) {
				if (silent) {
					hostMap.get(remote.port)!.disposeListener.dispose();
				}
				await hostMap.get(remote.port)!.tunnel.dispose();
				hostMap.delete(remote.port);
			}
		}
	}

	async $onDidTunnelsChange(): Promise<void> {
		this._onDidChangeTunnels.fire();
	}

	async $forwardPort(tunnelOptions: TunnelOptions, tunnelCreationOptions: TunnelCreationOptions): Promise<TunnelDto | string | undefined> {
		if (this._forwardPortProvider) {
			try {
				this.logService.trace('ForwardedPorts: (ExtHostTunnelService) Getting tunnel from provider.');
				const providedPort = this._forwardPortProvider(tunnelOptions, tunnelCreationOptions,);
				this.logService.trace('ForwardedPorts: (ExtHostTunnelService) Got tunnel promise from provider.');
				if (providedPort !== undefined) {
					const tunnel = await providedPort;
					this.logService.trace('ForwardedPorts: (ExtHostTunnelService) Successfully awaited tunnel from provider.');
					if (tunnel === undefined) {
						this.logService.error('ForwardedPorts: (ExtHostTunnelService) Resolved tunnel is undefined');
						return undefined;
					}
					if (!this._extensionTunnels.has(tunnelOptions.remoteAddress.host)) {
						this._extensionTunnels.set(tunnelOptions.remoteAddress.host, new Map());
					}
					const disposeListener = this._register(tunnel.onDidDispose(() => {
						this.logService.trace('ForwardedPorts: (ExtHostTunnelService) Extension fired tunnel\'s onDidDispose.');
						return this._proxy.$closeTunnel(tunnel.remoteAddress);
					}));
					this._extensionTunnels.get(tunnelOptions.remoteAddress.host)!.set(tunnelOptions.remoteAddress.port, { tunnel, disposeListener });
					return TunnelDtoConverter.fromApiTunnel(tunnel);
				} else {
					this.logService.trace('ForwardedPorts: (ExtHostTunnelService) Tunnel is undefined');
				}
			} catch (e) {
				this.logService.trace('ForwardedPorts: (ExtHostTunnelService) tunnel provider error');
				if (e instanceof Error) {
					return e.message;
				}
			}
		}
		return undefined;
	}

	async $applyCandidateFilter(candidates: CandidatePort[]): Promise<CandidatePort[]> {
		const filter = await Promise.all(candidates.map(candidate => this._showCandidatePort(candidate.host, candidate.port, candidate.detail ?? '')));
		const result = candidates.filter((candidate, index) => filter[index]);
		this.logService.trace(`ForwardedPorts: (ExtHostTunnelService) filtered from ${candidates.map(port => port.port).join(', ')} to ${result.map(port => port.port).join(', ')}`);
		return result;
	}
}

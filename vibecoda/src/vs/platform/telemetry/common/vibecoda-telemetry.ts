/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Vibecoda. All rights reserved.
 *  Licensed under the MIT License.
 *--------------------------------------------------------------------------------------------*/

export class VibecodaTelemetry {
    private endpoint = 'http://localhost:8080/collect';
    private sessionId = this.generateSessionId();
    
    private generateSessionId(): string {
        return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
    }
    
    public async sendEvent(eventName: string, properties?: Record<string, any>): Promise<void> {
        try {
            const payload = {
                event: eventName,
                sessionId: this.sessionId,
                timestamp: new Date().toISOString(),
                properties: properties || {},
                version: require('../../../../package.json').version
            };
            
            await fetch(this.endpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload)
            });
        } catch (error) {
            // Silently fail - don't break editor functionality
            console.debug('Telemetry error:', error);
        }
    }
    
    public trackCodeCompletion(accepted: boolean, model: string, latency: number): void {
        this.sendEvent('code_completion', {
            accepted,
            model,
            latency
        });
    }
    
    public trackEditorAction(action: string, context?: Record<string, any>): void {
        this.sendEvent('editor_action', {
            action,
            ...context
        });
    }
}

export const vibeTelemetry = new VibecodaTelemetry();

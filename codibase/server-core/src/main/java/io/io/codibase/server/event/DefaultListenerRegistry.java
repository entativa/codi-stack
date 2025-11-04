package io.codibase.server.event;

import io.codibase.commons.loader.AppLoader;
import io.codibase.commons.loader.ManagedSerializedForm;
import io.codibase.commons.utils.LockUtils;
import io.codibase.server.cluster.ClusterService;
import io.codibase.server.service.ProjectService;
import io.codibase.server.event.project.ProjectDeleted;
import io.codibase.server.event.project.ProjectEvent;
import io.codibase.server.event.project.ActiveServerChanged;
import io.codibase.server.persistence.SessionService;
import io.codibase.server.persistence.TransactionService;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class DefaultListenerRegistry implements ListenerRegistry, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultListenerRegistry.class);
	
	private final ProjectService projectService;
	
	private final TransactionService transactionService;
	
	private final SessionService sessionService;
	
	private final ClusterService clusterService;
	
	private volatile Map<Object, Collection<Method>> listenerMethods;
	
	private final Map<Class<?>, Collection<Listener>> listeners = new ConcurrentHashMap<>();
	
	@Inject
	public DefaultListenerRegistry(ProjectService projectService, ClusterService clusterService,
								   TransactionService transactionService, SessionService sessionService) {
		this.projectService = projectService;
		this.transactionService = transactionService;
		this.clusterService = clusterService;
		this.sessionService = sessionService;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(ListenerRegistry.class);
	}

	private Map<Object, Collection<Method>> getListenerMethods() {
		if (this.listenerMethods == null) {
			Map<Object, Collection<Method>> listenersBySingleton = new HashMap<>();
			for (Object singleton: AppLoader.getSingletons()) {
				Collection<Method> listeners = new ArrayList<>();
				Class<?> current = singleton.getClass();
				Set<Class<?>> encounteredEventTypes = new HashSet<>();
				while (current != Object.class) {
					for (Method method: current.getDeclaredMethods()) {
						if (method.isAnnotationPresent(Listen.class)) {
							if (method.getParameterTypes().length != 1) {
								throw new RuntimeException("Listener method " + method + " should have only one parameter");
							}
							Class<?> eventType = method.getParameterTypes()[0];
							if (!encounteredEventTypes.contains(eventType)) {
								listeners.add(method);
								encounteredEventTypes.add(eventType);
							}
						}
					}
					current = current.getSuperclass();
				}
				listenersBySingleton.put(singleton, listeners);
			}
			this.listenerMethods = listenersBySingleton;
		}
		return this.listenerMethods;
	}
	
	protected Collection<Listener> getListeners(Class<?> eventType) {
		Collection<Listener> listeners = this.listeners.get(eventType);
		if (listeners == null) {
			listeners = new ArrayList<>();
			for (Map.Entry<Object, Collection<Method>> entry: getListenerMethods().entrySet()) {
				for (Method method: entry.getValue()) {
					Class<?> paramType = method.getParameterTypes()[0];
					if (paramType.isAssignableFrom(eventType))
						listeners.add(new Listener(entry.getKey(), method));
				}
			}
			this.listeners.put(eventType, listeners);
		}
		return listeners;
	}
	
	@Override
	public void invokeListeners(Object event) {
		for (Listener listener: getListeners(event.getClass())) 
			listener.notify(event);
	}

	@Transactional
	@Override
	public void post(Object event) {
		if (event instanceof ProjectEvent) {
			ProjectEvent projectEvent = (ProjectEvent) event;
			Long projectId = projectEvent.getProject().getId();
			transactionService.runAfterCommit(() -> projectService.submitToActiveServer(projectId, () -> {
				SecurityUtils.bindAsSystem();
				try {
					String lockName = projectEvent.getLockName();
					if (lockName != null) {
						LockUtils.call(lockName, true, () -> {
							sessionService.run(() -> invokeListeners(event));
							return null;
						});
					} else {
						sessionService.run(() -> invokeListeners(event));
					}
				} catch (Exception e) {
					logger.error("Error invoking listeners", e);
				}
				return null;
			}));
		} else if (event instanceof ProjectDeleted) {
			ProjectDeleted projectDeleted = (ProjectDeleted) event;
			Long projectId = projectDeleted.getProjectId();
			String activeServer = projectService.getActiveServer(projectId, false);
			if (activeServer != null) {
				transactionService.runAfterCommit(() -> clusterService.submitToServer(activeServer, () -> {
					try {
						sessionService.run(() -> invokeListeners(event));
					} catch (Exception e) {
						logger.error("Error invoking listeners", e);
					}
					return null;
				}));
			}
		} else if (event instanceof ActiveServerChanged) {
			ActiveServerChanged activeServerChanged = (ActiveServerChanged) event;
			transactionService.runAfterCommit(() -> clusterService.submitToServer(activeServerChanged.getActiveServer(), () -> {
				try {
					sessionService.run(() -> invokeListeners(event));
				} catch (Exception e) {
					logger.error("Error invoking listeners", e);
				}
				return null;
			}));
		} else {
			invokeListeners(event);
		}
	}
	
}

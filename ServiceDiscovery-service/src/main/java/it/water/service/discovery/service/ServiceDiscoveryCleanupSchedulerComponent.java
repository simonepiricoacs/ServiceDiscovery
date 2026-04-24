package it.water.service.discovery.service;

import it.water.core.api.interceptors.OnActivate;
import it.water.core.api.interceptors.OnDeactivate;
import it.water.core.api.registry.ComponentRegistry;
import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.core.interceptors.annotations.Inject;
import it.water.service.discovery.api.ServiceRegistrationSystemApi;
import it.water.service.discovery.api.options.ServiceDiscoveryCleanupOptions;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@FrameworkComponent
public class ServiceDiscoveryCleanupSchedulerComponent {

    private static final Logger log = LoggerFactory.getLogger(ServiceDiscoveryCleanupSchedulerComponent.class);
    private static final long BOOTSTRAP_RETRY_SECONDS = 1L;

    @Inject
    @Setter
    private ServiceDiscoveryCleanupOptions cleanupOptions;

    @Inject
    @Setter
    private ServiceRegistrationSystemApi serviceRegistrationSystemApi;

    @Inject(injectOnceAtStartup = true)
    @Setter
    private ComponentRegistry componentRegistry;

    private final Object lifecycleMonitor = new Object();
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> bootstrapFuture;
    private ScheduledFuture<?> cleanupFuture;

    @OnActivate
    public void onActivate() {
        synchronized (lifecycleMonitor) {
            ensureSchedulerLocked();
            if (tryStartCleanupLocked()) {
                return;
            }
            if (bootstrapFuture == null || bootstrapFuture.isCancelled() || bootstrapFuture.isDone()) {
                log.debug("ServiceDiscovery cleanup scheduler waiting for dependencies; retrying every {} second(s)",
                        BOOTSTRAP_RETRY_SECONDS);
                bootstrapFuture = scheduler.scheduleAtFixedRate(this::retryStartCleanup,
                        BOOTSTRAP_RETRY_SECONDS,
                        BOOTSTRAP_RETRY_SECONDS,
                        TimeUnit.SECONDS);
            }
        }
    }

    private void retryStartCleanup() {
        synchronized (lifecycleMonitor) {
            tryStartCleanupLocked();
        }
    }

    private boolean tryStartCleanupLocked() {
        ServiceRegistrationSystemApi effectiveSystemApi = resolveSystemApi();
        if (effectiveSystemApi == null) {
            log.debug("ServiceDiscovery cleanup scheduler still waiting for ServiceRegistrationSystemApi");
            return false;
        }
        ServiceDiscoveryCleanupOptions effectiveCleanupOptions = resolveCleanupOptions();
        if (effectiveCleanupOptions == null) {
            log.debug("ServiceDiscovery cleanup scheduler still waiting for ServiceDiscoveryCleanupOptions");
            return false;
        }
        if (cleanupFuture != null && !cleanupFuture.isCancelled() && !cleanupFuture.isDone()) {
            return true;
        }
        serviceRegistrationSystemApi = effectiveSystemApi;
        cleanupOptions = effectiveCleanupOptions;
        long cleanupIntervalSeconds = cleanupOptions.getCleanupIntervalSeconds();
        long cleanupThresholdSeconds = cleanupOptions.getCleanupThresholdSeconds();
        if (cleanupThresholdSeconds <= cleanupIntervalSeconds) {
            log.warn("Cleanup threshold ({}) should be greater than cleanup interval ({})",
                    cleanupThresholdSeconds, cleanupIntervalSeconds);
        }
        cancelBootstrapLocked();
        cleanupFuture = scheduler.scheduleAtFixedRate(() -> {
            try {
                effectiveSystemApi.cleanupInactiveServices((int) cleanupThresholdSeconds);
            } catch (Exception e) {
                log.warn("ServiceDiscovery cleanup execution failed: {}", e.getMessage());
            }
        }, cleanupIntervalSeconds, cleanupIntervalSeconds, TimeUnit.SECONDS);
        log.info("ServiceDiscovery cleanup scheduler started: interval={}s threshold={}s",
                cleanupIntervalSeconds, cleanupThresholdSeconds);
        return true;
    }

    @OnDeactivate
    public void onDeactivate() {
        synchronized (lifecycleMonitor) {
            cancelBootstrapLocked();
            cancelCleanupLocked();
            if (scheduler != null) {
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        log.warn("ServiceDiscoveryCleanupScheduler did not terminate within 5s, forcing shutdown");
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    scheduler.shutdownNow();
                }
                scheduler = null;
            }
        }
    }

    private void ensureSchedulerLocked() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor(new CleanupThreadFactory());
        }
    }

    private void cancelBootstrapLocked() {
        if (bootstrapFuture != null) {
            bootstrapFuture.cancel(false);
            bootstrapFuture = null;
        }
    }

    private void cancelCleanupLocked() {
        if (cleanupFuture != null) {
            cleanupFuture.cancel(false);
            cleanupFuture = null;
        }
    }

    private ServiceRegistrationSystemApi resolveSystemApi() {
        if (serviceRegistrationSystemApi != null) {
            return serviceRegistrationSystemApi;
        }
        if (componentRegistry == null) {
            return null;
        }
        return componentRegistry.findComponent(ServiceRegistrationSystemApi.class, null);
    }

    private ServiceDiscoveryCleanupOptions resolveCleanupOptions() {
        if (cleanupOptions != null) {
            return cleanupOptions;
        }
        if (componentRegistry == null) {
            return null;
        }
        return componentRegistry.findComponent(ServiceDiscoveryCleanupOptions.class, null);
    }

    private static final class CleanupThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "ServiceDiscoveryCleanupScheduler");
            thread.setDaemon(true);
            return thread;
        }
    }
}

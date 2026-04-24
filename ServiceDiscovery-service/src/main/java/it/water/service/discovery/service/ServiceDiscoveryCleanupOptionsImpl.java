/*
 * Copyright 2024 Aristide Cittadino
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.water.service.discovery.service;

import it.water.core.api.bundle.ApplicationProperties;
import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.core.interceptors.annotations.Inject;
import it.water.service.discovery.api.options.ServiceDiscoveryCleanupOptions;
import it.water.service.discovery.model.ServiceDiscoveryConstants;
import lombok.Setter;

/**
 * Default implementation of {@link ServiceDiscoveryCleanupOptions} reading values
 * from {@link ApplicationProperties}. Provides sensible defaults so the component
 * is fully operational without any explicit configuration.
 */
@FrameworkComponent
public class ServiceDiscoveryCleanupOptionsImpl implements ServiceDiscoveryCleanupOptions {

    private static final long DEFAULT_INTERVAL_SECONDS = 30L;
    private static final long DEFAULT_THRESHOLD_SECONDS = 120L;

    @Inject
    @Setter
    private ApplicationProperties applicationProperties;

    @Override
    public long getCleanupIntervalSeconds() {
        if (applicationProperties == null) {
            return DEFAULT_INTERVAL_SECONDS;
        }
        return applicationProperties.getPropertyOrDefault(
                ServiceDiscoveryConstants.PROP_CLEANUP_INTERVAL_SECONDS, DEFAULT_INTERVAL_SECONDS);
    }

    @Override
    public long getCleanupThresholdSeconds() {
        if (applicationProperties == null) {
            return DEFAULT_THRESHOLD_SECONDS;
        }
        return applicationProperties.getPropertyOrDefault(
                ServiceDiscoveryConstants.PROP_CLEANUP_THRESHOLD_SECONDS, DEFAULT_THRESHOLD_SECONDS);
    }
}

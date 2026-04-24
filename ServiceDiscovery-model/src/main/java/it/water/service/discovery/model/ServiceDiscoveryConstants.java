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

package it.water.service.discovery.model;

/**
 * Property keys used by the ServiceDiscovery module.
 * Values are read via ApplicationProperties through the Options pattern
 * (see ServiceDiscoveryCleanupOptions).
 */
public abstract class ServiceDiscoveryConstants {

    /**
     * Interval in seconds between two runs of the inactive-services cleanup task.
     * Default: 30 seconds.
     */
    public static final String PROP_CLEANUP_INTERVAL_SECONDS  = "water.discovery.cleanup.interval.seconds";

    /**
     * Threshold in seconds after which a service with no recent heartbeat is
     * marked as OUT_OF_SERVICE. Must be strictly greater than the cleanup
     * interval to avoid false positives.
     * Default: 120 seconds.
     */
    public static final String PROP_CLEANUP_THRESHOLD_SECONDS = "water.discovery.cleanup.threshold.seconds";

    private ServiceDiscoveryConstants() {
        // prevent instantiation
    }
}

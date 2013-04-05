/**
 * Copyright 2009 OPS4J
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.useradmin.service.internal;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.ops4j.pax.useradmin.service.UserAdminConstants;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the configuration update and redirect to the underlying services
 */
public class ConfigurationListener implements ManagedService {

    private static final Logger                                 LOG = LoggerFactory.getLogger(ConfigurationListener.class);

    private final ServiceTracker<StorageProvider, PaxUserAdmin> providerTracker;
    private final String                                        type;

    /**
     * @param providerTracker
     */
    public ConfigurationListener(String type, ServiceTracker<StorageProvider, PaxUserAdmin> providerTracker) {
        this.type = type;
        this.providerTracker = providerTracker;
    }

    @Override
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
        Map<String, Object> config = new HashMap<String, Object>();
        if (properties != null) {
            Enumeration<String> keys = properties.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                config.put(key, properties.get(key));
            }
        }
        SortedMap<ServiceReference<StorageProvider>, PaxUserAdmin> tracked = providerTracker.getTracked();
        for (Entry<ServiceReference<StorageProvider>, PaxUserAdmin> entry : tracked.entrySet()) {
            if (type.equals(entry.getKey().getProperty(UserAdminConstants.STORAGEPROVIDER_TYPE))) {
                try {
                    entry.getValue().configurationUpdated(config);
                } catch (RuntimeException e) {
                    LOG.error("Configurationupdate failed for type {}", type, e);
                }
            }
        }
    }
}

/*
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

import org.ops4j.pax.useradmin.service.PaxUserAdminConstants;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
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

    private Map<String, Object>                                 currentConfig;

    private final ServiceRegistration<ManagedService>           service;

    ConfigurationListener(String type, ServiceTracker<StorageProvider, PaxUserAdmin> providerTracker, Dictionary<String, Object> properties,
                          BundleContext context) {
        this.type = type;
        this.providerTracker = providerTracker;
        service = context.registerService(ManagedService.class, this, properties);
    }

    @Override
    public synchronized void updated(Dictionary<String, ?> properties) throws ConfigurationException {
        Map<String, Object> config = new HashMap<String, Object>();
        if (properties != null) {
            Enumeration<String> keys = properties.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                config.put(key, properties.get(key));
            }
        }
        this.currentConfig = config;
        SortedMap<ServiceReference<StorageProvider>, PaxUserAdmin> tracked = providerTracker.getTracked();
        if (!tracked.isEmpty()) {
            LOG.info("Update Konfiguration for {} PaxUserAdmins of type {}...", tracked.size(), type);
            for (Entry<ServiceReference<StorageProvider>, PaxUserAdmin> entry : tracked.entrySet()) {
                if (type.equals(entry.getKey().getProperty(PaxUserAdminConstants.STORAGEPROVIDER_TYPE))) {
                    updateProvider(entry.getValue());
                }
            }
        }
    }

    synchronized void updateProvider(PaxUserAdmin admin) throws ConfigurationException {
        try {
            admin.configurationUpdated(this.currentConfig);
        } catch (RuntimeException e) {
            LOG.error("Configurationupdate failed for type {}", type, e);
        }
    }

    /**
     * 
     */
    void unregister() {
        service.unregister();
    }
}

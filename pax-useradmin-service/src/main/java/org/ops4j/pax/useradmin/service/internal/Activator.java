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

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.ops4j.pax.useradmin.service.PaxUserAdminConstants;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;
import org.osgi.service.useradmin.UserAdminListener;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activator which starts a ServiceTracker for StorageProvider services.
 * UserAdmin services are started/stopped on adding/removing providers.
 * 
 * @author Matthias Kuespert
 * @since 02.07.2009
 */
public class Activator implements BundleActivator, ServiceTrackerCustomizer<StorageProvider, PaxUserAdmin> {

    /**
     * Maximum number of parralllel event threads
     */
    private static final int                                     MAXIMUM_POOL_SIZE = 10;

    private static final Logger                                  LOG               = LoggerFactory.getLogger(Activator.class);

    private ServiceTracker<StorageProvider, PaxUserAdmin>        providerTracker;

    private BundleContext                                        context;

    private ServiceTracker<EventAdmin, EventAdmin>               eventAdminTracker;

    private ServiceTracker<LogService, LogService>               logServiceTracker;

    private ServiceTracker<UserAdminListener, UserAdminListener> listenerTracker;

    private final Map<String, ConfigurationListener>             managedServiceMap = new HashMap<String, ConfigurationListener>();

    private final ExecutorService                                eventExecutor     = new ThreadPoolExecutor(0, MAXIMUM_POOL_SIZE, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        LOG.info("Start PaxUserAdmin service bundle {} (version {}) and wait for StorageProvider...", context.getBundle().getSymbolicName(), context.getBundle().getVersion());
        //Create all tracker
        providerTracker = new ServiceTracker<StorageProvider, PaxUserAdmin>(context, StorageProvider.class.getName(), this);
        eventAdminTracker = new ServiceTracker<EventAdmin, EventAdmin>(context, EventAdmin.class, null);
        logServiceTracker = new ServiceTracker<LogService, LogService>(context, LogService.class, null);
        listenerTracker = new ServiceTracker<UserAdminListener, UserAdminListener>(context, UserAdminListener.class, null);
        //and open them...
        logServiceTracker.open();
        eventAdminTracker.open();
        providerTracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        LOG.info("Shutdown PaxUserAdmin service bundle {} (version {})...", context.getBundle().getSymbolicName(), context.getBundle().getVersion());
        providerTracker.close();
        eventAdminTracker.close();
        logServiceTracker.close();
        listenerTracker.close();
        synchronized (managedServiceMap) {
            Collection<ConfigurationListener> values = managedServiceMap.values();
            for (ConfigurationListener serviceRegistration : values) {
                serviceRegistration.unregister();
            }
            managedServiceMap.clear();
        }
    }

    @Override
    public PaxUserAdmin addingService(ServiceReference<StorageProvider> reference) {
        String type = (String) reference.getProperty(PaxUserAdminConstants.STORAGEPROVIDER_TYPE);
        if (null == type) {
            LOG.error("Ignoring provider without storage provider type");
            return null;
        }
        StorageProvider storageProvider = context.getService(reference);
        if (storageProvider != null) {
            try {
                PaxUserAdmin userAdminImpl = new PaxUserAdmin(storageProvider, logServiceTracker, eventAdminTracker, listenerTracker, eventExecutor);
                userAdminImpl.register(context, type, (Long) reference.getProperty(Constants.SERVICE_ID));
                LOG.info("New UserAdmin for StorageProvider {} (service.id = {}) is now available.", type, reference.getProperty(Constants.SERVICE_ID));
                synchronized (managedServiceMap) {
                    String pid = reference.getBundle().getSymbolicName();
                    ConfigurationListener l = managedServiceMap.get(pid);
                    if (l == null) {
                        LOG.info("The configuration PID for StorageProviders of type {} is {}", type, pid);
                        Dictionary<String, Object> properties = new Hashtable<String, Object>();
                        properties.put(Constants.SERVICE_PID, pid);
                        ConfigurationListener listener = new ConfigurationListener(type, providerTracker, properties, context);
                        managedServiceMap.put(pid, listener);
                    } else {
                        try {
                            l.updateProvider(userAdminImpl);
                        } catch (ConfigurationException e) {
                            LOG.error("Can't update configuration: {}", e.getMessage(), e);
                        }
                    }
                }
                return userAdminImpl;
            } catch (RuntimeException e) {
                //unget the service
                context.ungetService(reference);
                LOG.warn("registration of UserAdmin failed!", e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void modifiedService(ServiceReference<StorageProvider> reference, PaxUserAdmin service) {
        // we not support modifications yet
    }

    @Override
    public void removedService(ServiceReference<StorageProvider> reference, PaxUserAdmin service) {
        String type = (String) reference.getProperty(PaxUserAdminConstants.STORAGEPROVIDER_TYPE);
        //unget whatever happens
        context.ungetService(reference);
        service.unregister();
        LOG.info("UserAdmin for StorageProvider {} (service.id = {}) is now removed and no longer available.", type, reference.getProperty(Constants.SERVICE_ID));
    }
}

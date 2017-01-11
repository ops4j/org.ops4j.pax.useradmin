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
package org.ops4j.pax.useradmin.provider.jpa.internal;

import javax.persistence.EntityManagerFactory;

import org.ops4j.pax.useradmin.provider.jpa.ConfigurationConstants;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aktivates the JPA {@link StorageProvider} and tracks the underlying service
 * objects. For each {@link EntityManagerFactory} matching the filter
 * {@link #EMF_FILTER} a cosresponding {@link JPAStorageProvider} is registered
 */
public class Activator
        implements BundleActivator, ServiceTrackerCustomizer<EntityManagerFactory, JPAStorageProvider> {

    /**
     * filter used to track down releveant EMF services
     */
    private static final String                                      EMF_FILTER = "(&(" + Constants.OBJECTCLASS + "=" + EntityManagerFactory.class.getName()
                                                                                        + ")(" + EntityManagerFactoryBuilder.JPA_UNIT_NAME + "="
                                                                                        + ConfigurationConstants.PUNIT_NAME + "))";
    private static final Logger                                      LOG        = LoggerFactory.getLogger(Activator.class);
    private ServiceTracker<EntityManagerFactory, JPAStorageProvider> serviceTracker;
    private BundleContext                                            context;

    public void start(BundleContext context) throws Exception {
        this.context = context;
        LOG.info("Startup storage provider bundle {} (version {}), waiting for coresponding service...", context.getBundle().getSymbolicName(), context.getBundle().getVersion());
        serviceTracker = new ServiceTracker<EntityManagerFactory, JPAStorageProvider>(context, context.createFilter(EMF_FILTER), this);
        serviceTracker.open();
    }

    public void stop(BundleContext context) throws Exception {
        LOG.info("Shutdown storage provider bundle {} (version {})...", context.getBundle().getSymbolicName(), context.getBundle().getVersion());
        serviceTracker.close();
    }

    public JPAStorageProvider addingService(ServiceReference<EntityManagerFactory> reference) {
        EntityManagerFactory service = context.getService(reference);
        if (service != null) {
            try {
                JPAStorageProvider jpaStorageProvider = new JPAStorageProvider(service, (Long) reference.getProperty(Constants.SERVICE_ID));
                jpaStorageProvider.register(context);
                LOG.info("New JPAStorageProvider (EMF service.id = {}) is now ready to use and registered.", reference.getProperty(Constants.SERVICE_ID));
                return jpaStorageProvider;
            } catch (RuntimeException e) {
                LOG.warn("registration of storage provider failed!", e);
                //unget the service now...
                context.ungetService(reference);
                return null;
            }
        } else {
            //Service has gone away between calls...
            return null;
        }

    }

    public void modifiedService(ServiceReference<EntityManagerFactory> reference, JPAStorageProvider service) {
        // we are not interested in modifications of properties (for now)
    }

    public void removedService(ServiceReference<EntityManagerFactory> reference, JPAStorageProvider service) {
        // whatever happens, we unget the service here...
        context.ungetService(reference);
        service.unregister();
        LOG.info("JPAStorageProvider (EMF service.id = {}) is now removed and no longer active.", reference.getProperty(Constants.SERVICE_ID));
    }

}

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

package org.ops4j.pax.useradmin.provider.preferences.internal;

import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.PreferencesService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activator of the Pax UserAdmin Preferences StorageProvider bundle.
 * 
 * @author Matthias Kuespert
 * @since 08.07.2009
 */
public class Activator implements BundleActivator, ServiceTrackerCustomizer<PreferencesService, PreferencesStorageProvider> {

    private static final Logger                                            LOG = LoggerFactory.getLogger(Activator.class);

    private ServiceTracker<PreferencesService, PreferencesStorageProvider> preferencesTracker;

    private BundleContext                                                  context;

    /**
     * Initialize servicetrackers
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        this.context = context;
        LOG.info("Startup storage provider bundle {} (version {}), waiting for coresponding service...", context.getBundle().getSymbolicName(), context.getBundle().getVersion());
        preferencesTracker = new ServiceTracker<PreferencesService, PreferencesStorageProvider>(context, PreferencesService.class, this);
        preferencesTracker.open();
    }

    /**
     * Close servicetrackers and clean up
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        LOG.info("Shutdown storage provider bundle {} (version {})...", context.getBundle().getSymbolicName(), context.getBundle().getVersion());
        preferencesTracker.close();
    }

    /**
     * Called when a new {@link PreferencesService} registeres.
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(ServiceReference)
     */
    public PreferencesStorageProvider addingService(ServiceReference<PreferencesService> reference) {
        PreferencesService preferences = context.getService(reference);
        if (preferences != null) {
            try {
                PreferencesStorageProvider provider = new PreferencesStorageProvider(preferences, (Long) reference.getProperty(Constants.SERVICE_ID));
                provider.register(context);
                LOG.info("New PreferencesStorageProvider (PreferencesService service.id = {}) is now ready to use and registered.", reference.getProperty(Constants.SERVICE_ID));
                return provider;
            } catch (RuntimeException e) {
                //unget the service
                context.ungetService(reference);
                LOG.warn("registration of storage provider failed!", e);
                return null;
            } catch (StorageException e) {
                context.ungetService(reference);
                LOG.warn("registration of storage provider failed!", e);
                return null;
            }
        } else {
            return null;
        }

    }

    /**
     * Called when a known {@link PreferencesService} modified registration
     * properties.
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(ServiceReference,
     *      Object)
     */
    public void modifiedService(ServiceReference<PreferencesService> reference, PreferencesStorageProvider service) {
        // no-op
    }

    /**
     * Called when a known {@link PreferencesService} unregisters.
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(ServiceReference,
     *      Object)
     */
    public void removedService(ServiceReference<PreferencesService> reference, PreferencesStorageProvider service) {
        //unget whatever happens...
        context.ungetService(reference);
        service.unregister();
        LOG.info("PreferencesStorageProvider (PreferencesService service.id = {}) is now removed and no longer active.", reference.getProperty(Constants.SERVICE_ID));
    }

}

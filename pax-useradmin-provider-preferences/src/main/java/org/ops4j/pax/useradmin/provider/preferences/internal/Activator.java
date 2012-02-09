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

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.ops4j.pax.useradmin.provider.preferences.ConfigurationConstants;
import org.ops4j.pax.useradmin.service.UserAdminConstants;
import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.service.prefs.PreferencesService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * Activator of the Pax UserAdmin Preferences StorageProvider bundle.
 * 
 * @author Matthias Kuespert
 * @since 08.07.2009
 */
public class Activator implements BundleActivator, ServiceTrackerCustomizer, LogService {

    private BundleContext m_bundleContext = null;
    private ServiceTracker m_preferencesTracker = null;
    private ServiceTracker m_logServiceTracker = null;
    
    // Map PreferencesService references to StorageProvider registrations
    private Map<ServiceReference, ServiceRegistration> m_registrations =
        new HashMap<ServiceReference, ServiceRegistration>();
    
    private void registerStorageProvider(ServiceReference reference, PreferencesService preferences) {
        try {
            StorageProvider provider = new StorageProviderImpl(preferences, this);

            Dictionary<String, String> properties = new Hashtable<String, String>();
            properties.put(Constants.SERVICE_PID,
                ConfigurationConstants.SERVICE_PID);
            properties.put(UserAdminConstants.STORAGEPROVIDER_TYPE,
                ConfigurationConstants.STORAGEPROVIDER_TYPE);

            ServiceRegistration registration =
                m_bundleContext.registerService(StorageProvider.class.getName(), provider, properties);

            m_registrations.put(reference, registration);
        }
        catch (StorageException e) {
            log(LogService.LOG_ERROR, "Failed to created preferences storage provider", e);
        }
    }
    
    private void unregisterStorageProvider(ServiceReference reference) {
        ServiceRegistration registration = m_registrations.get(reference);
        if (registration != null) {
            registration.unregister();
        }
    }

    /**
     * Initialize servicetrackers
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        m_bundleContext = context;
        m_logServiceTracker = new ServiceTracker(context, LogService.class.getName(), null);
        m_logServiceTracker.open();
        m_preferencesTracker = new ServiceTracker(context, PreferencesService.class.getName(), this);
        m_preferencesTracker.open();
    }

    /**
     * Close servicetrackers and clean up
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        m_preferencesTracker.close();
        m_preferencesTracker = null;
        m_logServiceTracker.close();
        m_logServiceTracker = null;
        m_bundleContext = null;
    }

    /**
     * Called when a new {@link PreferencesService} registeres.
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(ServiceReference)
     */
    public Object addingService(ServiceReference reference) {
        PreferencesService preferences = (PreferencesService) m_bundleContext.getService(reference);
        if(preferences == null){
            return null;
        }
        registerStorageProvider(reference, preferences);
        return preferences;
    }

    /**
     * Called when a known {@link PreferencesService} modified registration properties.
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(ServiceReference, Object)
     */
    public void modifiedService(ServiceReference reference, Object service) {
        // no-op
    }

    /**
     * Called when a known {@link PreferencesService} unregisters.
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(ServiceReference, Object)
     */
    public void removedService(ServiceReference reference, Object service) {
        unregisterStorageProvider(reference);
    }

    /**
     * @see org.osgi.service.log.LogService#log(int, String)
     */
    public void log(int level, String message) {
        log(null, level, message, null);
    }

    /**
     * @see org.osgi.service.log.LogService#log(int, String, Throwable)
     */
    public void log(int level, String message, Throwable exception) {
        log(null, level, message, exception);
    }

    /**
     * @see org.osgi.service.log.LogService#log(ServiceReference, int, String)
     */
    public void log(ServiceReference sr, int level, String message) {
        log(sr, level, message, null);
    }

    /**
     * @see org.osgi.service.log.LogService#log(ServiceReference, int, String, Throwable)
     */
    public void log(ServiceReference sr, int level, String message, Throwable exception) {
        LogService logService = (LogService) m_logServiceTracker.getService();
        if (null != logService) {
            logService.log(sr, level, message, exception);
        }
    }
}

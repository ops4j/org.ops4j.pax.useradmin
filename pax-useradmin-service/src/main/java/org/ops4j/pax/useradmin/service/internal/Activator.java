/*
 * Copyright 2009 Matthias Kuespert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.ops4j.pax.useradmin.service.internal;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.ops4j.pax.useradmin.service.UserAdminConstants;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * Activator which starts a ServiceTracker for StorageProvider services.
 * UserAdmin services are started/stopped on adding/removing providers.
 * 
 * @author Matthias Kuespert
 * @since 02.07.2009
 */
public class Activator implements BundleActivator, ServiceTrackerCustomizer {

//    private static final Log LOGGER = LogFactory.getLog(Activator.class);
  
    private ServiceTracker m_providerTracker = null;
    private BundleContext m_context = null;
    
    private Map<String, ServiceRegistration> m_services = new HashMap<String, ServiceRegistration>();
    
    private StorageProvider startUserAdminService(String type, ServiceReference storageProvider) {
        if (null == type || "".equals(type)) {
             throw new IllegalArgumentException("Internal error: parameter 'type' must not be null or empty.");
        }
        StorageProvider provider = (StorageProvider) m_context.getService(storageProvider);
        if (null == provider) {
            // TODO: what to do?
            throw new IllegalStateException("Internal error: StorageProvider service implementation not available.");
        }
        if (!m_services.containsKey(type)) {
            System.out.println(" -- starting new UserAdmin service for provider: " + type);
            Dictionary<String, String> properties = new Hashtable<String, String>();
            properties.put(Constants.SERVICE_PID, UserAdminImpl.PID + "." + type);
            properties.put(UserAdminConstants.STORAGEPROVIDER_TYPE, type);
            UserAdminImpl userAdmin = new UserAdminImpl(m_context,
                                                        new ServiceTracker(m_context,
                                                                           storageProvider,
                                                                           null),
                                                        new ServiceTracker(m_context,
                                                                           LogService.class.getName(),
                                                                           null),
                                                        new ServiceTracker(m_context,
                                                                           EventAdmin.class.getName(),
                                                                           null));
            ServiceRegistration userAdminRegistration = m_context.registerService(UserAdmin.class.getName(),
                                                                                  userAdmin, properties);
            m_services.put(type, userAdminRegistration);
        }
        return provider;
    }
    
    private void stopUserAdminService(String type) {
        if (null == type || "".equals(type)) {
            throw new IllegalArgumentException("Internal error: parameter 'type' must not be null or empty.");
        }
        if (m_services.containsKey(type)) {
            ServiceRegistration registration = m_services.get(type);
            registration.unregister();
            m_services.remove(type);
        }
    }

    // BundleActivator interface
    /**
     * @see BundleActivator#start(BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        
        m_context = context;
        // install a service tracker to track StorageProvider services and
        // configure ourselves as event handler
        m_providerTracker = new ServiceTracker(context,
                                               StorageProvider.class.getName(),
                                               this);
        m_providerTracker.open();
    }

    /**
     * @see BundleActivator#stop(BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        m_providerTracker.close();
        // unregister all UserAdmin services we created
        for (ServiceRegistration registration : m_services.values()) {
            registration.unregister();
        }
        m_providerTracker = null;
    }
    
    // ServiceTrackerCustomizer interface
    
    /**
     * A new StorageProvider service was detected ...
     * 
     * @see ServiceTrackerCustomizer#addingService(ServiceReference)
     */
    public Object addingService(ServiceReference reference) {

//        System.out.println("adding service: " + reference.getProperty(Constants.SERVICE_ID));
//        System.out.println("                with interface(s): ");
//        for (String name : (String[]) reference.getProperty(Constants.OBJECTCLASS)) {
//            System.out.println("   " + Constants.OBJECTCLASS + " = " + name);
//        }
//        
//        for (String key : reference.getPropertyKeys()) {
//            System.out.println("  key: " + key);
//        }
        
        String type = (String) reference.getProperty(UserAdminConstants.STORAGEPROVIDER_TYPE);
        if (null == type) {
            // ignore if property not set: it's not a useable provider
            return null;
        }
        return startUserAdminService(type, reference);
    }
    
    /**
     * @see ServiceTrackerCustomizer#modifiedService(ServiceReference, Object)
     */
    public void modifiedService(ServiceReference reference, Object service) {
        // TODO Auto-generated method stub
        System.out.println("modified service: " + reference.getProperty(Constants.BUNDLE_SYMBOLICNAME));
        System.out.println("modified service: " + reference.getProperty(Constants.SERVICE_PID));
    }
    
    /**
     * @see ServiceTrackerCustomizer#removedService(ServiceReference, Object)
     */
    public void removedService(ServiceReference reference, Object service) {
        String type = (String) reference.getProperty(UserAdminConstants.STORAGEPROVIDER_TYPE);
        if (null == type) {
            // ignore if property not set: it's not a useable provider
            return;
        }
        stopUserAdminService(type);
    }
}

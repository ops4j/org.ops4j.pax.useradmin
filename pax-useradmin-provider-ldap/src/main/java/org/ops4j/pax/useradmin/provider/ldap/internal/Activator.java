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
package org.ops4j.pax.useradmin.provider.ldap.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.ops4j.pax.useradmin.provider.ldap.ConfigurationConstants;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;

import com.novell.ldap.LDAPConnection;

/**
 * Activator of the Pax UserAdmin bundle.
 * 
 * @author Matthias Kuespert
 * @since  08.07.2009
 */
public class Activator implements BundleActivator {

    /**
     * The service implementation provided by the bundle.
     */
    private StorageProviderImpl m_serviceImpl = null;
    
    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        m_serviceImpl = new StorageProviderImpl(context, new LDAPConnection());
        
//        Dictionary<String, String> properties = new Hashtable<String, String>();
//        properties.put(StorageProviderImpl.PROP_LDAP_SERVER_URL, StorageProviderImpl.DEFAULT_LDAP_SERVER_URL);
//        properties.put(StorageProviderImpl.PROP_LDAP_SERVER_PORT, StorageProviderImpl.DEFAULT_LDAP_SERVER_PORT);
//        properties.put(StorageProviderImpl.PROP_LDAP_ROOT_DN, StorageProviderImpl.DEFAULT_LDAP_ROOT_DN);
//        m_serviceImpl.updated(properties);
        
        
        Dictionary<String, String> serviceProperties = new Hashtable<String, String>();
        serviceProperties.put(Constants.SERVICE_PID, ConfigurationConstants.SERVICE_PID);
        context.registerService(new String[] { StorageProvider.class.getName(), ManagedService.class.getName() }, m_serviceImpl, serviceProperties);
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        if (null != m_serviceImpl) {
//            m_serviceImpl.stop();
        }
    }

}

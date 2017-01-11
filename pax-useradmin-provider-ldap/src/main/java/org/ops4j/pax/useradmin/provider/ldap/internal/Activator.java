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

package org.ops4j.pax.useradmin.provider.ldap.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.ops4j.pax.useradmin.provider.ldap.ConfigurationConstants;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.novell.ldap.LDAPConnection;

/**
 * Activator of the Pax UserAdmin LDAP StorageProvider bundle.
 */
public class Activator
        implements BundleActivator {

    /**
     * Create and register the <code>StorageProvider</code> service.
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        //
        // create LDAP connection access object
        //
        LDAPConnection connection = new LDAPConnection();
        //
        // set service properties
        //
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(org.ops4j.pax.useradmin.service.PaxUserAdminConstants.STORAGEPROVIDER_TYPE, ConfigurationConstants.STORAGEPROVIDER_TYPE);
        //
        // create & register service implementation
        //
        context.registerService(StorageProvider.class, new StorageProviderImpl(connection), properties);
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
    }
}

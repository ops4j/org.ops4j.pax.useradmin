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
package org.ops4j.pax.useradmin.provider.preferences.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.ops4j.pax.useradmin.provider.preferences.ConfigurationConstants;
import org.ops4j.pax.useradmin.service.UserAdminConstants;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Activator which starts the PreferencesService based
 * <code>StorageProvider</code> service.
 * 
 * @author Matthias Kuespert
 * @since 08.07.2009
 */
public class Activator implements BundleActivator {

    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(UserAdminConstants.STORAGEPROVIDER_TYPE,
                       ConfigurationConstants.STORAGEPROVIDER_TYPE);
        //
        context.registerService(StorageProvider.class.getName(),
                                new StorageProviderImpl(context),
                                properties);
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        // nothing to do
    }
}

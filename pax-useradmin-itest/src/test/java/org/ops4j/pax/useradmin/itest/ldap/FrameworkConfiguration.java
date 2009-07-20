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
package org.ops4j.pax.useradmin.itest.ldap;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.Assert;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.useradmin.provider.ldap.ConfigurationConstants;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Provides configuration specific to the LDAP based variant of
 * the UserAdmin service.
 * 
 * @author Matthias Kuespert
 * @since 14.07.2009
 */
public class FrameworkConfiguration {

    /**
     * @return The additional configuration needed for testing the Preferences
     *         service based variant of the UserAdmin service
     */
    protected static Option get() {
        return composite(rawPaxRunnerOption("bootDelegation", "javax.naming.*"),
                         mavenBundle().groupId("org.ops4j.pax.useradmin")
                                      .artifactId("pax-useradmin-provider-ldap")
                                      .version("0.0.1-SNAPSHOT").startLevel(4));
    }
    
    /**
     * Standard initialization for all tests: update configuration for the StorageProvider bundle
     * 
     * @param context The <code>BundleContext</code> of the bundle containing the test.
     */
    @SuppressWarnings(value = "unchecked")
    protected static void setup(BundleContext context) {
        
        // TODO create partition and structural entries??
        
        ServiceReference refProvider = context.getServiceReference(StorageProvider.class.getName());
        Assert.assertNotNull("No StorageProvider service reference found", refProvider);
        //
        ServiceReference refConfigAdmin = context.getServiceReference(ConfigurationAdmin.class.getName());
        Assert.assertNotNull("No ConfigurationAdmin service reference found", refConfigAdmin);
        ConfigurationAdmin configAdmin = (ConfigurationAdmin) context.getService(refConfigAdmin);
        Assert.assertNotNull("No ConfigurationAdmin service found", configAdmin);
        try {
            Configuration config = configAdmin.getConfiguration(ConfigurationConstants.SERVICE_PID,
                                                                refProvider.getBundle().getLocation());
            Dictionary<String, String> properties = config.getProperties();
            if (null == properties) {
                properties = new Hashtable<String, String>();
            }
            properties.put(ConfigurationConstants.PROP_LDAP_SERVER_URL,
                           ConfigurationConstants.DEFAULT_LDAP_SERVER_URL);
            properties.put(ConfigurationConstants.PROP_LDAP_SERVER_PORT,
                           ConfigurationConstants.DEFAULT_LDAP_SERVER_PORT);
            properties.put(ConfigurationConstants.PROP_LDAP_ROOT_DN,
                           ConfigurationConstants.DEFAULT_LDAP_ROOT_DN);
            config.update(properties);

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }
    }
    
    protected static void cleanup() {
        // TODO delete all entries from users and groups??
    }
}

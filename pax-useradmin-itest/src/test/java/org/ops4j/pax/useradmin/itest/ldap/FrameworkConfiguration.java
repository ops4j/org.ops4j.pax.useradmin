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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.Assert;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.ldapserver.apacheds.ApacheDSConfiguration;
import org.ops4j.pax.ldapserver.apacheds.ApacheDSServer;
import org.ops4j.pax.useradmin.provider.ldap.ConfigurationConstants;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

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
        return composite(rawPaxRunnerOption("bootDelegation", "javax.naming.ldap.*"),
                         mavenBundle().groupId("org.ops4j.pax.ldapserver")
                                      .artifactId("pax-ldapserver-apacheds")
                                      .version("0.0.1-SNAPSHOT").startLevel(4),
                         mavenBundle().groupId("org.ops4j.pax.useradmin")
                                      .artifactId("pax-useradmin-provider-ldap")
                                      .version("0.0.1-SNAPSHOT").startLevel(5));
    }
    
    private static void delete(File root, boolean deleteRoot) {
        if (root.isDirectory()) {
            for (File f : root.listFiles()) {
                delete(f, true);
            }
        }
        if (deleteRoot) {
            root.delete();
        }
    }
    
    private static void copyResourceToFile(String resourcePath, File directory) {
        InputStream is = FrameworkConfiguration.class.getResourceAsStream(resourcePath);
        Assert.assertNotNull("Could not load file: " + resourcePath, is);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(directory.getPath()
                                                                      + resourcePath));
            while (true) {
                String line = reader.readLine();
                if (null == line)
                    break;
                writer.append(line);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(  "Error copying resource " + resourcePath + " to "
                        + directory.getPath() + " - " + e.getMessage());
        }
    }
    
    /**
     * Standard initialization for all tests: update configuration for the StorageProvider bundle
     * 
     * @param context The <code>BundleContext</code> of the bundle containing the test.
     */
    @SuppressWarnings(value = "unchecked")
    protected static void setup(BundleContext context) {
        
        ServiceReference refConfigAdmin = context.getServiceReference(ConfigurationAdmin.class.getName());
        Assert.assertNotNull("No ConfigurationAdmin service reference found", refConfigAdmin);
        ConfigurationAdmin configAdmin = (ConfigurationAdmin) context.getService(refConfigAdmin);
        Assert.assertNotNull("No ConfigurationAdmin service found", configAdmin);
        //
        // clean ApacheDS working directory
        //
        File workDir = new File("./server-work");
        if (workDir.exists() && workDir.isDirectory()) {
            delete(workDir, true);
        }
        //
        // clean/initialize data directory
        //
        File dataDir = new File("./server-data");
        if (dataDir.exists() && dataDir.isDirectory()) {
            delete(dataDir, false);
        } else {
            dataDir.mkdir();
        }
        copyResourceToFile("/ldaptree-test.ldif", dataDir);
        //
        // configure ApacheDS LDAP server
        //
        try {
            ServiceReference refLDAPServer = context.getServiceReference(ApacheDSServer.class.getName());
            Assert.assertNotNull("No LDAP server service reference found", refLDAPServer);
            //
            Configuration config = configAdmin.getConfiguration(ApacheDSConfiguration.SERVICE_PID,
                                                                refLDAPServer.getBundle().getLocation());
            Dictionary<String, String> properties = config.getProperties();
            if (null == properties) {
                properties = new Hashtable<String, String>();
            }
            properties.put(ApacheDSConfiguration.PROP_DATA_DIR,
                           dataDir.getPath());
            properties.put(ApacheDSConfiguration.PROP_LDAP_SERVER_PORT,
                           ApacheDSConfiguration.DEFAULT_PORT_LDAP);
            properties.put(ApacheDSConfiguration.PROP_PARTITIONS,
                           "ops4j");
            properties.put(ApacheDSConfiguration.PROP_PARTITION_DN_STUB + "ops4j",
                           "dc=ops4j,dc=org");
            config.update(properties);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //
        // wait for ApacheDS to be ready ...
        //
        ServiceTracker apacheDSTracker = new ServiceTracker(context,
                                                            context.getServiceReference(ApacheDSServer.class.getName()),
                                                            null);
        apacheDSTracker.open();
        try {
            apacheDSTracker.waitForService(5000);
        } catch (InterruptedException e) {
            // ignore
        }
        ApacheDSServer server = (ApacheDSServer) apacheDSTracker.getService();
        while (!server.isAvailable()) {
            try {
                Thread.sleep(500);
                System.out.println("... waiting for ApacheDS ...");
            } catch (InterruptedException e) {
                // ignore
            }
        }
        //
        // configure LDAP storage provider
        //
        ServiceReference refProvider = context.getServiceReference(StorageProvider.class.getName());
        Assert.assertNotNull("No StorageProvider service reference found", refProvider);
        //
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
    }
}

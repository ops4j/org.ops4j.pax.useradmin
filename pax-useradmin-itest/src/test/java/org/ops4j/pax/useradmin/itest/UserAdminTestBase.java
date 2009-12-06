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

package org.ops4j.pax.useradmin.itest;

import static org.ops4j.pax.exam.CoreOptions.*;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.useradmin.itest.service.CopyFilesEnvironmentCustomizer;
import org.osgi.framework.BundleContext;

/**
 * Abstract base class for all integration tests.
 * 
 * @author Matthias Kuespert
 * @since 12.07.2009
 */
public abstract class UserAdminTestBase {

    /**
     * Change this to false to disable security during tests.
     */
    private static boolean m_doEnableSecurity = false; // true;
    
    /**
     * Abstract method used to retrieve the <code>BundleContext</code>.
     * 
     * @return <code>BundleContext</code> used throughout the tests.
     */
    protected abstract BundleContext getBundleContext();

    /**
     * Checks if security should be enabled when running the test.
     * 
     * @return True if security should be enabled in tests, false otherwise.
     */
    protected static boolean doEnableSecurity() {
        return m_doEnableSecurity;
    }

    // Note:
    //   Only basic security is tested: if AllPermissions is set or not.
    //   
    //   The various permissions can only be tested seperately with (Conditional)PermissionAdmin installed,
    //   since Felix autmatically gives AllPermission to its bundles.
    //   
    //   see: http://www.mail-archive.com/users@felix.apache.org/msg02636.html

    /**
     * @return The basic OSGi framework configuration used to run the tests.
     */
    protected static Option getBasicFrameworkConfiguration() {
        return composite(when(doEnableSecurity()).useOptions(new CopyFilesEnvironmentCustomizer().sourceDir("src/test/resources")
                                                                                                 .sourceFilter(".*.permissions")
                                                                                                 .targetDir("/permissions"),
                                                             systemProperty("java.security.manager"),
//                                                           systemProperty("java.security.debug").value("access,failure"),
                                                             systemProperty("java.security.policy").value("permissions/useradmin-test.permissions")),
                         mavenBundle().groupId("org.osgi")
                                      .artifactId("org.osgi.core")
                                      .versionAsInProject().startLevel(1),
                         mavenBundle().groupId("org.osgi")
                                      .artifactId("org.osgi.compendium")
                                      .versionAsInProject().startLevel(1),
                         mavenBundle().groupId("org.ops4j.pax.logging")
                                      .artifactId("pax-logging-api")
                                      .versionAsInProject().startLevel(1),
                         mavenBundle().groupId("org.ops4j.pax.logging")
                                      .artifactId("pax-logging-service")
                                      .versionAsInProject().startLevel(1),
                         mavenBundle().groupId("org.apache.felix")
                                      .artifactId("org.apache.felix.eventadmin")
                                      .versionAsInProject().startLevel(2),
                         mavenBundle().groupId("org.apache.felix")
                                      .artifactId("org.apache.felix.configadmin")
                                      .versionAsInProject().startLevel(2),
                         mavenBundle().groupId("org.ops4j.pax.useradmin")
                                      .artifactId("pax-useradmin-service")
                                      .versionAsInProject().startLevel(6));
    }

}

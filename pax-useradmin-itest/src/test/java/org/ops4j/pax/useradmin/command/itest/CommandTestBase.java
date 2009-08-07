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

package org.ops4j.pax.useradmin.command.itest;

import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.dsProfile;

import org.apache.felix.shell.Command;
import org.junit.Assert;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Abstract base class for all command integration tests.
 * 
 * @author Matthias Kuespert
 * @since  06.08.2009
 */
public abstract class CommandTestBase {

    /**
     * Abstract method used to retrieve the <code>BundleContext</code>.
     * 
     * @return <code>BundleContext</code> used throughout the tests.
     */
    protected abstract BundleContext getBundleContext();

    /**
     * @return The basic OSGi framework configuration used to run the tests.
     */
    protected static Option getBasicFrameworkConfiguration() {
        return composite(dsProfile(),
                         mavenBundle().groupId("org.ops4j.pax.logging")
                                      .artifactId("pax-logging-api")
                                      .version("1.3.0").startLevel(1),
                         mavenBundle().groupId("org.ops4j.pax.logging")
                                      .artifactId("pax-logging-service")
                                      .version("1.3.0").startLevel(1),
//                         mavenBundle().groupId("org.ops4j.pax.shell")
//                                      .artifactId("pax-shell-connector-http")
//                                      .version("0.1.0-SNAPSHOT").startLevel(1),
                         mavenBundle().groupId("org.ops4j.pax.shell")
                                      .artifactId("osgi-service-command")
                                      .version("0.1.0-SNAPSHOT").startLevel(1),
                         mavenBundle().groupId("org.apache.felix")
                                      .artifactId("org.apache.felix.prefs")
                                      .version("1.0.2")
                                      .startLevel(1),
                         mavenBundle().groupId("org.apache.felix")
                                      .artifactId("org.apache.felix.shell")
                                      .version("1.0.0")
                                      .startLevel(1),
                         mavenBundle().groupId("org.ops4j.pax.useradmin")
                                      .artifactId("pax-useradmin-service")
                                      .version("0.0.1-SNAPSHOT")
                                      .startLevel(6),
                         mavenBundle().groupId("org.ops4j.pax.useradmin")
                                      .artifactId("pax-useradmin-provider-preferences")
                                      .version("0.0.1-SNAPSHOT")
                                      .startLevel(6),
                         mavenBundle().groupId("org.ops4j.pax.useradmin")
                                      .artifactId("pax-useradmin-command")
                                      .version("0.0.1-SNAPSHOT")
                                      .startLevel(6));
    }

    /**
     * @return The <code>UserAdmin</code> service instance to test.
     */
    protected Command getUserAdminCommand() {
        BundleContext context = getBundleContext();
        try {
            ServiceReference[] refs = context.getServiceReferences(Command.class.getName(), null);
            Assert.assertNotNull("No Command service references found", refs);
            for (ServiceReference ref : refs) {
                Command command = (Command) getBundleContext().getService(ref);
                if (command.getName().equals("userAdmin")) {
                    return command;
                }
            }
        } catch (InvalidSyntaxException e) {
            // ignore
        }
        return null;
    }
}

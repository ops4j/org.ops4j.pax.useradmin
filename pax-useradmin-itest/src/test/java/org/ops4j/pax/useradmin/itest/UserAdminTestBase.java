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
package org.ops4j.pax.useradmin.itest;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;

import org.junit.Assert;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.useradmin.UserAdmin;

/**
 * Abstract base class for all integration tests.
 * 
 * @author Matthias Kuespert
 * @since 12.07.2009
 */
public abstract class UserAdminTestBase {

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
        return composite(logProfile(),
                         mavenBundle().groupId("org.apache.felix")
                                      .artifactId("org.apache.felix.prefs")
                                      .version("1.0.2")
                                      .startLevel(1),
                         mavenBundle().groupId("org.ops4j.pax.useradmin")
                                      .artifactId("pax-useradmin-service")
                                      .version("0.0.1-SNAPSHOT")
                                      .startLevel(5));
    }

    /**
     * @return The <code>UserAdmin</code> service instance to test.
     */
    protected UserAdmin getUserAdmin() {
        BundleContext context = getBundleContext();
        ServiceReference ref = context.getServiceReference(UserAdmin.class.getName());
        Assert.assertNotNull("No UserAdmin service reference found", ref);
        UserAdmin userAdmin = (UserAdmin) context.getService(ref);
        Assert.assertNotNull("No UserAdmin service found", userAdmin);
        return userAdmin;
    }
}

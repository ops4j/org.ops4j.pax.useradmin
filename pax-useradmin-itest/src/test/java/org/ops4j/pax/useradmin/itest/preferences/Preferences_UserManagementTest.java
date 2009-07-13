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

package org.ops4j.pax.useradmin.itest.preferences;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.useradmin.itest.UserManagement;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

/**
 * @author Matthias Kuespert
 * @since  09.07.2009
 */
@RunWith(JUnit4TestRunner.class)
public class Preferences_UserManagementTest extends UserManagement {

    @Inject
    private BundleContext m_context;

    protected BundleContext getBundleContext() {
        return m_context;
    };
    
    @Configuration
    public static Option[] configure() {
        return options(logProfile(),
                       mavenBundle().groupId("org.apache.felix")
                                    .artifactId("org.apache.felix.prefs").version("1.0.2")
                                    .startLevel(1),
                       mavenBundle().groupId("org.ops4j.pax.useradmin")
                                    .artifactId("pax-useradmin-provider-preferences")
                                    .version("0.0.1-SNAPSHOT").startLevel(4),
                       mavenBundle().groupId("org.ops4j.pax.useradmin")
                                    .artifactId("pax-useradmin-service")
                                    .version("0.0.1-SNAPSHOT").startLevel(5)
                       );
    }

//    public static String USER_NAME = "jdeveloper";

    @Test
    public void createAndFindUserOk() {
        super.createAndFindUserOk();
    }

    @Test
    public void createAndFindGroupOk() {
        super.createAndFindGroupOk();
    }

    @Test
    public void createAndRemoveUserOk() {
        super.createAndRemoveUserOk();
    }

    //    @Test
//    public void retrieveUser() {
//        
//        UserAdmin userAdmin = getUserAdmin();
//        User user = (User) userAdmin.getRole(USER_NAME);
//        Assert.assertNotNull("Could not retrieve user", user);
//        Assert.assertEquals("Mismatching user name", user.getName(), USER_NAME);
//    }
}

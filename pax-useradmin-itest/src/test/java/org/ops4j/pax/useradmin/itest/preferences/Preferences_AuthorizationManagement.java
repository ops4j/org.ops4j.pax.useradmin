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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.options.TimeoutOption;
import org.ops4j.pax.useradmin.itest.AuthorizationManagement;
import org.osgi.framework.BundleContext;

/**
 * @author Matthias Kuespert
 * @since  12.07.2009
 */
@RunWith(JUnit4TestRunner.class)
public class Preferences_AuthorizationManagement extends AuthorizationManagement {

    @Inject
    private BundleContext m_context;

    protected BundleContext getBundleContext() {
        return m_context;
    };
    
    @Configuration
    public static Option[] configure() {
        return options(logProfile(),
//                       vmOption("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"),
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

    @Test
    public void hasRole() {
        super.hasRole();
    }
}

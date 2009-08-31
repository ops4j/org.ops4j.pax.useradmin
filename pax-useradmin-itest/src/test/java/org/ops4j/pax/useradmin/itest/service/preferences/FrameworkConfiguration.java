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
package org.ops4j.pax.useradmin.itest.service.preferences;

import static org.ops4j.pax.exam.CoreOptions.*;

import org.ops4j.pax.exam.Option;

/**
 * Provides configuration specific to the Preferences service based variant of
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
        return composite(mavenBundle().groupId("org.apache.felix")
                                      .artifactId("org.apache.felix.prefs")
                                      .version("1.0.2")
                                      .startLevel(1),
                         mavenBundle().groupId("org.ops4j.pax.useradmin")
                                      .artifactId("pax-useradmin-provider-preferences")
                                      .version("0.0.1-SNAPSHOT")
                                      .startLevel(4));
    }
}

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

package org.ops4j.pax.useradmin.service;

/**
 * @author Matthias Kuespert
 * @since 11.08.2009
 */
public interface UserAdminConstants {

    /**
     * The PID used to identify configuration data.
     */
    final static String  SERVICE_PID          = "org.ops4j.pax.useradmin";

    /**
     * The property that must be set by StorageProvider implementations.
     */
    final static String  STORAGEPROVIDER_TYPE = "org.ops4j.pax.useradmin.storageprovider.type";

    /**
     * Property to switch security on/off.
     */
    // final static String  PROP_SECURITY        = "org.ops4j.pax.useradmin.security";

    /**
     * The default security setting.
     */
    // final static boolean DEFAULT_SECURITY     = false;

    /**
     * The prefix used for events sent by the UserAdmin service.
     */
    final static String  EVENT_TOPIC_PREFIX   = "org/osgi/service/useradmin/UserAdmin/";
}

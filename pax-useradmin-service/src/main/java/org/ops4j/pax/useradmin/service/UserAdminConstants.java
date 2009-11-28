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
    final static String SERVICE_PID                          = "org.ops4j.pax.useradmin";

    /**
     * The property that must be set by StorageProvider implementations.
     */
    final static String STORAGEPROVIDER_TYPE                 = "org.ops4j.pax.useradmin.storageprovider.type";

    /**
     * The prefix used for events sent by the UserAdmin service.
     */
    final static String EVENT_TOPIC_PREFIX                   = "org/osgi/service/useradmin/UserAdmin/";

    // property names

    /**
     * Property that controls which encryption algorithm is used. Defaults to ENCRYPTION_ALGORITHM_NONE.
     */
    final static String PROP_ENCRYPTION_ALGORITHM            = "org.ops4j.pax.useradmin.encryption.algorithm";

    /**
     * Property that controls which random number algorithm is used for encryption. Defaults to DEFAULT_ENCRYPTION_RANDOM_ALGORITHM.
     */
    final static String PROP_ENCRYPTION_RANDOM_ALGORITHM     = "org.ops4j.pax.useradmin.encryption.random.algorithm";
    
    
    /**
     * Property that controls which salt-length is used by the random number algorithm. Defaults to DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH.
     */
    final static String PROP_ENCRYPTION_RANDOM_SALTLENGTH    = "org.ops4j.pax.useradmin.encryption.random.saltlength";

    // property defaults

    /**
     * The default algorithm to use for random number generation.
     */
    final static String DEFAULT_ENCRYPTION_RANDOM_ALGORITHM  = "SHA1PRNG";

    /**
     * The default salt length to use by the random number algorithm.
     */
    final static String DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH = "32";

    // special values
    
    final static String ENCRYPTION_ALGORITHM_NONE            = "none";

    /**
     * Property to switch security on/off.
     */
    // final static String PROP_SECURITY = "org.ops4j.pax.useradmin.security";

    /**
     * The default security setting.
     */
    // final static boolean DEFAULT_SECURITY = false;

}

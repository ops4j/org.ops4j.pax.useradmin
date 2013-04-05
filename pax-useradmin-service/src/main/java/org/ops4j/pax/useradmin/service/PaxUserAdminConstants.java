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

package org.ops4j.pax.useradmin.service;

/**
 * @author Matthias Kuespert
 * @since 11.08.2009
 */
public interface PaxUserAdminConstants {

    /**
     * The PID used to identify configuration data.
     */
    final static String SERVICE_PID                                 = "org.ops4j.pax.useradmin";

    /**
     * The property that must be set by StorageProvider implementations.
     */
    final static String STORAGEPROVIDER_TYPE                        = SERVICE_PID + ".storageprovider.type";

    final static String STORAGEPROVIDER_SPI_SERVICE_ID              = SERVICE_PID + ".storageprovider.spi_service_id";

    /**
     * The prefix used for events sent by the UserAdmin service.
     */
    final static String EVENT_TOPIC_PREFIX                          = "org/osgi/service/useradmin/UserAdmin/";

    /**
     * (optional) Property that controls which hash algorithm is used, if not
     * given, an internal default is used
     */
    final static String PROPERTY_ENCRYPTION_HASH_ALGORITHM          = "org.ops4j.pax.useradmin.encryption.hash.algorithm";

    /**
     * (optional) property that controls which random number algorithm is used,
     * if not given, an internal default is used for encryption.
     */
    final static String PROPERTY_ENCRYPTION_SECURERANDOM_ALGORITHM  = "org.ops4j.pax.useradmin.encryption.securerandom.algorithm";

    /**
     * (optional) property that controls which salt-length is used by the
     * random, if not given, an internal default is used number algorithm.
     */
    final static String PROPERTY_ENCRYPTION_SECURERANDOM_SALTLENGTH = "org.ops4j.pax.useradmin.encryption.securerandom.saltlength";

}

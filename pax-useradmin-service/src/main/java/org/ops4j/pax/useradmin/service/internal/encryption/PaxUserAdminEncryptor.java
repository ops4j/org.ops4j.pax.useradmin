/**
 * Copyright 2013 OPS4J
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
package org.ops4j.pax.useradmin.service.internal.encryption;

import org.ops4j.pax.useradmin.service.UserAdminTools;
import org.ops4j.pax.useradmin.service.spi.Encryptor;

/**
 * @author Christoph Läubrich
 */
public class PaxUserAdminEncryptor implements Encryptor {

    private final EncryptorContext context;

    public PaxUserAdminEncryptor(EncryptorContext context) {
        this.context = context;
    }

    @Override
    public PaxUserAdminEncryptedValue encrypt(String key, String value) {
        return new PaxUserAdminEncryptedValue(key, UserAdminTools.stringToBytes(value), true, context);
    }

    @Override
    public PaxUserAdminEncryptedValue encrypt(String key, byte[] value) {
        return new PaxUserAdminEncryptedValue(key, value, false, context);
    }

}

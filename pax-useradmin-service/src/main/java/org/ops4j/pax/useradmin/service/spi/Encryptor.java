/*
 * Copyright 2012 OPS4J
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
package org.ops4j.pax.useradmin.service.spi;

/**
 * The {@link Encryptor} provides methods to encrypt sensitive informations for
 * credentials
 */
public interface Encryptor {

    /**
     * Encrypts a value for a given key, the key might be used for salting
     * and/or verification
     * 
     * @return the encrypted value for the given key and Value
     */
    EncryptedValue encrypt(String key, String value);

    /**
     * Encrypts a value for a given key, the key might be used for salting
     * and/or verification
     * 
     * @return the encrypted value for the given key and Value
     */
    EncryptedValue encrypt(String key, byte[] value);
}

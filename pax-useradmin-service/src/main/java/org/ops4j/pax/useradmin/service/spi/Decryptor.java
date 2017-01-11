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
 * The {@link Decryptor} allows to decryt and/or vaildate informations encrypted
 * with the {@link Encryptor}
 * 
 * @author Christoph Läubrich
 */
public interface Decryptor {

    /**
     * Decrypts a previously encrypted value
     * 
     * @return the encrypted value
     */
    Object decrypt(byte[] encryptedBytes, byte[] verificationBytes, byte[] salt, byte[] algorithmParameter);

    boolean verify(String key, String value, byte[] verificationBytes, byte[] salt, byte[] algorithmParameter);

    boolean verify(String key, byte[] value, byte[] verificationBytes, byte[] salt, byte[] algorithmParameter);
}

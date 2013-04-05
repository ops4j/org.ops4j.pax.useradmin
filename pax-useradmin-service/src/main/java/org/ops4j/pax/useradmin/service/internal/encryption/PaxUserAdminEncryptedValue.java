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
import org.ops4j.pax.useradmin.service.spi.EncryptedValue;

/**
 * Most work is delegated to the {@link EncryptorContext}, this class just
 * caches the values and provides implementation of the {@link EncryptedValue}
 * interface.
 * 
 * @author Christoph Läubrich
 */
public class PaxUserAdminEncryptedValue implements EncryptedValue {

    private final String           key;
    private final byte[]           bytes;
    private final boolean          isString;
    private final EncryptorContext context;
    private byte[]                 algorithmBytes;
    private byte[]                 salt;
    private byte[]                 hash;

    /**
     * @param key
     * @param bytes
     * @param isString
     * @param context
     */
    public PaxUserAdminEncryptedValue(String key, byte[] bytes, boolean isString, EncryptorContext context) {
        this.key = key;
        this.bytes = bytes;
        this.isString = isString;
        this.context = context;
    }

    @Override
    public synchronized byte[] getEncryptedBytes() {
        // TODO We currently do not support encryption of data!
        return null;
    }

    @Override
    public synchronized byte[] getVerificationBytes() {
        if (hash == null) {
            hash = context.hashValues(UserAdminTools.stringToBytes(key), getSalt(), bytes);
        }

        return hash;
    }

    @Override
    public synchronized byte[] getSalt() {
        if (salt == null) {
            salt = context.generateRandomSalt();
        }
        return salt;
    }

    @Override
    public synchronized byte[] getAlgorithmParameter() {
        if (algorithmBytes == null) {
            algorithmBytes = UserAdminTools.stringToBytes(context.toStringValue() + "##" + isString);
        }
        return algorithmBytes;
    }

    /**
     * Unbuffered method to provide a salt value
     * 
     * @param preSalt
     * @return
     */
    public byte[] getVerificationBytes(byte[] preSalt) {
        return context.hashValues(UserAdminTools.stringToBytes(key), preSalt, bytes);
    }
}

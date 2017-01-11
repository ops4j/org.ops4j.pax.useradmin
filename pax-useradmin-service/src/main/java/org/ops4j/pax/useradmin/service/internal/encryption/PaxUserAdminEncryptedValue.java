/*
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

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import org.ops4j.pax.useradmin.service.spi.EncryptedValue;
import org.ops4j.pax.useradmin.service.spi.UserAdminTools;

/**
 * Most work is delegated to the {@link EncryptorContext}, this class just
 * caches the values and provides implementation of the {@link EncryptedValue}
 * interface.
 * 
 * @author Christoph Läubrich
 */
public class PaxUserAdminEncryptedValue implements EncryptedValue {

    /**
     * indicated, that the encrypted value was a byte[]
     */
    private static final byte       BYTE_INDICATOR   = (byte) 'b';

    /**
     * indicates, that the encrypted value was a String
     */
    static final byte              STRING_INDICATOR = (byte) 's';
    private final String           key;
    private final byte[]           bytes;
    private final boolean          isString;
    private final EncryptorContext context;
    private byte[]                 algorithmBytes;
    private byte[]                 salt;
    private byte[]                 hash;
    private byte[]                 encrypted;

    PaxUserAdminEncryptedValue(String key, byte[] bytes, boolean isString, EncryptorContext context) {
        this.key = key;
        this.bytes = bytes;
        this.isString = isString;
        this.context = context;
    }

    @Override
    public synchronized byte[] getEncryptedBytes() {
        if (encrypted == null) {
            Cipher cipher = context.getCipher(Cipher.ENCRYPT_MODE);
            if (cipher == null) {
                return null;
            } else {
                try {
                    byte[] toEncrypt = new byte[bytes.length + 1];
                    System.arraycopy(bytes, 0, toEncrypt, 1, bytes.length);
                    toEncrypt[0] = isString ? STRING_INDICATOR : BYTE_INDICATOR;
                    encrypted = cipher.doFinal(toEncrypt);
                } catch (GeneralSecurityException e) {
                    throw new IllegalStateException("cipher value failed", e);
                }
            }
        }
        return encrypted;
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
            algorithmBytes = UserAdminTools.stringToBytes(context.toStringValue());
        }
        return algorithmBytes;
    }

    /**
     * Unbuffered method to provide a salt value
     */
    byte[] getVerificationBytes(byte[] preSalt) {
        return context.hashValues(UserAdminTools.stringToBytes(key), preSalt, bytes);
    }
}

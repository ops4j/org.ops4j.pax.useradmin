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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.ops4j.pax.useradmin.service.UserAdminTools;
import org.ops4j.pax.useradmin.service.spi.Decryptor;

/**
 * @author Christoph Läubrich
 */
public class PaxUserAdminDecryptor implements Decryptor {

    @Override
    public Object decrypt(byte[] encryptedBytes, byte[] verificationBytes, byte[] salt, byte[] algorithmParameter) {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean verify(String key, String value, byte[] verificationBytes, byte[] salt, byte[] algorithmParameter) {
        PaxUserAdminEncryptedValue encrypt = createEncryptor(algorithmParameter).encrypt(key, value);
        return MessageDigest.isEqual(encrypt.getVerificationBytes(salt), verificationBytes);
    }

    @Override
    public boolean verify(String key, byte[] value, byte[] verificationBytes, byte[] salt, byte[] algorithmParameter) {
        PaxUserAdminEncryptedValue encrypt = createEncryptor(algorithmParameter).encrypt(key, value);
        return MessageDigest.isEqual(encrypt.getVerificationBytes(salt), verificationBytes);
    }

    protected PaxUserAdminEncryptor createEncryptor(byte[] algorithmParameter) {
        String[] param = UserAdminTools.bytesToString(algorithmParameter).split("##");
        EncryptorContext context;
        try {
            context = EncryptorContext.fromParams(param);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("one of the algorithms used to encrypt the value can't be recovered", e);
        }
        return new PaxUserAdminEncryptor(context);
    }

}

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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.ops4j.pax.useradmin.service.spi.Decryptor;
import org.ops4j.pax.useradmin.service.spi.UserAdminTools;

/**
 * @author Christoph Läubrich
 */
public class PaxUserAdminDecryptor implements Decryptor {

    @Override
    public Object decrypt(byte[] encryptedBytes, byte[] verificationBytes, byte[] salt, byte[] algorithmParameter) {
        EncryptorContext context = createContext(algorithmParameter);
        Cipher cipher = context.getCipher(Cipher.DECRYPT_MODE);
        if (cipher == null) {
            throw new UnsupportedOperationException("credential can't be decrypted, it was not stored for retrival");
        }
        try {
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            byte[] finalBytes = new byte[decryptedBytes.length - 1];
            System.arraycopy(decryptedBytes, 1, finalBytes, 0, finalBytes.length);
            boolean isString = decryptedBytes[0] == PaxUserAdminEncryptedValue.STRING_INDICATOR;
            if (isString) {
                return UserAdminTools.bytesToString(finalBytes);
            } else {
                return finalBytes;
            }
        } catch (IllegalBlockSizeException e) {
            throw new IllegalStateException("An illegal blocksize was detected while decrypting the value", e);
        } catch (BadPaddingException e) {
            throw new IllegalStateException("bad padding was detected while decrypting the value", e);
        }

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
        EncryptorContext context = createContext(algorithmParameter);
        return new PaxUserAdminEncryptor(context);
    }

    protected EncryptorContext createContext(byte[] algorithmParameter) {
        String[] param = UserAdminTools.bytesToString(algorithmParameter).split("##");
        EncryptorContext context;
        try {
            context = EncryptorContext.fromParams(param);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("one of the algorithms used to encrypt the value can't be recovered", e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("a number can't be decoded", e);
        } catch (NoSuchPaddingException e) {
            throw new IllegalStateException("one of the padding algorithms used to encrypt the value can't be recovered", e);
        }
        return context;
    }

}

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
package org.ops4j.pax.useradmin.service.internal;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * A class to encrypt and verify values based on the <code>java.security</code>
 * API.
 * 
 * @see <a href=
 *      "http://java.sun.com/javase/6/docs/technotes/guides/security/index.html"
 *      >http://java.sun.com/javase/6/docs/technotes/guides/security/index.html</a>
 * @author Matthias Kuespert
 * @since 25.11.2009
 */
public class EncryptorImpl {

    private MessageDigest m_messageDigest = null;

    private int           m_saltLength    = 0;

    private SecureRandom  m_secureRandom  = null;

    /**
     * Initializing constructor.
     * 
     * @param algorithm
     *            The name of the encryption algorithm to use.
     * @param rngAlgorithm
     *            The random-number generator algorithm to use.
     * @param saltLength
     *            The length of the salt.
     * @throws NoSuchAlgorithmException
     *             Thrown if a specified algorithm is not available.
     */
    protected EncryptorImpl(String algorithm, String rngAlgorithm, String saltLength) throws NoSuchAlgorithmException {
        if (null == algorithm || "".equals(algorithm)) {
            throw new IllegalArgumentException("Error: parameter algorithm must no be null or empty.");
        }
        if (null == rngAlgorithm || "".equals(rngAlgorithm)) {
            throw new IllegalArgumentException("Error: parameter rngAlgorithm must no be null or empty.");
        }
        if (null == saltLength || "".equals(saltLength)) {
            throw new IllegalArgumentException("Error: parameter saltLength must no be null or empty.");
        }
        m_messageDigest = MessageDigest.getInstance(algorithm);
        m_saltLength = new Integer(saltLength);
        m_secureRandom = SecureRandom.getInstance(rngAlgorithm);
        m_secureRandom.setSeed(System.currentTimeMillis());
    }

    /**
     * Creates a new salt for encrypting a value.
     * 
     * @return A byte[] containing the new salt.
     */
    private byte[] createSalt() {
        byte[] salt = new byte[m_saltLength];
        m_secureRandom.nextBytes(salt);
        return salt;
    }

    /**
     * Encrypts a value using the given salt.
     * 
     * @param value
     *            The value to encrypt.
     * @param salt
     *            The salt to use for encryption.
     * @return A byte[] containing salt and encrypted value.
     */
    private byte[] encrypt(byte[] value, byte[] salt) {
        m_messageDigest.reset();
        m_messageDigest.update(salt);
        m_messageDigest.update(value);
        //
        byte[] encryptedValue = m_messageDigest.digest();

        // TODO: check if to encrypt encrypted value over and over again
        // perform iteration safe is to do more than 1000
        //        for (int i = 0; i <= 1001; i++) {
        //            m_messageDigest.reset();
        //            encryptedValue = m_messageDigest.digest(encryptedValue);
        //        }
        //
        // concatenate salt and encrypted value and return that data
        byte[] result = new byte[salt.length + encryptedValue.length];
        System.arraycopy(salt, 0, result, 0, salt.length);
        System.arraycopy(encryptedValue, 0, result, salt.length, encryptedValue.length);
        return result;
    }

    // public interface

    /**
     * Encrypts the given value. Creates a new salt on each call.
     * 
     * @param value
     *            The value to encrypt.
     * @return A byte[] containing salt and encrypted value.
     */
    public byte[] encrypt(byte[] value) {
        if (null == value) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_VALUE);
        }
        byte[] salt = createSalt();
        return encrypt(value, salt);
    }

    /**
     * Checks if the plain input value equals the given encrypted value. Uses
     * the salt stored in the encrypted value to encrypt the input value and
     * compares the result to the data part of the encrypted value.
     * 
     * @param inputValue
     *            The plain-text input value.
     * @param encryptedValue
     *            The encrypted value.
     * @return True if the encrypted input value equals the stored value.
     */
    public boolean compare(byte[] inputValue, byte[] encryptedValue) {
        if (null == inputValue) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_VALUE);
        }
        if (null == encryptedValue) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_VALUE);
        }
        // copy salt from stored value
        byte[] salt = new byte[m_saltLength];
        System.arraycopy(encryptedValue, 0, salt, 0, m_saltLength);
        // compare encrypted values
        byte[] encryptedInputValue = encrypt(inputValue, salt);
        return MessageDigest.isEqual(encryptedInputValue, encryptedValue);
    }
}

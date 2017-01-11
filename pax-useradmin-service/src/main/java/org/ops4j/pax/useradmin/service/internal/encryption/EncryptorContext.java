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
import java.security.SecureRandom;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.NullCipher;

import org.ops4j.pax.useradmin.service.PaxUserAdminConstants;
import org.ops4j.pax.useradmin.service.spi.UserAdminTools;

/**
 * @author Christoph Läubrich
 */
public class EncryptorContext {
    /**
     * 
     */
    private static final String CIPHER_PAX_EMPTY                     = "PAX_EMPTY";

    private static final String CIPHER_PAX_PLAIN                     = "PAX_PLAIN";

    /**
     * The default algorithm to use for random number generation.
     */
    private final static String DEFAULT_ENCRYPTION_RANDOM_ALGORITHM  = "SHA1PRNG";

    private final static String DEFAULT_ENCRYPTION_HASH_ALGORITHM    = "MD5";

    private final static String DEFAULT_ENCRYPTION_CIPHER_ALGORITHM  = CIPHER_PAX_EMPTY;

    /**
     * The default salt length to use by the random number algorithm.
     */
    private final static String DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH = "32";

    private final int           saltLength;

    private final SecureRandom  secureRandom;

    private final MessageDigest messageDigest;

    private final Cipher        cipher;

    public EncryptorContext(Map<String, ?> properties) throws NoSuchAlgorithmException, NumberFormatException, NoSuchPaddingException {
        this(UserAdminTools.getOptionalProperty(properties, PaxUserAdminConstants.PROPERTY_ENCRYPTION_HASH_ALGORITHM, DEFAULT_ENCRYPTION_HASH_ALGORITHM),//
        UserAdminTools.getOptionalProperty(properties, PaxUserAdminConstants.PROPERTY_ENCRYPTION_SECURERANDOM_ALGORITHM, DEFAULT_ENCRYPTION_RANDOM_ALGORITHM), //
        Integer.parseInt(UserAdminTools.getOptionalProperty(properties, PaxUserAdminConstants.PROPERTY_ENCRYPTION_SECURERANDOM_SALTLENGTH, DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH)),//
        UserAdminTools.getOptionalProperty(properties, PaxUserAdminConstants.PROPERTY_ENCRYPTION_CIPHER_ALGORITHM, DEFAULT_ENCRYPTION_CIPHER_ALGORITHM));
    }

    private EncryptorContext(String hashAlgorith, String secureRandomAlgorith, int saltLength, String cipherAlgorithm) throws NoSuchAlgorithmException,
            NoSuchPaddingException {
        this.saltLength = saltLength;
        secureRandom = SecureRandom.getInstance(secureRandomAlgorith);
        messageDigest = MessageDigest.getInstance(hashAlgorith);
        if (CIPHER_PAX_EMPTY.equals(cipherAlgorithm)) {
            cipher = null;
        } else if (CIPHER_PAX_PLAIN.equals(cipherAlgorithm) || "null".equals(cipherAlgorithm)) {
            cipher = new NullCipher();
        } else {
            //Fetch a default one
            cipher = Cipher.getInstance(cipherAlgorithm);
        }
    }

    /**
     * @return the current value of cipher
     */
    Cipher getCipher(int encryptMode) {
        //FIXME: we need to init the cypher!
        //This depend on the choosen algorithm...
        //        SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
        //        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        //        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        return cipher;
    }

    /**
     * @return a String representation of this {@link EncryptorContext}
     */
    String toStringValue() {
        StringBuilder sb = new StringBuilder();
        sb.append(messageDigest.getAlgorithm());
        sb.append("##");
        sb.append(secureRandom.getAlgorithm());
        sb.append("##");
        sb.append(saltLength);
        sb.append("##");
        sb.append(cipher == null ? CIPHER_PAX_EMPTY : ((cipher instanceof NullCipher) ? CIPHER_PAX_PLAIN : cipher.getAlgorithm()));
        return sb.toString();
    }

    synchronized byte[] generateRandomSalt() {
        byte[] bytes = new byte[saltLength];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    synchronized byte[] hashValues(byte[]... byteArray) {
        messageDigest.reset();
        for (byte[] bs : byteArray) {
            messageDigest.update(bs);
        }
        return messageDigest.digest();
    }

    static EncryptorContext fromParams(String[] params) throws NumberFormatException, NoSuchAlgorithmException, NoSuchPaddingException {
        return new EncryptorContext(params[0], params[1], Integer.parseInt(params[2]), params[3]);
    }

}

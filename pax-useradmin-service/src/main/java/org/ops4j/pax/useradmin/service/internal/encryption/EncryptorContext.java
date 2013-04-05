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
import java.security.SecureRandom;
import java.util.Map;

import org.ops4j.pax.useradmin.service.PaxUserAdminConstants;
import org.ops4j.pax.useradmin.service.UserAdminTools;

/**
 * @author Christoph Läubrich
 */
public class EncryptorContext {
    /**
     * The default algorithm to use for random number generation.
     */
    final static String         DEFAULT_ENCRYPTION_RANDOM_ALGORITHM  = "SHA1PRNG";

    final static String         DEFAULT_ENCRYPTION_HASH_ALGORITHM    = "MD5";

    /**
     * The default salt length to use by the random number algorithm.
     */
    final static String         DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH = "32";

    private final int           saltLength;

    private final SecureRandom  secureRandom;

    private final MessageDigest messageDigest;

    /**
     * @throws NoSuchAlgorithmException
     */
    public EncryptorContext(Map<String, ?> properties) throws NoSuchAlgorithmException {
        this(UserAdminTools.getOptionalProperty(properties, PaxUserAdminConstants.PROPERTY_ENCRYPTION_HASH_ALGORITHM, DEFAULT_ENCRYPTION_HASH_ALGORITHM),//
        UserAdminTools.getOptionalProperty(properties, PaxUserAdminConstants.PROPERTY_ENCRYPTION_SECURERANDOM_ALGORITHM, DEFAULT_ENCRYPTION_RANDOM_ALGORITHM), //
        Integer.parseInt(UserAdminTools.getOptionalProperty(properties, PaxUserAdminConstants.PROPERTY_ENCRYPTION_SECURERANDOM_SALTLENGTH, DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH)));
    }

    public EncryptorContext(String hashAlgorith, String secureRandomAlgorith, int saltLength) throws NoSuchAlgorithmException {
        this.saltLength = saltLength;
        secureRandom = SecureRandom.getInstance(secureRandomAlgorith);
        messageDigest = MessageDigest.getInstance(hashAlgorith);
    }

    /**
     * @return a String representation of this {@link EncryptorContext}
     */
    public String toStringValue() {
        return messageDigest.getAlgorithm() + "##" + secureRandom.getAlgorithm() + "##" + saltLength;
    }

    /**
     * @return
     */
    public synchronized byte[] generateRandomSalt() {
        byte[] bytes = new byte[saltLength];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    /**
     * @param byteArray
     * @return
     */
    public synchronized byte[] hashValues(byte[]... byteArray) {
        messageDigest.reset();
        for (byte[] bs : byteArray) {
            messageDigest.update(bs);
        }
        return messageDigest.digest();
    }

    public static EncryptorContext fromParams(String[] params) throws NumberFormatException, NoSuchAlgorithmException {
        return new EncryptorContext(params[0], params[1], Integer.parseInt(params[2]));
    }
}

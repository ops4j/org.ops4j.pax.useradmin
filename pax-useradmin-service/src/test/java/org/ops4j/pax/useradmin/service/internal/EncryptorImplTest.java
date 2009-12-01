/*
 * Copyright 2009 Matthias Kuespert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.useradmin.service.internal;

import java.security.NoSuchAlgorithmException;

import org.junit.Assert;
import org.junit.Test;
import org.ops4j.pax.useradmin.service.UserAdminConstants;

/**
 * @author Matthias Kuespert
 * @since  28.11.2009
 */
public class EncryptorImplTest {

    // tested algorithms
    //
    private static final String ENCRYPTION_ALGORITHM_SHA = "SHA";
    private static final String ENCRYPTION_ALGORITHM_MD5 = "MD5";
    
    // the value used for testing encryption
    //
    private static String VALUE = "someValue";
    
    // testing Encryptor creation

    @Test (expected = NoSuchAlgorithmException.class)
    public void createEncryptorNoSuchEncryptionAlgorithm() throws Exception {
        new EncryptorImpl(UserAdminConstants.ENCRYPTION_ALGORITHM_NONE,
                          UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                          UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
    }
    
    @Test (expected = NoSuchAlgorithmException.class)
    public void createEncryptorNoSuchRandomAlgorithm() throws Exception {
        new EncryptorImpl(ENCRYPTION_ALGORITHM_MD5,
                          "noSuchAlgorithm",
                          UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void createEncryptorNullAlgorithm() throws Exception {
        new EncryptorImpl(null,
                          UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                          UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void createEncryptorEmptyAlgorithm() throws Exception {
        new EncryptorImpl("",
                          UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                          UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
    }

    @Test (expected = IllegalArgumentException.class)
    public void createEncryptorNullRngAlgorithm() throws Exception {
        new EncryptorImpl(UserAdminConstants.ENCRYPTION_ALGORITHM_NONE,
                          null,
                          UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
    }

    @Test (expected = IllegalArgumentException.class)
    public void createEncryptorEmptyRngAlgorithm() throws Exception {
        new EncryptorImpl(UserAdminConstants.ENCRYPTION_ALGORITHM_NONE,
                          "",
                          UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
    }

    @Test (expected = IllegalArgumentException.class)
    public void createEncryptorNullSaltLength() throws Exception {
        new EncryptorImpl(UserAdminConstants.ENCRYPTION_ALGORITHM_NONE,
                          UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                          null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void createEncryptorEmptySaltLength() throws Exception {
        new EncryptorImpl(UserAdminConstants.ENCRYPTION_ALGORITHM_NONE,
                          UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                          "");
    }

    @Test (expected = NumberFormatException.class)
    public void createEncryptorMD5DefaultRngNoIntSaltLength() throws Exception {
        new EncryptorImpl(ENCRYPTION_ALGORITHM_MD5,
                          UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                          "abc");
    }

    @Test
    public void createEncryptorOkMD5DefaultRngDefaultSaltLength() {
        try {
            EncryptorImpl encryptor = new EncryptorImpl(ENCRYPTION_ALGORITHM_MD5,
                                                        UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                                                        UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
            Assert.assertNotNull("No encryptor created.", encryptor);
        } catch (NoSuchAlgorithmException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
    }
    
    @Test
    public void createEncryptorOkSHADefaultRngDefaultSaltLength() {
        try {
            EncryptorImpl encryptor = new EncryptorImpl(ENCRYPTION_ALGORITHM_SHA,
                                                        UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                                                        UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
            Assert.assertNotNull("No encryptor created.", encryptor);
        } catch (NoSuchAlgorithmException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
    }

    // testing Encryptor.encrypt()

    @Test (expected = IllegalArgumentException.class)
    public void encryptNullValue() throws Exception {
        EncryptorImpl encryptor = new EncryptorImpl(ENCRYPTION_ALGORITHM_MD5,
                                                    UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                                                    UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
        encryptor.encrypt(null);
    }

    private byte[] encrypt(String algorithm,
                           String rngAlgorithm,
                           String saltLength) throws Exception {
        EncryptorImpl encryptor = new EncryptorImpl(algorithm,
                                                    rngAlgorithm,
                                                    saltLength);
        byte[] encryptedValue = encryptor.encrypt(VALUE.getBytes());
        Assert.assertNotNull("Encrypted value is null", encryptedValue);
        Assert.assertTrue("Encrypted value is too short", encryptedValue.length > 0);
        return encryptedValue;
    }

    @Test
    public void encryptOk() throws Exception {
        encrypt(ENCRYPTION_ALGORITHM_MD5,
                  UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                  UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
        encrypt(ENCRYPTION_ALGORITHM_MD5,
                  UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                  UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
    }

    // testing Encryptor.compare()
    
    @Test (expected = IllegalArgumentException.class)
    public void compareInvalidInput() throws Exception {
        compare(ENCRYPTION_ALGORITHM_MD5,
                UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH,
                null,
                null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void compareInvalidStored() throws Exception {
        compare(ENCRYPTION_ALGORITHM_MD5,
                UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH,
                VALUE,
                null);
    }

    private void compare(String algorithm,
                         String rngAlgorithm,
                         String saltLength,
                         String value,
                         byte[] encryptedValue) throws Exception {
        EncryptorImpl encryptor = new EncryptorImpl(algorithm,
                                                    rngAlgorithm,
                                                    saltLength);
        boolean result = encryptor.compare(value == null ? null : value.getBytes(), encryptedValue);
        Assert.assertTrue("Comparison failed", result);
    }
    
    @Test
    public void compareOk() throws Exception {
        compare(ENCRYPTION_ALGORITHM_MD5,
                UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH,
                VALUE,
                encrypt(ENCRYPTION_ALGORITHM_MD5,
                        UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                        UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH));
        compare(ENCRYPTION_ALGORITHM_SHA,
                UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH,
                VALUE,
                encrypt(ENCRYPTION_ALGORITHM_SHA,
                        UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                        UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH));
    }
}

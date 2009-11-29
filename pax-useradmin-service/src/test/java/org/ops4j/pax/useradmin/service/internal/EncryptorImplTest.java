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

import junit.framework.Assert;

import org.junit.Test;
import org.ops4j.pax.useradmin.service.UserAdminConstants;

/**
 * @author Matthias Kuespert
 * @since  28.11.2009
 */
public class EncryptorImplTest {

    private static String VALUE = "someValue";
    
    @Test (expected = NoSuchAlgorithmException.class)
    public void createEncryptorNoSuchEncryptionAlgorithm() throws Exception {
        new EncryptorImpl(UserAdminConstants.ENCRYPTION_ALGORITHM_NONE,
                          UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                          UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
    }
    
    @Test (expected = NoSuchAlgorithmException.class)
    public void createEncryptorNoSuchRandomAlgorithm() throws Exception {
        new EncryptorImpl("MD5",
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
    public void createEncryptorNoIntSaltLength() throws Exception {
        new EncryptorImpl("MD5",
                          UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                          "abc");
    }

    @Test
    public void createEncryptorMD5Ok() {
        try {
            EncryptorImpl encryptor = new EncryptorImpl("MD5",
                                                        UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                                                        UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
            Assert.assertNotNull("No encryptor created.", encryptor);
        } catch (NoSuchAlgorithmException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void encryptValueNullTest() throws Exception {
        EncryptorImpl encryptor = new EncryptorImpl("MD5",
                                                    UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                                                    UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
        encryptor.encrypt(null);
    }

    @Test
    public void encryptOkTest() throws Exception {
        EncryptorImpl encryptor = new EncryptorImpl("MD5",
                                                    UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                                                    UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
        byte[] encryptedValue = encryptor.encrypt(VALUE.getBytes());
        Assert.assertNotNull("Enrypted value is null", encryptedValue);
    }

    @Test (expected = IllegalArgumentException.class)
    public void compareInvalidInputTest() throws Exception {
        EncryptorImpl encryptor = new EncryptorImpl("MD5",
                                                    UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                                                    UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
        byte[] encryptedValue = encryptor.encrypt(VALUE.getBytes());
        Assert.assertNotNull("Enrypted value is null", encryptedValue);
        encryptor.compare(null, encryptedValue);
    }

    @Test (expected = IllegalArgumentException.class)
    public void compareInvalidStoredTest() throws Exception {
        EncryptorImpl encryptor = new EncryptorImpl("MD5",
                                                    UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                                                    UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
        encryptor.compare(VALUE.getBytes(), null);
    }

    @Test
    public void compareFailedTest() {
        // TODO: implement test
    }

    @Test
    public void compareOkTest() throws Exception {
        EncryptorImpl encryptor = new EncryptorImpl("MD5",
                                                    UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                                                    UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
        byte[] encryptedValue = encryptor.encrypt(VALUE.getBytes());
        Assert.assertNotNull("Enrypted value is null", encryptedValue);
        boolean result = encryptor.compare(VALUE.getBytes(), encryptedValue);
        Assert.assertTrue("Comparison failed", result);
    }
}

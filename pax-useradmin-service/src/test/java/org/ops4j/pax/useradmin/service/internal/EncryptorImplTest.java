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

import org.junit.Test;
import org.ops4j.pax.useradmin.service.UserAdminConstants;

/**
 * @author Matthias Kuespert
 * @since  28.11.2009
 */
public class EncryptorImplTest {

    @Test (expected = NoSuchAlgorithmException.class)
    public void createEncryptorNoSuchEncryptorAlgorithm() {
        try {
            new EncryptorImpl(UserAdminConstants.ENCRYPTION_ALGORITHM_NONE,
                              UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                              UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
        } catch (NoSuchAlgorithmException e) {
        }
    }
    
    @Test (expected = NoSuchAlgorithmException.class)
    public void createEncryptorNoSuchRandomAlgorithm() {
        try {
            new EncryptorImpl("MD5",
                              "noSuchAlgorithm",
                              UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
        } catch (NoSuchAlgorithmException e) {
        }
    }
    
    @Test
    public void createEncryptorMD5Ok() {
        try {
            new EncryptorImpl("MD5",
                              UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM,
                              UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Must not happen here.");
        }
    }
    
    @Test
    public void createEncryptorNoSuchEncrytionAlgorithm() {
    }
        
    @Test
    public void encryptParameterNullTest() {
        // TODO: implement test
    }

    @Test
    public void encryptOkTest() {
        // TODO: implement test
    }

    @Test
    public void compareOkTest() {
        // TODO: implement test
    }

    @Test
    public void compareInvalidInputTest() {
        // TODO: implement test
    }

    @Test
    public void compareInvalidStoredTest() {
        // TODO: implement test
    }

    @Test
    public void compareFailedTest() {
        // TODO: implement test
    }
}

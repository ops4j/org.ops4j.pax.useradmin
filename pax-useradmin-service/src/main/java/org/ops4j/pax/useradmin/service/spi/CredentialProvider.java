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
package org.ops4j.pax.useradmin.service.spi;

import org.osgi.service.useradmin.User;

/**
 * Provides access to user credential attributes, all methods here are called
 * after several checks are made:
 * <ul>
 * <li>The caller is allowed to access the credential</li>
 * <li>The key is a String</li>
 * <li>The value is either a String or a byte[]</li>
 * </ul>
 */
public interface CredentialProvider {

    /**
     * gets a <code>String</code> credential to a user.
     * 
     * @param decryptor
     *            The {@link Decryptor} to validate and decrypt values
     * @param user
     *            The <code>User</code> to get the credential from.
     * @param key
     *            The key of the credential.
     * @throws StorageException
     */
    Object getUserCredential(Decryptor decryptor, User user, String key) throws StorageException;

    /**
     * Check a <code>String</code> credential to a user.
     * @param decryptor
     *            The {@link Decryptor} to validate and decrypt values
     * @param user
     *            The {@link User} to check the credential to.
     * @param key
     *            The key of the credential.
     * @param value
     *            The value of the credential.
     * 
     * @throws StorageException
     */
    boolean hasUserCredential(Decryptor decryptor, User user, String key, Object value) throws StorageException;

    /**
     * Sets a <code>String</code> credential to a user.
     * 
     * @param encryptor
     *            the {@link Encryptor} to use for encrypt sensitive values
     * @param user
     *            The <code>User</code> to set the credential to.
     * @param key
     *            The key of the credential.
     * @param value
     *            The value of the credential.
     * @throws StorageException
     */
    void setUserCredential(Encryptor encryptor, User user, String key, Object value) throws StorageException;

    /**
     * Removes a credential from a role.
     * 
     * @param user
     *            The <code>User</code> to remove the credential from.
     * @param key
     *            The key of the credential.
     * @throws StorageException
     */
    void removeUserCredential(User user, String key) throws StorageException;

    /**
     * Removes all credentials for a user.
     * 
     * @param user
     *            The <code>User</code> to remove the credentials for.
     * @throws StorageException
     */
    void clearUserCredentials(User user) throws StorageException;
}

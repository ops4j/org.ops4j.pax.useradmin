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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdminPermission;

/**
 * A dictionary to manage user credentials and communicate changes.
 * 
 * @author Matthias Kuespert
 * @since 02.07.2009
 */
public class UserCredentials extends AbstractProperties<User> {

    private static final long serialVersionUID = 1L;
    private final Set<String> credentialKeys;

    /**
     * Initializing constructor.
     * 
     * @param initialCredentialKeys
     * @see AbstractProperties#AbstractProperties(Role, UserAdminUtil, Map)
     */
    protected UserCredentials(User user, UserAdminUtil util, Set<String> initialCredentialKeys) {
        super(user, util, null);
        if (initialCredentialKeys != null) {
            this.credentialKeys = new HashSet<String>(initialCredentialKeys);
        } else {
            this.credentialKeys = new HashSet<String>();
        }
    }

    @Override
    protected void checkGetPermission(String key) {
        getUtil().checkPermission(key, UserAdminPermission.GET_CREDENTIAL);
    }

    @Override
    protected synchronized Object store(StorageProvider storageProvider, String key, Object plainValue) throws StorageException {
        UserAdminUtil util = getUtil();
        util.checkPermission(key, UserAdminPermission.CHANGE_CREDENTIAL);
        storageProvider.getCredentialProvider().setUserCredential(util.getEncryptor(), getRole(), key, plainValue);
        credentialKeys.add(key);
        return plainValue;
    }

    @Override
    protected synchronized void remove(StorageProvider storageProvider, String key) throws StorageException {
        getUtil().checkPermission(key, UserAdminPermission.CHANGE_CREDENTIAL);
        storageProvider.getCredentialProvider().removeUserCredential(getRole(), key);
        credentialKeys.remove(key);
    }

    @Override
    public synchronized Object get(Object key) {
        //call super for security and param checks...
        super.get(key);
        UserAdminUtil util = getUtil();
        return util.getStorageProvider().getCredentialProvider().getUserCredential(util.getDecryptor(), getRole(), (String) key);
    }

    @Override
    protected Object putInternal(String key, Object storedValue, Object oldValue) {
        return oldValue;
    }

    @Override
    public Enumeration<String> keys() {
        return Collections.enumeration(credentialKeys);
    }

    @Override
    public boolean isEmpty() {
        return credentialKeys.isEmpty();
    }

    @Override
    public int size() {
        return credentialKeys.size();
    }

    @Override
    public Enumeration<Object> elements() {
        ArrayList<Object> list = new ArrayList<Object>();
        String[] keys = credentialKeys.toArray(new String[0]);
        for (String key : keys) {
            list.add(get(key));
        }
        return Collections.enumeration(list);
    }

    /**
     * @param key
     * @param value
     * @return
     */
    public boolean hasCredential(String key, Object value) {
        checkKeyValid(key);
        checkGetPermission(key);
        UserAdminUtil util = getUtil();
        return util.getStorageProvider().getCredentialProvider().hasUserCredential(util.getDecryptor(), getRole(), key, value);
    }
}

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

import java.util.Map;

import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdminPermission;

/**
 * A dictionary to manage credentials and communicate changes.
 * 
 * @author Matthias Kuespert
 * @since  02.07.2009
 */
public class UserCredentials extends AbstractProperties {

    private static final long serialVersionUID = 1L;

    public UserCredentials(User user, UserAdminUtil util, Map<String, String> properties) {
        super(user, util, properties);
    }

    private User getUser() {
        return (User) getRole();
    }

    @Override
    protected void store(StorageProvider storageProvider, String key, String value)
        throws StorageException {
        getUtil().checkPermission(key, UserAdminPermission.CHANGE_CREDENTIAL);
        storageProvider.setUserCredential(getUser(), key, value);
    }

    @Override
    protected void store(StorageProvider storageProvider, String key, byte[] value)
        throws StorageException {
        getUtil().checkPermission(key, UserAdminPermission.CHANGE_CREDENTIAL);
        storageProvider.setUserCredential(getUser(), key, value);
    }

    @Override
    protected void remove(StorageProvider storageProvider, String key) throws StorageException {
        getUtil().checkPermission(key, UserAdminPermission.CHANGE_CREDENTIAL);
        storageProvider.removeUserCredential(getUser(), key);
    }

    @Override
    protected void clear(StorageProvider storageProvider) throws StorageException {
        for (Object key : keySet()) {
            getUtil().checkPermission((String) key, UserAdminPermission.CHANGE_CREDENTIAL);
        }
        storageProvider.clearUserCredentials(getUser());
    }
    
    @Override
    protected void checkGetPermission(String key) {
        getUtil().checkPermission((String) key, UserAdminPermission.GET_CREDENTIAL);
    }
}

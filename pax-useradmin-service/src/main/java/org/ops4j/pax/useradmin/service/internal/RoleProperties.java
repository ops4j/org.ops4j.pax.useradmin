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

import java.util.Map;

import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.UserAdminPermission;

/**
 * A dictionary to manage role properties and communicate changes.
 * 
 * @author Matthias Kuespert
 * @since 02.07.2009
 */
public class RoleProperties extends AbstractProperties<Role> {

    private static final long serialVersionUID = 1L;

    /**
     * Initializing constructor.
     * 
     * @see AbstractProperties#AbstractProperties(Role, UserAdminUtil, Map)
     */
    protected RoleProperties(Role role, UserAdminUtil util, Map<String, Object> properties) {
        super(role, util, properties);
    }

    /**
     * @see AbstractProperties#store(StorageProvider, String, Object)
     */
    @Override
    protected Object store(StorageProvider storageProvider, String key, Object value) throws StorageException {
        getUtil().checkPermission(key, UserAdminPermission.CHANGE_PROPERTY);
        storageProvider.setRoleAttribute(getRole(), key, value);
        return value;
    }

    /**
     * @see AbstractProperties#remove(StorageProvider, String)
     */
    @Override
    protected void remove(StorageProvider storageProvider, String key) throws StorageException {
        getUtil().checkPermission(key, UserAdminPermission.CHANGE_PROPERTY);
        storageProvider.removeRoleAttribute(getRole(), key);
    }

    @Override
    protected void checkGetPermission(String key) {
        //Nothing to do
    }

}

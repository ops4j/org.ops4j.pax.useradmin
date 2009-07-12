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

import java.util.Dictionary;
import java.util.Map;

import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/**
 * Implementation of the User interface.
 * 
 * @author Matthias Kuespert
 * @since  02.07.2009
 */
public class UserImpl extends RoleImpl implements User {

    private UserCredentials m_credentials = null;

    protected UserImpl(String name,
                       UserAdminImpl admin,
                       Map<String, String> properties,
                       Map<String, String> credentials) {
        super(name, admin, properties);
        //
        m_credentials = new UserCredentials(this, admin, credentials);
    }

    @SuppressWarnings(value = "unchecked")
    public Dictionary getCredentials() {
        return m_credentials;
    }

    public boolean hasCredential(String key, Object value) {
        getAdmin().checkAdminPermission();
        if (null != key && null != value && (value instanceof String || value instanceof byte[])) {
            for (Object credential : m_credentials.keySet()) {
                if (((String) credential).equals(key)) {
                    // check this credential
                    Object credentialValue = m_credentials.get(key);
                    return null != credentialValue && credentialValue.equals(value);
                }
            }
        }
        return false;
    }

    public int getType() {
        return Role.USER;
    }
}

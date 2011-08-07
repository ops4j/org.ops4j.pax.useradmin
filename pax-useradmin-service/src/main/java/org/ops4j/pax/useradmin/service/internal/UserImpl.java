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

import java.util.Collection;
import java.util.Dictionary;
import java.util.Map;

import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/**
 * Implementation of the <code>User</code> interface.
 *
 * @see <a href="http://www.osgi.org/javadoc/r4v42/org/osgi/service/useradmin/User.html" />
 *
 * @author Matthias Kuespert
 * @since  02.07.2009
 */
public class UserImpl extends RoleImpl implements User {

    /**
     * The credentials stored for this user.
     */
    private UserCredentials m_credentials = null;

    /**
     * Constructor.
     *
     * @see RoleImpl#RoleImpl(String, UserAdminImpl, Map)
     *
     * @param credentials The credentials of this user.
     */
    protected UserImpl(String name,
                       UserAdminImpl admin,
                       Map<String, Object> properties,
                       Map<String, Object> credentials) {
        super(name, admin, properties);
        //
        m_credentials = new UserCredentials(this, admin, credentials);
    }

    /**
     * @see User#getCredentials()
     */
    @SuppressWarnings(value = "unchecked")
    public Dictionary getCredentials() {
        return m_credentials;
    }

    /**
     * @see User#hasCredential(String, Object)
     */
    public boolean hasCredential(String key, Object value) {
        if (null == key) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY);
        }
        if ("".equals(key)) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_EMPTY_KEY);
        }
        if (null == value) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_VALUE);
        }
        if (value instanceof String || value instanceof byte[]) {
            getAdmin().checkAdminPermission();
            for (Object credentialKey : m_credentials.keySet()) {
                if (credentialKey.equals(key)) {
                    // check this credential
                    byte[] credentialValue = (byte[]) m_credentials.get(key);
                    return    null != credentialValue
                           && getAdmin().compareToEncryptedValue(value, credentialValue);
                }
            }
        }
        return false;
    }

    /**
     * @see User#getType()
     */
    public int getType() {
        return Role.USER;
    }

    /**
     * Checks if this user is implied by the given role. Users are only implied
     * by themselves.
     *
     * @param role The role to check.
     * @param checkedRoles Used for loop detection.
     * @return True if this role is implied by the given one, false otherwise.
     */
    protected ImplicationResult isImpliedBy(Role role, Collection<String> checkedRoles) {
        if (checkedRoles.contains(getName())) {
            return ImplicationResult.IMPLIEDBY_LOOPDETECTED;
        }
        checkedRoles.add(getName());
        return    getName().equals(Role.USER_ANYONE)
               || getName().equals(role.getName())   ? ImplicationResult.IMPLIEDBY_YES : ImplicationResult.IMPLIEDBY_NO;
        // TODO check if we need that: || Role.USER_ANYONE.equals(role.getName());
    }
}

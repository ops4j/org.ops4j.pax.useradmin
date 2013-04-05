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
import java.util.Set;

import org.ops4j.pax.useradmin.service.spi.SPIRole;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/**
 * Implementation of the <code>User</code> interface.
 * 
 * @see <a
 *      href="http://www.osgi.org/javadoc/r4v42/org/osgi/service/useradmin/User.html">http://www.osgi.org/javadoc/r4v42/org/osgi/service/useradmin/User.html</a>
 * @author Matthias Kuespert
 * @since 02.07.2009
 */
public class UserImpl extends RoleImpl implements User {

    /**
     * The credentials stored for this user.
     */
    private UserCredentials m_credentials = null;

    /**
     * Constructor.
     * 
     * @param initialCredentialKeys
     * @see RoleImpl#RoleImpl(String, PaxUserAdmin, Map)
     * @param credentialKeys
     *            The credentials of this user.
     */
    protected UserImpl(String name, PaxUserAdmin admin, Map<String, Object> properties, Set<String> initialCredentialKeys) {
        super(name, admin, properties);
        m_credentials = new UserCredentials(this, admin, initialCredentialKeys);
    }

    /**
     * @see User#getCredentials()
     */
    @Override
    public Dictionary<String, Object> getCredentials() {
        return m_credentials;
    }

    /**
     * @see User#hasCredential(String, Object)
     */
    @Override
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
            return m_credentials.hasCredential(key, value);
        }
        return false;
    }

    /**
     * @see User#getType()
     */
    @Override
    public int getType() {
        return Role.USER;
    }

    /**
     * Checks if this user is implied by the given role. Users are only implied
     * by themselves.
     * 
     * @param role
     *            The role to check.
     * @param checkedRoles
     *            Used for loop detection.
     * @return True if this role is implied by the given one, false otherwise.
     */
    @Override
    public ImplicationResult isImpliedBy(SPIRole role, Collection<String> checkedRoles) {
        if (checkedRoles.contains(getName())) {
            return ImplicationResult.IMPLIEDBY_LOOPDETECTED;
        }
        checkedRoles.add(getName());
        return getName().equals(Role.USER_ANYONE) || getName().equals(role.getName()) ? ImplicationResult.IMPLIEDBY_YES : ImplicationResult.IMPLIEDBY_NO;
        // TODO check if we need that: || Role.USER_ANYONE.equals(role.getName());
    }

    @Override
    public String toString() {
        return "User-" + getName();
    }
}

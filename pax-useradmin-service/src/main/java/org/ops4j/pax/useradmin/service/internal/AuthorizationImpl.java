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

import java.util.ArrayList;
import java.util.Collection;

import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/**
 * Implementation of the Authorization interface.
 * 
 * @author Matthias Kuespert
 * @since 02.07.2009
 */
public class AuthorizationImpl implements Authorization {

    private User          m_user      = null;

    private UserAdminImpl m_userAdmin = null;

    protected AuthorizationImpl(UserAdminImpl userAdmin, User user) throws StorageException {
        m_userAdmin = userAdmin;
        m_user = user;
        if (null == m_user) {
            m_user = (User) m_userAdmin.getRole(Role.USER_ANYONE);
            if (null == m_user) {
                throw new StorageException("Anonymous user '" + Role.USER_ANYONE + "' does not exist");
            }
        }
    }

    public String getName() {
        return null != m_user ? m_user.getName() : Role.USER_ANYONE;
    }

    public String[] getRoles() {
        Collection<String> roleNames = new ArrayList<String>();
        try {
            for (Role role : m_userAdmin.getRoles(null)) {
                if (((RoleImpl) role).isImpliedBy(m_user, new ArrayList<String>())) {
                    String name = role.getName();
                    if (!Role.USER_ANYONE.equals(name)) {
                        roleNames.add(name);
                    }
                }
            }
            if (!roleNames.isEmpty()) {
                return roleNames.toArray(new String[0]);
            }
        } catch (InvalidSyntaxException e) {
            // will never be reached b/o checks in getRoles()
        }
        return null;
    }

    public boolean hasRole(String name) {
        RoleImpl roleToCheck = (RoleImpl) m_userAdmin.getRole(name);
        if (null != roleToCheck) {
            return roleToCheck.isImpliedBy(m_user, new ArrayList<String>());
        }
        return false;
    }
}

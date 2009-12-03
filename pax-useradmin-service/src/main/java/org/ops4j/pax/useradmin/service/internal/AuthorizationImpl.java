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

import org.ops4j.pax.useradmin.service.internal.RoleImpl.ImplicationResult;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/**
 * Implementation of the Authorization interface.
 * 
 * @see http://www.osgi.org/javadoc/r4v42/org/osgi/service/useradmin/Authorization.html
 * 
 * @author Matthias Kuespert
 * @since 02.07.2009
 */
public class AuthorizationImpl implements Authorization {

    /**
     * The user <code>Role</code> we are managing.
     */
    private User          m_user      = null;

    /**
     * The <code>UserAdmin</code> service used.
     */
    private UserAdminImpl m_userAdmin = null;

    /**
     * Initializing constructor.
     * 
     * @param userAdmin The <code>UserAdmin</code> service to use authorization.
     * @param user The <code>User</code> instance whose authorization is managed.
     */
    protected AuthorizationImpl(UserAdminImpl userAdmin, User user) {
        m_userAdmin = userAdmin;
        m_user = user;
    }

    /**
     * @see Authorization#getName()
     */
    public String getName() {
        return null != m_user ? m_user.getName() : null;
    }

    /**
     * @see Authorization#getRoles()
     */
    public String[] getRoles() {
        Collection<String> roleNames = new ArrayList<String>();
        try {
            Role[] roles = m_userAdmin.getRoles(null);
            if (null != roles) {
                for (Role role : roles) {
                    if (!Role.USER_ANYONE.equals(role.getName())) {
                        ImplicationResult result = ((RoleImpl) role).isImpliedBy(m_user,
                                                                                 new ArrayList<String>());
                        if (ImplicationResult.IMPLIEDBY_YES == result) {
                            String name = role.getName();
                            roleNames.add(name);
                        }
                    }
                }
            }
            if (!roleNames.isEmpty()) {
                return roleNames.toArray(new String[0]);
            }
        } catch (InvalidSyntaxException e) {
            // will never be reached because UserAdmin.getRoles() allows null filters
            throw new IllegalStateException(  "Unexpected InvalidSyntaxException caught while using null filter: "
                                            + e.getMessage() + " for filter: " + e.getFilter(), e);
        }
        return null;
    }

    /**
     * @see Authorization#hasRole(String)
     */
    public boolean hasRole(String name) {
//        String[] roles = getRoles();
//        if (null != roles) {
//            for (String role : roles) {
//                if (role.equals(name)) {
//                    return true;
//                }
//            }
//        }
//        return false;
        RoleImpl roleToCheck = (RoleImpl) m_userAdmin.getRole(name);
        if (null != roleToCheck) {
            return ImplicationResult.IMPLIEDBY_YES == roleToCheck.isImpliedBy(m_user, new ArrayList<String>());
        }
        return false;
    }
}

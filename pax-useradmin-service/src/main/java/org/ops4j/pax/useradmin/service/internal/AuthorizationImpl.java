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
import java.util.Collection;

import org.ops4j.pax.useradmin.service.spi.SPIRole;
import org.ops4j.pax.useradmin.service.spi.SPIRole.ImplicationResult;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.log.LogService;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/**
 * Implementation of the Authorization interface.
 * 
 * @see <a
 *      href="http://www.osgi.org/javadoc/r4v42/org/osgi/service/useradmin/Authorization.html">http://www.osgi.org/javadoc/r4v42/org/osgi/service/useradmin/Authorization.html</a>
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
    private PaxUserAdmin m_userAdmin = null;

    /**
     * Initializing constructor.
     * 
     * @param userAdmin
     *            The <code>UserAdmin</code> service to use authorization.
     * @param user
     *            The <code>User</code> instance whose authorization is managed.
     */
    protected AuthorizationImpl(PaxUserAdmin userAdmin, User user) {
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
     * @return the current value of m_userAdmin
     */
    public PaxUserAdmin getAdmin() {
        return m_userAdmin;
    }

    /**
     * @see Authorization#getRoles()
     */
    public String[] getRoles() {
        if (m_user instanceof SPIRole) {
            SPIRole spiRoleUser = (SPIRole) m_user;
            Collection<String> roleNames = new ArrayList<String>();
            try {
                Role[] roles = m_userAdmin.getRoles(null);
                if (null != roles) {
                    for (Role role : roles) {
                        if (!Role.USER_ANYONE.equals(role.getName())) {
                            if (role instanceof SPIRole) {
                                ImplicationResult result = ((SPIRole) role).isImpliedBy(spiRoleUser, new ArrayList<String>());
                                if (ImplicationResult.IMPLIEDBY_YES == result) {
                                    String name = role.getName();
                                    roleNames.add(name);
                                }
                            } else {
                                if (role != null) {
                                    getAdmin().logMessage(AuthorizationImpl.class.getSimpleName(), LogService.LOG_WARNING, "getRoles(): role " + role.getName()
                                            + " is ignored because " + role.getClass().getName() + " does not implement the SPIRole interface");
                                }
                            }
                        }
                    }
                }
                if (!roleNames.isEmpty()) {
                    return roleNames.toArray(new String[0]);
                }
            } catch (InvalidSyntaxException e) {
                // will never be reached because UserAdmin.getRoles() allows null filters
                throw new IllegalStateException("Unexpected InvalidSyntaxException caught while using null filter: " + e.getMessage() + " for filter: "
                        + e.getFilter(), e);
            }
        } else {
            getAdmin().logMessage(AuthorizationImpl.class.getSimpleName(), LogService.LOG_WARNING, "getRoles(): denoted user is ignored because "
                    + m_user.getClass().getName() + " does not implement the SPIRole interface");
        }
        return null;
    }

    /**
     * @see Authorization#hasRole(String)
     */
    public boolean hasRole(String name) {
        Role roleToCheck = getAdmin().getRole(name);
        if (null != roleToCheck) {
            if (roleToCheck instanceof SPIRole) {
                if (m_user instanceof SPIRole) {
                    ImplicationResult result = ((SPIRole) roleToCheck).isImpliedBy((SPIRole) m_user, new ArrayList<String>());
                    return ImplicationResult.IMPLIEDBY_YES == result;
                } else {
                    getAdmin().logMessage(AuthorizationImpl.class.getSimpleName(), LogService.LOG_WARNING, "hasRole(" + name
                            + "): denoted user is ignored because " + m_user.getClass().getName() + " does not implement the SPIRole interface");
                }
            } else {
                getAdmin().logMessage(AuthorizationImpl.class.getSimpleName(), LogService.LOG_WARNING, "hasRole(" + name
                        + "): denoted role is ignored because " + roleToCheck.getClass().getName() + " does not implement the SPIRole interface");
            }
        }
        return false;
    }
}

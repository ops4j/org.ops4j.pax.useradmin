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

/**
 * Implementation of the UserAdmin Role interface.
 * 
 * @author Matthias Kuespert
 * @since 02.07.2009
 */
public class RoleImpl implements Role {

    /**
     * The name of the role.
     */
    private String         m_name       = "";

    /**
     * The UserAdmin that uses this role.
     */
    private UserAdminImpl  m_admin      = null;

    /**
     * The properties of this role.
     * 
     * Note: changing properties is detected and leads to storage update and
     * notification events.
     */
    private RoleProperties m_properties = null;

    /**
     * Constructor.
     * 
     * @param name
     *            The name of the role.
     * @param admin
     *            The UserAdmin that uses this role.
     * @param properties
     *            A map containing the raw properties of this role as read by
     *            the StorageProvider.
     */
    protected RoleImpl(String name, UserAdminImpl userAdmin, Map<String, String> properties) {
        if (name == null) {
            throw (new IllegalArgumentException(UserAdminMessages.MSG_INVALID_NAME));
        }
        if (userAdmin == null) {
            throw (new IllegalArgumentException(UserAdminMessages.MSG_INVALID_USERADMIN));
        }
        //
        m_name = name;
        m_admin = userAdmin;
        m_properties = new RoleProperties(this, m_admin, properties);
    }

    /**
     * @see Role#getName()
     */
    public String getName() {
        return m_name;
    }

    /**
     * @see Role#getProperties()
     */
    @SuppressWarnings(value = "unchecked")
    public Dictionary getProperties() {
        return m_properties;
    }

    /**
     * @see Role#getType()
     */
    public int getType() {
        return Role.ROLE;
    }

    /**
     * @see Role#getAdmin()
     */
    protected UserAdminImpl getAdmin() {
        return m_admin;
    }
}

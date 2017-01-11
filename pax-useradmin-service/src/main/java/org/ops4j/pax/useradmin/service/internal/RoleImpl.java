/*
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

import java.util.Dictionary;
import java.util.Map;

import org.ops4j.pax.useradmin.service.spi.SPIRole;
import org.osgi.service.useradmin.Role;

/**
 * Implementation of the UserAdmin Role interface.
 * 
 * @see <a href="http://www.osgi.org/javadoc/r4v42/org/osgi/service/useradmin/Role.html">http://www.osgi.org/javadoc/r4v42/org/osgi/service/useradmin/Role.html</a>
 */
public abstract class RoleImpl
        implements Role, SPIRole {

    /**
     * The name of the role.
     */
    private final String         m_name;

    /**
     * The UserAdmin that uses this role.
     */
    private final PaxUserAdmin   m_admin;

    /**
     * The properties of this role. Note: changing properties is detected and
     * leads to storage update and notification events.
     */
    private final RoleProperties m_properties;

    /**
     * Constructor.
     * 
     * @param name
     *            The name of the role.
     * @param userAdmin
     *            The UserAdmin that uses this role.
     * @param properties
     *            A map containing the raw properties of this role as read by
     *            the StorageProvider.
     */
    RoleImpl(String name, PaxUserAdmin userAdmin, Map<String, Object> properties) {
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
    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public Dictionary<String, Object> getProperties() {
        return m_properties;
    }

    @Override
    public abstract int getType();

    PaxUserAdmin getAdmin() {
        return m_admin;
    }

    @Override
    public String toString() {
        return "Role-" + getName();
    }

}

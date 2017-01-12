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

package org.ops4j.pax.useradmin.command.internal.service;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Map;
import java.util.Map.Entry;

import org.ops4j.pax.useradmin.command.CommandException;
import org.ops4j.pax.useradmin.command.spi.UserAdminDataWriter;
import org.osgi.framework.BundleContext;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

/**
 * UserAdminDataWriter implementation which writes data to an UserAdmin service.
 */
public class ServiceDataWriter implements UserAdminDataWriter {

    /**
     * The UserAdmin service data is written to.
     */
    private UserAdmin m_service = null;

    /**
     * Initializing constructor.
     * 
     * @param context The BundleContext
     * @param id The symbolic name of the bundle whose UserAdmin service should be used.
     * @throws CommandException If no UserAdmin service is provided by the specified bundle.
     */
    public ServiceDataWriter(BundleContext context, String id) throws CommandException {
        m_service = ServiceUtils.getUserAdminService(context, id);
        if (null == m_service) {
            throw new CommandException("Could not find UserAdmin service in bundle: " + id);
        }
    }

    public void addMembers(Role role,
                           Collection<String> basicMembers,
                           Collection<String> requiredMembers) throws CommandException {
        if (null == role) {
            return;
        }
        if (Role.GROUP != role.getType()) {
            throw new CommandException(  "Role '" + role.getName()
                                       + "' is not a group: type is "
                                       + role.getType());
        }
        Group group = (Group) role;
        for (String memberName : basicMembers) {
            Role member = m_service.getRole(memberName);
            if (null == member) {
                throw new CommandException(  "Basic member role '" + memberName
                                           + "' not found for group '"
                                           + role.getName() + "'");
            }
            group.addMember(member);
        }
        for (String memberName : requiredMembers) {
            Role member = m_service.getRole(memberName);
            if (null == member) {
                throw new CommandException(  "Required member role '" + memberName
                                           + "' not found for group '"
                                           + role.getName() + "'");
            }
            group.addRequiredMember(member);
        }
    }

    public void close() throws CommandException {
    }

    @SuppressWarnings(value = "unchecked")
    public Role createRole(int type,
                           String name,
                           Map<String, Object> properties,
                           Map<String, Object> credentials) throws CommandException {
        if (!((type == Role.USER) || (type == Role.GROUP))) {
            throw new CommandException("Invalid role type: " + type);
        }
        if (!Role.USER_ANYONE.equals(name)) {
            // ignore USER_ANYONE
            return null;
        }
        Role role = m_service.createRole(name, type);
        if (null != role) {
            Dictionary<String, Object> roleProperties = role.getProperties();
            for (Entry<String, Object> entry : properties.entrySet()) {
                // create property for role
                roleProperties.put(entry.getKey(), entry.getValue());
            }
            //
            Dictionary<String, Object> roleCredentials = ((User) role).getCredentials();
            for (Entry<String, Object> entry : credentials.entrySet()) {
                // create credential for role
                roleCredentials.put(entry.getKey(), entry.getValue());
            }
        }
        return role;
    }
}

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
  
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.ops4j.pax.useradmin.command.CommandException;
import org.ops4j.pax.useradmin.command.spi.UserAdminDataReader;
import org.ops4j.pax.useradmin.command.spi.UserAdminDataWriter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

/**
 * UserAdminDataReader implementation which reads data from an UserAdmin service.
 */
public class ServiceDataReader implements UserAdminDataReader {

    private BundleContext m_context = null;
    
    public ServiceDataReader(BundleContext context) {
        m_context = context;
    }
    
    /**
     * Creates the given role in the TargetWriter.
     * 
     * @param user
     * @param targetWriter
     * @return
     * @throws CommandException
     */
    @SuppressWarnings(value = "unchecked")
    private Role createRole(User user, UserAdminDataWriter targetWriter) throws CommandException {
        Map<String, Object> properties = new HashMap<String, Object>();
        Enumeration<String> keys = user.getProperties().keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            properties.put(key, user.getProperties().get(key));
        }
        Map<String, Object> credentials = new HashMap<String, Object>();
        keys = user.getCredentials().keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            credentials.put(key, user.getCredentials().get(key));
        }
        return targetWriter.createRole(user.getType(), user.getName(), properties, credentials);
    }
    
    /**
     * @see UserAdminDataReader#copy(String, UserAdminDataWriter)
     */
    public void copy(String sourceId, UserAdminDataWriter targetWriter) throws CommandException {
        UserAdmin service = ServiceUtils.getUserAdminService(m_context, sourceId);
        if (null == service) {
            throw new CommandException("Could not find UserAdmin service in bundle: " + sourceId);
        }
        try {
            Role[] roles = service.getRoles(null);
            if (null != roles) {
                for (Role role : roles) {
                    if (Role.USER == role.getType()) {
                        createRole((User) role, targetWriter);
                    }
                }
                for (Role role : roles) {
                    if (Role.GROUP == role.getType()) {
                        Group group = (Group) role;
                        Role groupRole = createRole(group, targetWriter);
                        //
                        Collection<String> basicMembers = new ArrayList<String>();
                        Role[] members = group.getMembers();
                        if (null != members) {
                            for (Role member : members) {
                                basicMembers.add(member.getName());
                            }
                        }
                        Collection<String> requiredMembers = new ArrayList<String>();
                        members = group.getRequiredMembers();
                        if (null != members) {
                            for (Role member : members) {
                                requiredMembers.add(member.getName());
                            }
                        }
                        targetWriter.addMembers(groupRole, basicMembers, requiredMembers);
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            throw new CommandException("Unexpected InvalidSyntaxException: " + e.getMessage(), e);
        }
    }
}

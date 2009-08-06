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

package org.ops4j.pax.useradmin.command.internal.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import org.ops4j.pax.useradmin.command.CommandException;
import org.ops4j.pax.useradmin.command.spi.UserAdminDataWriter;
import org.osgi.service.useradmin.Role;

/**
 * UserAdminDataWriter implementation which writes data to an XML file.
 * 
 * @author Matthias Kuespert
 * @since  04.08.2009
 */
public class XMLDataWriter implements UserAdminDataWriter {

    private PrintStream m_out = null;

    /**
     * A class to store role data .
     * 
     * @author Matthias Kuespert
     * @since  05.08.2009
     */
    protected class RoleData implements Role {
        
        private int m_type = Role.ROLE;
        private String m_name = null;
        private Map<String, Object> m_properties = null;
        private Map<String, Object> m_credentials = null;
        private Collection<String> m_basicMembers = new ArrayList<String>();
        private Collection<String> m_requiredMembers = new ArrayList<String>();
        
        protected RoleData(int type,
                           String name,
                           Map<String, Object> properties,
                           Map<String, Object> credentials) {
            m_type = type;
            m_name = name;
            m_properties = properties;
            m_credentials = credentials;
        }
        
        public String getName() {
            return m_name;
        }
        
        public Dictionary<?, ?> getProperties() {
            return new Hashtable<String, Object>(m_properties);
        }

        public Dictionary<?, ?> getCredentials() {
            return new Hashtable<String, Object>(m_credentials);
        }

        public int getType() {
            return m_type;
        }
        
        public void addBasicMember(String name) {
            m_basicMembers.add(name);
        }

        public void addRequiredMember(String name) {
            m_requiredMembers.add(name);
        }
        
        public Collection<String> getBasicMembers() {
            return m_basicMembers;
        }
        
        public Collection<String> getRequiredMembers() {
            return m_requiredMembers;
        }
    }
    
    private Collection<RoleData> m_users = new HashSet<RoleData>();
    
    private Collection<RoleData> m_groups = new HashSet<RoleData>();
    
    public XMLDataWriter(String id) throws CommandException {
        File file = new File(id);
        try {
            m_out = new PrintStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new CommandException("Output file could not be opened: " + id);
        }
    }
    
    /**
     * @see UserAdminDataWriter#addMembers(Role, Collection, Collection)
     */
    public void addMembers(Role role,
                           Collection<String> basicMembers,
                           Collection<String> requiredMembers) throws CommandException {
        if (Role.GROUP != role.getType()) {
            throw new CommandException("Role '" + role.getName() + "' is not a group: type is "
                                       + role.getType());
        }
        if (!m_groups.contains(role)) {
            throw new CommandException("Group '" + role.getName() + "' not found");
        }
        // add members:
        for (String userId : basicMembers) {
            ((RoleData) role).addBasicMember(userId);
        }
        for (String userId : requiredMembers) {
            ((RoleData) role).addRequiredMember(userId);
        }
    }

    private void printAttribute(PrintStream out, String type, String key, Object value) {
        out.println("      <" + XMLConstants.ELEMENT_ATTRIBUTE + " " + XMLConstants.ATTRIBUTE_TYPE + "  = \"" + type + "\"");
        out.println("                 " + XMLConstants.ATTRIBUTE_KEY + "   = \"" + key + "\"");
        out.println("                 " + XMLConstants.ATTRIBUTE_VALUE + " = \"" + value + "\"");
        out.println("                 />");
    }

    private void printRoleAttributes(PrintStream out, Dictionary<?, ?> data, String type) {
        Enumeration<?> keys = data.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            Object value = data.get(key);
            printAttribute(m_out, type, key, value);
        }
    }

    private void printRoleMembers(PrintStream out, Collection<String> data, String type, String value) {
        for (String memberId : data) {
            printAttribute(out, type, memberId, value);
        }
    }

    /**
     * @see UserAdminDataWriter#close()
     */
    public void close() throws CommandException {
        // write all data
        //
        m_out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        m_out.println(  "<!DOCTYPE " + XMLConstants.ELEMENT_ROOT
                      + " SYSTEM \"" + XMLConstants.ELEMENT_ROOT + ".dtd\">");
        m_out.println("<" + XMLConstants.ELEMENT_ROOT + ">");
        //
        m_out.println("  <" + XMLConstants.ELEMENT_USERS + ">");
        for (RoleData data : m_users) {
            // write XML
            m_out.println(  "    <" + XMLConstants.ELEMENT_ROLE
                          + " " + XMLConstants.ATTRIBUTE_NAME
                          + " = \"" + data.getName() + "\">");
            printRoleAttributes(m_out, data.getProperties(), XMLConstants.ELEMENT_ATT_TYPE_PROPERTY);
            printRoleAttributes(m_out, data.getCredentials(),
                                XMLConstants.ELEMENT_ATT_TYPE_CREDENTIAL);
            m_out.println("    </" + XMLConstants.ELEMENT_ROLE + ">");
        }
        m_out.println("  </" + XMLConstants.ELEMENT_USERS + ">");
        m_out.println("  <" + XMLConstants.ELEMENT_GROUPS + ">");
        for (RoleData data : m_groups) {
            // write XML
            m_out.println(  "    <" + XMLConstants.ELEMENT_ROLE
                          + " " + XMLConstants.ATTRIBUTE_NAME
                          + " = \"" + data.getName() + "\">");
            printRoleAttributes(m_out, data.getProperties(), XMLConstants.ELEMENT_ATT_TYPE_PROPERTY);
            printRoleAttributes(m_out, data.getCredentials(),
                                XMLConstants.ELEMENT_ATT_TYPE_CREDENTIAL);
            printRoleMembers(m_out, data.getBasicMembers(), XMLConstants.ELEMENT_ATT_TYPE_MEMBER,
                             XMLConstants.ELEMENT_ATT_TYPE_MEMBER_BASIC);
            printRoleMembers(m_out, data.getRequiredMembers(),
                             XMLConstants.ELEMENT_ATT_TYPE_MEMBER,
                             XMLConstants.ELEMENT_ATT_TYPE_MEMBER_REQUIRED);
            m_out.println("    </" + XMLConstants.ELEMENT_ROLE + ">");
        }
        m_out.println("  </" + XMLConstants.ELEMENT_GROUPS + ">");
        m_out.println("</" + XMLConstants.ELEMENT_ROOT + ">");
        // close the underlying stream
        m_out.close();
    }
    
    /**
     * @see UserAdminDataWriter#createRole(int, String, Map, Map)
     */
    public Role createRole(int type,
                           String name,
                           Map<String, Object> properties,
                           Map<String, Object> credentials) throws CommandException {
        if (!((type == Role.USER) || (type == Role.GROUP))) {
            throw new CommandException("Invalid role type: " + type);
        }
        RoleData data = new RoleData(type, name, properties, credentials);
        switch (data.getType()) {
            case Role.GROUP:
                m_groups.add(data);
                break;
            case Role.USER:
                m_users.add(data);
                break;
        }
        return data;
    }
}

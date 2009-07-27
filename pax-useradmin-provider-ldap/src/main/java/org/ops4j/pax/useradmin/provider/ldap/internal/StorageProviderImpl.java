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
package org.ops4j.pax.useradmin.provider.ldap.internal;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.ops4j.pax.useradmin.provider.ldap.ConfigurationConstants;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.UserAdminFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPModification;
import com.novell.ldap.LDAPSearchResults;

public class StorageProviderImpl implements StorageProvider, ManagedService {

    private static final String PATTERN_SPLIT_LIST_VALUE = ", *";

    private String              m_accessUser             = "";
    private String              m_accessPassword         = "";
    private String              m_host                   = ConfigurationConstants.DEFAULT_LDAP_SERVER_URL;
    private String              m_port                   = ConfigurationConstants.DEFAULT_LDAP_SERVER_PORT;

    private String              m_rootDN                 = ConfigurationConstants.DEFAULT_LDAP_ROOT_DN;
    private String              m_rootUsersDN            = ConfigurationConstants.DEFAULT_LDAP_ROOT_USERS
                                                                         + "," + m_rootDN;
    private String              m_rootGroupsDN           = ConfigurationConstants.DEFAULT_LDAP_ROOT_GROUPS
                                                                         + "," + m_rootDN;

    private String              m_userObjectclass        = ConfigurationConstants.DEFAULT_USER_OBJECTCLASS;
    private String              m_userIdAttr             = ConfigurationConstants.DEFAULT_USER_ATTR_ID;
    private String              m_userMandatoryAttr      = ConfigurationConstants.DEFAULT_USER_ATTR_MANDATORY;
    private String              m_userCredentialAttr     = ConfigurationConstants.DEFAULT_USER_ATTR_CREDENTIAL;

    private String              m_groupObjectclass       = ConfigurationConstants.DEFAULT_GROUP_OBJECTCLASS;
    private String              m_groupIdAttr            = ConfigurationConstants.DEFAULT_GROUP_ATTR_ID;
    private String              m_groupMandatoryAttr     = ConfigurationConstants.DEFAULT_GROUP_ATTR_MANDATORY;
    private String              m_groupMemberAttr        = ConfigurationConstants.DEFAULT_GROUP_ATTR_MEMBER;
    private String              m_groupCredentialAttr    = ConfigurationConstants.DEFAULT_GROUP_ATTR_CREDENTIAL;
    
    /**
     * The connection which is used for access.
     */
    private LDAPConnection m_connection = null;

    /**
     * Constructor.
     * 
     * @param connection Unit tests can provide a mock via this parameter.
     */
    protected StorageProviderImpl(BundleContext context, LDAPConnection connection) {
        if (null == connection) {
            throw new IllegalArgumentException("Internal error: no LDAPConnection object specified when constructing the StorageProvider instance.");
        }
        m_connection = connection;
        if (null == context) {
            throw new IllegalArgumentException("Internal error: no BundleContext object specified when constructing the StorageProvider instance.");
        }
    }

    // ManagedService interface
    
    /**
     * Retrieves a property and throws an exception if not found.
     * 
     * @param properties The properties used for lookup.
     * @param name The name of the property to retrieve.
     * @return The value of the property.
     * @throws ConfigurationException If the property is not found in the given properties.
     * @throws IllegalArgumentException If the properties argument is null.
     */
    private static String getMandatoryProperty(Dictionary<String, String> properties,
                                               String name) throws ConfigurationException {
        if (null == properties) {
            throw new IllegalArgumentException("getMandatoryProperty() argument 'properties' must not be null");
        }
        String value = properties.get(name);
        if (null == value) {
            throw new ConfigurationException(name,
                                               "no value found for property '"
                                             + name
                                             + "' - please check the configuration");
        }
        return value;
    }

    /**
     * Retrieves a property and returns a default value if not found.
     * 
     * @param properties The properties used for lookup.
     * @param name The name of the property to retrieve.
     * @param defaultValue The default value to return if the property is not found.
     * @return The value of the property or the given default value.
     * @throws IllegalArgumentException If the properties argument is null.
     */
    private static String getOptionalProperty(Dictionary<String, String> properties,
                                              String name,
                                              String defaultValue) {
        if (null == properties) {
            throw new IllegalArgumentException("getMandatoryProperty() argument 'properties' must not be null");
        }
        String value = properties.get(name);
        if (null == value) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * @see ManagedService#updated(Dictionary)
     */
    @SuppressWarnings(value = "unchecked")
    public void updated(Dictionary properties) throws ConfigurationException {
        if (null == properties) {
            // ignore empty properties
            return;
        }
        //
        m_accessUser = getOptionalProperty(properties,
                                           ConfigurationConstants.PROP_LDAP_ACCESS_USER, "");
        m_accessPassword = getOptionalProperty(properties,
                                               ConfigurationConstants.PROP_LDAP_ACCESS_PWD, "");
        //
        m_host = getOptionalProperty(properties, ConfigurationConstants.PROP_LDAP_SERVER_URL,
                                     ConfigurationConstants.DEFAULT_LDAP_SERVER_URL);
        m_port = getOptionalProperty(properties, ConfigurationConstants.PROP_LDAP_SERVER_PORT,
                                     ConfigurationConstants.DEFAULT_LDAP_SERVER_PORT);
        //
        m_rootDN = getMandatoryProperty(properties, ConfigurationConstants.PROP_LDAP_ROOT_DN);
        m_rootUsersDN = getOptionalProperty(properties,
                                            ConfigurationConstants.PROP_LDAP_ROOT_USERS,
                                            ConfigurationConstants.DEFAULT_LDAP_ROOT_USERS)
                        + "," + m_rootDN;
        m_rootGroupsDN = getOptionalProperty(properties,
                                             ConfigurationConstants.PROP_LDAP_ROOT_GROUPS,
                                             ConfigurationConstants.DEFAULT_LDAP_ROOT_GROUPS)
                         + "," + m_rootDN;

        m_userObjectclass = getOptionalProperty(properties,
                                                ConfigurationConstants.PROP_USER_OBJECTCLASS,
                                                ConfigurationConstants.DEFAULT_USER_OBJECTCLASS);
        m_userIdAttr = getOptionalProperty(properties, ConfigurationConstants.PROP_USER_ATTR_ID,
                                           ConfigurationConstants.DEFAULT_USER_ATTR_ID);
        m_userMandatoryAttr = getOptionalProperty(properties, ConfigurationConstants.PROP_USER_ATTR_MANDATORY,
                                                ConfigurationConstants.DEFAULT_USER_ATTR_MANDATORY);

        m_groupObjectclass = getOptionalProperty(properties,
                                                 ConfigurationConstants.PROP_GROUP_OBJECTCLASS,
                                                 ConfigurationConstants.DEFAULT_GROUP_OBJECTCLASS);
        m_groupIdAttr = getOptionalProperty(properties, ConfigurationConstants.PROP_GROUP_ATTR_ID,
                                            ConfigurationConstants.DEFAULT_GROUP_ATTR_ID);
        m_groupMandatoryAttr = getOptionalProperty(properties, ConfigurationConstants.PROP_GROUP_ATTR_MANDATORY,
                                                 ConfigurationConstants.DEFAULT_GROUP_ATTR_MANDATORY);
        m_groupMemberAttr = getOptionalProperty(properties, ConfigurationConstants.PROP_GROUP_ATTR_MEMBER,
                                                ConfigurationConstants.DEFAULT_GROUP_ATTR_MEMBER);
    }

    // StorageProvider interface
    
    /**
     * Opens a connection to the LDAP server.
     * 
     * Each public method implementation of this <code>StorageProvider</code>
     * must open and close a connection to the LDAP server.
     *
     * @see StorageProviderImpl#closeConnection()
     * 
     * @return An initialized connection.
     * @throws StorageException If the connection could not be initialized.
     */
    private LDAPConnection openConnection() throws StorageException {
        try {
            if (m_connection.isConnected() || m_connection.isBound()) {
                m_connection.disconnect();
            }
            m_connection.connect(m_host, new Integer(m_port));
            m_connection.bind(LDAPConnection.LDAP_V3, m_accessUser, m_accessPassword.getBytes("UTF8"));
            return m_connection;
        } catch (LDAPException e) {
            throw new StorageException("Error opening connection to LDAP server '"
                                       + m_host + ":" + m_port + "': " + e.getMessage() + " - " + e.getLDAPErrorMessage());
        } catch (UnsupportedEncodingException e) {
            throw new StorageException("Unknown encoding when opening connection: " + e.getMessage());
        }
    }

    /**
     * Closes the current connection.
     * 
     * Each public method implementation of this <code>StorageProvider</code>
     * must open and close a connection to the LDAP server.
     *
     * @see StorageProviderImpl#openConnection()
     * 
     * @throws StorageException
     */
    private void closeConnection() throws StorageException {
        try {
            m_connection.disconnect();
        } catch (LDAPException e) {
            throw new StorageException("Error closing connection: " + e.getMessage());
        }
    }
    
    private boolean configuredClassedContainedInObjectClasses(String configuredClasslist, String[] objectClasses) {
        for (String typeName : configuredClasslist.split(PATTERN_SPLIT_LIST_VALUE)) {
            boolean contained = false;
            for (String objectClass : objectClasses) {
                if (objectClass.trim().equals(typeName.trim())) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                return false;
            }
        }
        return true;
    }
    
    private int getRoleType(String[] objectClasses) {
        // check if all our required user objectclasses are contained in the
        // given list
        if (configuredClassedContainedInObjectClasses(m_userObjectclass, objectClasses)) {
            return Role.USER;
        }
        if (configuredClassedContainedInObjectClasses(m_groupObjectclass, objectClasses)) {
            return Role.GROUP;
        }
        return Role.ROLE;
    }

    @SuppressWarnings(value = "unchecked")
    private Role createRole(UserAdminFactory factory, LDAPEntry entry) throws StorageException {
        Map<String, Object> properties = new HashMap<String, Object>();
        Map<String, Object> credentials = new HashMap<String, Object>();
        // first determine the type
        LDAPAttribute typeAttr = entry.getAttribute(ConfigurationConstants.ATTR_OBJECTCLASS);
        if (null == typeAttr) {
            throw new StorageException("No type attribute '" + ConfigurationConstants.ATTR_OBJECTCLASS + "' found for entry: " + entry);
        }
        int type = getRoleType(typeAttr.getStringValueArray());
        // then read additional attributes
        Iterator<LDAPAttribute> it = entry.getAttributeSet().iterator();
        while (it.hasNext()) {
            LDAPAttribute attribute = it.next();
            if (ConfigurationConstants.ATTR_OBJECTCLASS.equals(attribute.getName())) {
                // ignore: we've read that already
            } else if (   (type == Role.GROUP && m_groupCredentialAttr.equals(attribute.getName()))
                       || (type == Role.USER && m_userCredentialAttr.equals(attribute.getName()))) {
                for (String value : attribute.getStringValueArray()) {
                    String[] data = value.split("; *");
                    if (2 != data.length) {
                        throw new StorageException("Wrong credential format '" + value + "' found for entry: " + entry);
                    }
                    credentials.put(data[0], data[1]);
                }
            } else {
                properties.put(attribute.getName(), attribute.getStringValue());
            }
        }
        switch (type) {
            case Role.USER:
                return factory.createUser(entry.getAttribute(m_userIdAttr).getStringValue(), properties, credentials);
            case Role.GROUP:
                return factory.createGroup(entry.getAttribute(m_groupIdAttr).getStringValue(), properties, credentials);
            default:
                return null;
                // throw new StorageException("Unexpected role type '" + type + "' detected.");
        }
    }
    
    private String getRoleDN(Role role) throws StorageException {
        String dn;
        switch (role.getType()) {
            case Role.USER:
                dn = m_userIdAttr + "=" + role.getName() + "," + m_rootUsersDN;
                break;
            case Role.GROUP:
                dn = m_groupIdAttr + "=" + role.getName() + "," + m_rootGroupsDN;
                break;
            default:
                throw new StorageException("Invalid role type '" + role.getType() + "'");
        }
        return dn;
    }
    
    private LDAPEntry readEntry(LDAPConnection connection, String name) throws LDAPException {
        LDAPEntry entry = null;
        try {
            String dn = m_groupIdAttr + "=" + name + "," + m_rootGroupsDN;
            entry = connection.read(dn);
        } catch (LDAPException e) {
            if (e.getResultCode() != LDAPException.NO_SUCH_OBJECT) {
                // rethrow
                throw e;
            } // else ignore
        }
        if (null == entry) {
            try {
                String dn = m_userIdAttr + "=" + name + "," + m_rootUsersDN;
                entry = connection.read(dn);
            } catch (LDAPException e) {
                if (e.getResultCode() != LDAPException.NO_SUCH_OBJECT) {
                    // rethrow
                    throw e;
                } // else ignore
            }
        }
        return entry;
    }
    /**
     * @see StorageProvider#createUser(UserAdminFactory, String)
     */
    public User createUser(UserAdminFactory factory, String name) throws StorageException {
        LDAPConnection connection = openConnection();
        // fill attribute set (for LDAP creation) and properties (for UserAdmin creation)
        LDAPAttributeSet attributes = new LDAPAttributeSet();
        Map<String, Object> properties = new HashMap<String, Object>();
        //
        attributes.add(new LDAPAttribute(ConfigurationConstants.ATTR_OBJECTCLASS,
                                         m_userObjectclass.split(PATTERN_SPLIT_LIST_VALUE)));
        attributes.add(new LDAPAttribute(m_userIdAttr, name));
        properties.put(m_userIdAttr, name);
        // set all mandatory attributes to name
        if (!"".equals(m_userMandatoryAttr)) {
            for (String attr : m_userMandatoryAttr.split(PATTERN_SPLIT_LIST_VALUE)) {
                attributes.add(new LDAPAttribute(attr.trim(), name));
                properties.put(attr.trim(), name);
            }
        }
        attributes.add(new LDAPAttribute("cn", name));
        attributes.add(new LDAPAttribute("sn", name));
        //
        LDAPEntry entry = new LDAPEntry(m_userIdAttr + "=" + name + "," + m_rootUsersDN, attributes);
        //
        try {
            connection.add(entry);
            return factory.createUser(name, properties, null);
        } catch (LDAPException e) {
            throw new StorageException(  "Error creating user '" + name + "' " + entry + ": "
                                       + e.getMessage() + " / " + e.getLDAPErrorMessage());
        } finally {
            closeConnection();
        }
    }

    public Group createGroup(UserAdminFactory factory, String name) throws StorageException {
        LDAPAttributeSet attributes = new LDAPAttributeSet();
        Map<String, Object> properties = new HashMap<String, Object>();
        attributes.add(new LDAPAttribute(ConfigurationConstants.ATTR_OBJECTCLASS,
                                         m_groupObjectclass.split(PATTERN_SPLIT_LIST_VALUE)));
        //
        attributes.add(new LDAPAttribute(m_groupIdAttr, name));
        properties.put(m_groupIdAttr, name);
        //
        // set USER_ANYONE as initial member
        //
        String userAnyoneDN = m_userIdAttr + "=" + Role.USER_ANYONE + "," + m_rootUsersDN;
        attributes.add(new LDAPAttribute(m_groupMemberAttr, userAnyoneDN));
        properties.put(m_groupMemberAttr, userAnyoneDN);
        //
        // set all mandatory attributes to name
        //
        if (!"".equals(m_groupMandatoryAttr)) {
            for (String attr : m_groupMandatoryAttr.split(PATTERN_SPLIT_LIST_VALUE)) {
                attributes.add(new LDAPAttribute(attr.trim(), name));
                properties.put(attr.trim(), name);
            }
        }
        //
        LDAPEntry entry = new LDAPEntry(m_groupIdAttr + "=" + name + "," + m_rootGroupsDN, attributes);
        //
        LDAPConnection connection = openConnection();
        try {
            connection.add(entry);
            return factory.createGroup(name, properties, null);
        } catch (LDAPException e) {
            throw new StorageException(  "Error creating group '" + name + "' " + entry + ": "
                                       + e.getMessage() + " / " + e.getLDAPErrorMessage());
        } finally {
            closeConnection();
        }
    }
    
    public boolean deleteRole(Role role) throws StorageException {
        String dn = getRoleDN(role);
        LDAPConnection connection = openConnection();
        try {
            connection.delete(dn);
            return true;
        } catch (LDAPException e) {
            throw new StorageException(  "Error deleting role with name '" + role.getName() + "': "
                                       + e.getMessage() + " / " + e.getLDAPErrorMessage());
        } finally {
            closeConnection();
        }
    }
    
    @SuppressWarnings(value = "unchecked")
    public Collection<Role> getMembers(UserAdminFactory factory, Group group) throws StorageException {
        Collection<Role> roles = new ArrayList<Role>();
        LDAPConnection connection = openConnection();
        try {
            LDAPEntry entry = readEntry(connection, group.getName());
            if (entry == null) {
                throw new StorageException(  "Internal error: group '" + group.getName()
                                           + "' could not be retrieved.");
            }
            Iterator<LDAPAttribute> it = entry.getAttributeSet().iterator();
            while (it.hasNext()) {
                LDAPAttribute attribute = it.next();
                if (m_groupMemberAttr.equals(attribute.getName())) {
                    String userDN = attribute.getStringValue();
                    LDAPEntry userEntry = connection.read(userDN);
                    if (null == userEntry) {
                        throw new StorageException(  "Internal error: group member '" + userDN
                                                     + "' could not be retrieved.");
                    }
                    Role role = createRole(factory, userEntry);
                    roles.add(role);
                }
            }
            return roles;
        } catch (LDAPException e) {
            throw new StorageException(  "Error deleting role with name '" + group.getName() + "': "
                                       + e.getMessage() + " / " + e.getLDAPErrorMessage());
        } finally {
            closeConnection();
        }
    }
    
    public Collection<Role> getRequiredMembers(UserAdminFactory factory, Group group) throws StorageException {
        // TODO: add required member handling
        throw new IllegalStateException("required member handling is not yet implemented");
    }
    
    @SuppressWarnings(value = "unchecked")
    public boolean addMember(Group group, Role role) throws StorageException {
        LDAPConnection connection = openConnection();
        try {
            LDAPEntry entry = readEntry(connection, group.getName());
            if (entry == null) {
                throw new StorageException(  "Internal error: group '" + group.getName()
                                           + "' could not be retrieved.");
            }
            String roleDN = getRoleDN(role);
            Iterator<LDAPAttribute> it = entry.getAttributeSet().iterator();
            while (it.hasNext()) {
                LDAPAttribute attribute = it.next();
                if (m_groupMemberAttr.equals(attribute.getName()) && roleDN.equals(attribute.getStringValue())) {
                    // ignore already existing members
                    return false;
                }
            }
            LDAPModification modification = new LDAPModification(LDAPModification.REPLACE, new LDAPAttribute(m_groupMemberAttr, roleDN));
            connection.modify(entry.getDN(), modification);
            return true;
        } catch (LDAPException e) {
            throw new StorageException(  "Error deleting role with name '" + group.getName() + "': "
                                       + e.getMessage() + " / " + e.getLDAPErrorMessage());
        } finally {
            closeConnection();
        }
    }
    
    public boolean addRequiredMember(Group group, Role role) throws StorageException {
        // TODO: add required member handling
        throw new IllegalStateException("required member handling is not yet implemented");
    }
    
    public boolean removeMember(Group group, Role role) throws StorageException {
        LDAPConnection connection = openConnection();
        try {
            LDAPEntry entry = readEntry(connection, group.getName());
            if (entry == null) {
                throw new StorageException(  "Internal error: group '" + group.getName()
                                           + "' could not be retrieved.");
            }
            String roleDN = getRoleDN(role);
            LDAPModification modification = new LDAPModification(LDAPModification.DELETE, new LDAPAttribute(m_groupMemberAttr, roleDN));
            connection.modify(entry.getDN(), modification);
            return true;
        } catch (LDAPException e) {
            throw new StorageException(  "Error deleting role with name '" + group.getName() + "': "
                                       + e.getMessage() + " / " + e.getLDAPErrorMessage());
        } finally {
            closeConnection();
        }
    }
    
    public void setRoleAttribute(Role role, String key, String value) throws StorageException {
        if (ConfigurationConstants.ATTR_OBJECTCLASS.equals(key)) {
            throw new StorageException(  "Cannot modify attribute '" + ConfigurationConstants.ATTR_OBJECTCLASS
                                       + "' - change the configuration instead.");
        }
        if (Role.USER == role.getType() && m_userIdAttr.equals(key)) {
            throw new StorageException(  "Cannot modify ID attribute '" + m_userIdAttr
                                       + "' - recreate the user instead.");
        }
        if (Role.GROUP == role.getType() && m_groupIdAttr.equals(key)) {
            throw new StorageException(  "Cannot modify ID attribute '" + m_groupIdAttr
                                       + "' - recreate the group instead.");
        }
        LDAPConnection connection = openConnection();
        try {
            String dn = getRoleDN(role);
            LDAPModification modification = new LDAPModification(LDAPModification.REPLACE, new LDAPAttribute(key, value));
            connection.modify(dn, modification);
        } catch (LDAPException e) {
            throw new StorageException(  "Error setting attribute '" + key + "' = '" + value
                                       + "' for role '" + role.getName() + "': "
                                       + e.getMessage() + " / " + e.getLDAPErrorMessage());
        } finally {
            closeConnection();
        }
    }

    public void setRoleAttribute(Role role, String key, byte[] value) throws StorageException {
        if (ConfigurationConstants.ATTR_OBJECTCLASS.equals(key)) {
            throw new StorageException(  "Cannot modify attribute '" + ConfigurationConstants.ATTR_OBJECTCLASS
                                         + "' - change the configuration instead.");
        }
        if (Role.USER == role.getType() && m_userIdAttr.equals(key)) {
            throw new StorageException(  "Cannot modify ID attribute '" + m_userIdAttr
                                       + "' - recreate the user instead.");
        }
        if (Role.GROUP == role.getType() && m_groupIdAttr.equals(key)) {
            throw new StorageException(  "Cannot modify ID attribute '" + m_groupIdAttr
                                       + "' - recreate the group instead.");
        }
        LDAPConnection connection = openConnection();
        try {
            String dn = getRoleDN(role);
            LDAPModification modification = new LDAPModification(LDAPModification.REPLACE, new LDAPAttribute(key, value));
            connection.modify(dn, modification);
        } catch (LDAPException e) {
            throw new StorageException(  "Error setting attribute '" + key + "' = '" + value
                                       + "' for role '" + role.getName() + "': "
                                       + e.getMessage() + " / " + e.getLDAPErrorMessage());
        } finally {
            closeConnection();
        }
    }
    
    public void removeRoleAttribute(Role role, String key) throws StorageException {
        if (ConfigurationConstants.ATTR_OBJECTCLASS.equals(key)) {
            throw new StorageException(  "Cannot remove '" + ConfigurationConstants.ATTR_OBJECTCLASS
                                       + "' attribute - change the configuration instead.");
        }
        if (Role.USER == role.getType()) {
            if (m_userIdAttr.equals(key)) {
                throw new StorageException(  "Cannot remove mandatory ID attribute '" + m_userIdAttr
                                             + "'.");
            } else if (m_userMandatoryAttr.contains(key)) {
                throw new StorageException(  "Cannot remove mandatory attribute '" + key
                                             + "'.");
            }
        }
        if (Role.GROUP == role.getType()) {
            if (m_groupIdAttr.equals(key)) {
                throw new StorageException(  "Cannot remove mandatory ID attribute '" + m_groupIdAttr
                                             + "'.");
            } else if (m_userMandatoryAttr.contains(key)) {
                throw new StorageException(  "Cannot remove mandatory attribute '" + key
                                             + "'.");
            }
        }
        LDAPConnection connection = openConnection();
        try {
            String dn = getRoleDN(role);
            LDAPModification modification = new LDAPModification(LDAPModification.DELETE, new LDAPAttribute(key, ""));
            connection.modify(dn, modification);
        } catch (LDAPException e) {
            throw new StorageException(  "Error deleting attribute '" + key
                                       + "'of role '" + role.getName() + "': "
                                       + e.getMessage() + " / " + e.getLDAPErrorMessage());
        } finally {
            closeConnection();
        }
    }
    
    // TODO: how to detect dynamically which non-mandatory arguments to delete?
    public void clearRoleAttributes(Role role) throws StorageException {
        throw new IllegalStateException("clearing attributes is not yet implemented");
    }
    
    public void setUserCredential(User user, String key, String value) throws StorageException {
        LDAPConnection connection = openConnection();
        try {
            LDAPEntry entry = readEntry(connection, user.getName());
            if (null == entry) {
                throw new StorageException("Could not find user '" + user.getName() + "'");
            }
            String dn = getRoleDN(user);
            String attrName = (Role.USER == user.getType()) ? m_userCredentialAttr : m_groupCredentialAttr; 
            LDAPAttribute attribute = entry.getAttribute(attrName);
            if (null != attribute) {
                for (String attrValue : attribute.getStringValueArray()) {
                    String[] data = attrValue.split("; *");
                    if (2 != data.length) {
                        throw new StorageException("Wrong credential format '" + value + "' found for entry: " + entry);
                    }
                    if (data[0].equals(key)) {
                        // modify existing entry
                        attribute.removeValue(attrValue);
                        attribute.addValue(key + ";" + value);
                        LDAPModification modification = new LDAPModification(LDAPModification.REPLACE, attribute);
                        connection.modify(dn, modification);
                        return;
                    }
                }
            } else {
                LDAPModification modification = new LDAPModification(LDAPModification.ADD,
                                                                     new LDAPAttribute(attrName, key + ";" + value));
                connection.modify(dn, modification);
                return;
            }
        } catch (LDAPException e) {
            throw new StorageException(  "Error setting credential for user '" + user.getName() + "': "
                                       + e.getMessage() + " / " + e.getLDAPErrorMessage());
        } finally {
            closeConnection();
        }
    }

    public void setUserCredential(User user, String key, byte[] value) throws StorageException {
        LDAPConnection connection = openConnection();
        try {
            LDAPEntry entry = readEntry(connection, user.getName());
            if (null == entry) {
                throw new StorageException("Could not find user '" + user.getName() + "'");
            }
            String dn = getRoleDN(user);
            String attrName = (Role.USER == user.getType()) ? m_userCredentialAttr : m_groupCredentialAttr; 
            LDAPAttribute attribute = entry.getAttribute(attrName);
            if (null != attribute) {
                for (String attrValue : attribute.getStringValueArray()) {
                    String[] data = attrValue.split("; *");
                    if (2 != data.length) {
                        throw new StorageException("Wrong credential format '" + value + "' found for entry: " + entry);
                    }
                    if (data[0].equals(key)) {
                        // modify existing entry
                        attribute.removeValue(attrValue);
                        attribute.addValue(key + ";" + value);
                        LDAPModification modification = new LDAPModification(LDAPModification.REPLACE, attribute);
                        connection.modify(dn, modification);
                        return;
                    }
                }
            } else {
                LDAPModification modification = new LDAPModification(LDAPModification.ADD,
                                                                     new LDAPAttribute(attrName, key + ";" + value));
                connection.modify(dn, modification);
                return;
            }
        } catch (LDAPException e) {
            throw new StorageException(  "Error setting credential for user '" + user.getName() + "': "
                                       + e.getMessage() + " / " + e.getLDAPErrorMessage());
        } finally {
            closeConnection();
        }
    }
    
    public void removeUserCredential(User user, String key) throws StorageException {
        throw new IllegalStateException("credential handling is not yet implemented");
    }
    
    public void clearUserCredentials(User user) throws StorageException {
        throw new IllegalStateException("credential handling is not yet implemented");
    }
    
    public Role getRole(UserAdminFactory factory, String name) throws StorageException {
        LDAPConnection connection = openConnection();
        try {
            LDAPEntry entry = readEntry(connection, name);
            return null != entry ? createRole(factory, entry) : null;
        } catch (LDAPException e) {
            throw new StorageException(  "Error finding role with name '" + name + "': "
                                       + e.getMessage() + " / " + e.getLDAPErrorMessage());
        } finally {
            closeConnection();
        }
    }
    
    public User getUser(UserAdminFactory factory, String key, String value) throws StorageException {
        LDAPConnection connection = openConnection();
        try {
            String filterString = "(&";
            for (String objectClass : m_userObjectclass.split(PATTERN_SPLIT_LIST_VALUE)) {
                filterString += "(" + ConfigurationConstants.ATTR_OBJECTCLASS + "=" + objectClass.trim() + ")";
            }
            filterString += "(" + key + "=" + value + "))";
            LDAPSearchResults result = connection.search(m_rootDN, LDAPConnection.SCOPE_SUB,
                                                         filterString, null, false);
            User user = null;
            while (result.hasMore()) {
                if (null != user) {
                    throw new StorageException("more than one user found");
                }
                LDAPEntry entry = result.next();
                Role role = createRole(factory, entry);
                if (null != role) {
                    if (Role.USER != role.getType()) {
                        throw new StorageException("Internal error: found role is not a user");
                    }
                    user = (User) role;
                }
            }
            return user;
        } catch (LDAPException e) {
            throw new StorageException(  "Error finding user with attribute '" + key + "=" + value + "': "
                                       + e.getMessage() + " / " + e.getLDAPErrorMessage());
        } finally {
            closeConnection();
        }
    }
    
    /**
     * @see StorageProvider#findRoles(UserAdminFactory, String)
     */
    public Collection<Role> findRoles(UserAdminFactory factory, String filterString) throws StorageException {
        LDAPConnection connection = openConnection();
        Collection<Role> roles = new ArrayList<Role>();
        try {
            LDAPSearchResults result = connection.search(m_rootDN, LDAPConnection.SCOPE_SUB,
                                                         filterString, null, false);
            while (result.hasMore()) {
                LDAPEntry entry = result.next();
                Role role = createRole(factory, entry);
                if (null != role) {
                    roles.add(role);
                }
            }
            return roles;
        } catch (LDAPException e) {
            throw new StorageException( "Error finding roles with filter '" + filterString + "': "
                                       + e.getMessage() + " / " + e.getLDAPErrorMessage());
        } finally {
            closeConnection();
        }
    }
}

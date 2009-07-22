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
    
    private String  m_accessUser       = "";
    private String  m_accessPassword   = "";
    private String  m_host             = ConfigurationConstants.DEFAULT_LDAP_SERVER_URL;
    private String  m_port             = ConfigurationConstants.DEFAULT_LDAP_SERVER_PORT;

    private String  m_rootDN           = ConfigurationConstants.DEFAULT_LDAP_ROOT_DN;
    private String  m_rootUsersDN      = ConfigurationConstants.DEFAULT_LDAP_ROOT_USERS  + "," + m_rootDN;
    private String  m_rootGroupsDN     = ConfigurationConstants.DEFAULT_LDAP_ROOT_GROUPS + "," + m_rootDN;

    private String  m_userObjectclass  = ConfigurationConstants.DEFAULT_USER_OBJECTCLASS;
    private String  m_userIdAttr       = ConfigurationConstants.DEFAULT_USER_ATTR_ID;
    private String  m_userInitialAttr  = ConfigurationConstants.DEFAULT_USER_ATTR_INITIAL;

    private String  m_groupObjectclass = ConfigurationConstants.DEFAULT_GROUP_OBJECTCLASS;
    private String  m_groupIdAttr      = ConfigurationConstants.DEFAULT_GROUP_ATTR_ID;
    private String  m_groupInitialAttr = ConfigurationConstants.DEFAULT_GROUP_ATTR_INITIAL;
    private String  m_groupMemberAttr  = ConfigurationConstants.DEFAULT_GROUP_ATTR_MEMBER;
    
    /**
     * The connection which is used for access.
     */
    private LDAPConnection m_connection = null;

    private BundleContext m_context = null;
    
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
        m_context = context;
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
        m_userInitialAttr = getOptionalProperty(properties, ConfigurationConstants.PROP_USER_ATTR_INITIAL,
                                                ConfigurationConstants.DEFAULT_USER_ATTR_INITIAL);

        m_groupObjectclass = getOptionalProperty(properties,
                                                 ConfigurationConstants.PROP_GROUP_OBJECTCLASS,
                                                 ConfigurationConstants.DEFAULT_GROUP_OBJECTCLASS);
        m_groupIdAttr = getOptionalProperty(properties, ConfigurationConstants.PROP_GROUP_ATTR_ID,
                                            ConfigurationConstants.DEFAULT_GROUP_ATTR_ID);
        m_groupInitialAttr = getOptionalProperty(properties, ConfigurationConstants.PROP_GROUP_ATTR_INITIAL,
                                                 ConfigurationConstants.DEFAULT_GROUP_ATTR_INITIAL);
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
            throw new StorageException("Error opening connection: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new StorageException("Error opening connection: " + e.getMessage());
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
    
    // TODO: check algorithm
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

    // TODO: credential handling
    @SuppressWarnings(value = "unchecked")
    private Role createRole(UserAdminFactory factory, LDAPEntry entry) throws StorageException {
        Map<String, String> properties = new HashMap<String, String>();
        int type = Role.ROLE;
        Iterator<LDAPAttribute> it = entry.getAttributeSet().iterator();
        while (it.hasNext()) {
            LDAPAttribute attribute = it.next();
            if (ConfigurationConstants.ATTR_OBJECTCLASS.equals(attribute.getName())) {
                type = getRoleType(attribute.getStringValueArray());
            } else {
                properties.put(attribute.getName(), attribute.getStringValue());
            }
        }
        switch (type) {
            case Role.USER:
                return factory.createUser(entry.getAttribute(m_userIdAttr).getStringValue(), properties, null);
            case Role.GROUP:
                return factory.createGroup(entry.getAttribute(m_groupIdAttr).getStringValue(), properties, null);
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
     * 
     * TODO: how to handle mandatory scheme arguments (i.e. sn, cn) dynamically?
     */
    public User createUser(UserAdminFactory factory, String name) throws StorageException {
        LDAPConnection connection = openConnection();
        // fill attribute set (for LDAP creation) and properties (for UserAdmin creation)
        LDAPAttributeSet attributes = new LDAPAttributeSet();
        Map<String, String> properties = new HashMap<String, String>();
        //
        attributes.add(new LDAPAttribute(ConfigurationConstants.ATTR_OBJECTCLASS,
                                         m_userObjectclass.split(PATTERN_SPLIT_LIST_VALUE)));
        attributes.add(new LDAPAttribute(m_userIdAttr, name));
        properties.put(m_userIdAttr, name);
        // set all initial attributes to name
        if (!"".equals(m_userInitialAttr)) {
            for (String attr : m_userInitialAttr.split(PATTERN_SPLIT_LIST_VALUE)) {
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
        Map<String, String> properties = new HashMap<String, String>();
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
        // set all initial attributes to name
        //
        if (!"".equals(m_groupInitialAttr)) {
            for (String attr : m_groupInitialAttr.split(PATTERN_SPLIT_LIST_VALUE)) {
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
    
    public Collection<Role> getMembers(UserAdminFactory factory, Group group) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }
    
    public Collection<Role> getRequiredMembers(UserAdminFactory factory, Group group) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }
    
    public boolean addMember(Group group, Role role) throws StorageException {
        // TODO Auto-generated method stub
        return false;
    }
    
    public boolean addRequiredMember(Group group, Role role) throws StorageException {
        // TODO Auto-generated method stub
        return false;
    }
    
    public boolean removeMember(Group group, Role role) throws StorageException {
        // TODO Auto-generated method stub
        return false;
    }
    
    public void setRoleAttribute(Role role, String key, String value) throws StorageException {
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
        // TODO Auto-generated method stub
        
    }
    public void setUserCredential(User user, String key, byte[] value) throws StorageException {
        // TODO Auto-generated method stub
        
    }
    
    public void removeUserCredential(User user, String key) throws StorageException {
        // TODO Auto-generated method stub
        
    }
    
    public void clearUserCredentials(User user) throws StorageException {
        // TODO Auto-generated method stub
        
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
        // TODO Auto-generated method stub
        return null;
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

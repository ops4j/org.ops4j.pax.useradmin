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

    private String  m_objectclassUser  = ConfigurationConstants.DEFAULT_USER_OBJECTCLASS;
    private String  m_objectclassGroup = ConfigurationConstants.DEFAULT_GROUP_OBJECTCLASS;
    private String  m_idattrUser       = ConfigurationConstants.DEFAULT_USER_IDATTR;
    private String  m_idattrGroup      = ConfigurationConstants.DEFAULT_GROUP_IDATTR;
    
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
        m_objectclassUser = getOptionalProperty(properties,
                                                ConfigurationConstants.PROP_USER_OBJECTCLASS,
                                                ConfigurationConstants.DEFAULT_USER_OBJECTCLASS);
        m_objectclassGroup = getOptionalProperty(properties,
                                                 ConfigurationConstants.PROP_GROUP_OBJECTCLASS,
                                                 ConfigurationConstants.DEFAULT_GROUP_OBJECTCLASS);
        m_idattrUser = getOptionalProperty(properties, ConfigurationConstants.PROP_USER_IDATTR,
                                           ConfigurationConstants.DEFAULT_USER_IDATTR);
        m_idattrGroup = getOptionalProperty(properties, ConfigurationConstants.PROP_GROUP_IDATTR,
                                            ConfigurationConstants.DEFAULT_GROUP_IDATTR);
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
    
    // TODO: check algorithm
    int getRoleType(String[] objectClasses) {
        
        System.out.println("--------- checking for user: " + objectClasses);
        int type = Role.ROLE;
        for (String typeName : objectClasses) {
            if (!m_objectclassUser.contains(typeName)) {
                type = -1;
                break;
            }
        }
        if (type >= Role.ROLE) {
            // 
            return Role.USER;
        }
        System.out.println("--------- checking for group: " + objectClasses);
        for (String typeName : objectClasses) {
            if (!m_objectclassGroup.contains(typeName)) {
                break;
            }
            return Role.GROUP;
        }
        return Role.ROLE;
    }

    // TODO: credential handling
    private Role createRole(UserAdminFactory factory, LDAPEntry entry) throws StorageException {
//      System.out.println("checking attributes - objectClass = " + entry);
        Map<String, String> properties = new HashMap<String, String>();
        int type = Role.ROLE;
        Iterator<LDAPAttribute> it = entry.getAttributeSet().iterator();
        while (it.hasNext()) {
            LDAPAttribute attribute = it.next();
//            System.out.println("------------- checking attr: " + attribute);
            if (ConfigurationConstants.ATTR_OBJECTCLASS.equals(attribute.getName())) {
                type = getRoleType(attribute.getStringValueArray());
            } else {
                properties.put(attribute.getName(), attribute.getStringValue());
            }
        }
        switch (type) {
            case Role.USER:
                return factory.createUser(entry.getAttribute(m_idattrUser).getStringValue(), properties, null);
            case Role.GROUP:
                return factory.createGroup(entry.getAttribute(m_idattrGroup).getStringValue(), properties, null);
            default:
                return null;
                // throw new StorageException("Unexpected role type '" + type + "' detected.");
        }
    }
    
    /**
     * @see StorageProvider#createUser(UserAdminFactory, String)
     */
    public User createUser(UserAdminFactory factory, String name) throws StorageException {
        LDAPConnection connection = openConnection();
        //
        LDAPAttributeSet attributes = new LDAPAttributeSet();
        attributes.add(new LDAPAttribute(ConfigurationConstants.ATTR_OBJECTCLASS,
                                         m_objectclassUser.split(PATTERN_SPLIT_LIST_VALUE)));
        attributes.add(new LDAPAttribute(m_idattrUser, name));
        attributes.add(new LDAPAttribute("cn", name));
        attributes.add(new LDAPAttribute("sn", name));
        //
        LDAPEntry entry = new LDAPEntry(m_idattrUser + "=" + name + "," + m_rootUsersDN, attributes);
        //
        try {
            connection.add(entry);
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(m_idattrUser, name);
            properties.put("cn", name);
            properties.put("sn", name);
            return factory.createUser(name, properties, null);
        } catch (LDAPException e) {
            throw new StorageException(  "Error creating user '" + name + "' " + entry + ": "
                                       + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    public Group createGroup(UserAdminFactory factory, String name) throws StorageException {
        LDAPAttributeSet attributes = new LDAPAttributeSet();
        attributes.add(new LDAPAttribute(ConfigurationConstants.ATTR_OBJECTCLASS,
                                         m_objectclassGroup.split(PATTERN_SPLIT_LIST_VALUE)));
        attributes.add(new LDAPAttribute(m_idattrGroup, name));
        //
        LDAPEntry entry = new LDAPEntry(m_idattrGroup + "=" + name + "," + m_rootGroupsDN, attributes);
        //
        LDAPConnection connection = openConnection();
        try {
            connection.add(entry);
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(m_idattrGroup, name);
            return factory.createGroup(name, properties, null);
        } catch (LDAPException e) {
            throw new StorageException(  "Error creating group '" + name + "' " + entry + ": "
                                       + e.getMessage());
        } finally {
            closeConnection();
        }
    }
    
    public boolean deleteRole(Role role) throws StorageException {
        // TODO Auto-generated method stub
        return false;
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
        // TODO Auto-generated method stub
        
    }
    public void setRoleAttribute(Role role, String key, byte[] value) throws StorageException {
        // TODO Auto-generated method stub
        
    }
    
    public void removeRoleAttribute(Role role, String key) throws StorageException {
        // TODO Auto-generated method stub
        
    }
    
    public void clearRoleAttributes(Role role) throws StorageException {
        // TODO Auto-generated method stub
        
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
            
//            connection.read(new LDAPUrl(m_host, new Integer(m_port), m_idattrGroup + "=" + name + "," + m_rootGroupsDN));
            
            
            
//            LDAPSearchResults result;
            // search groups
            System.out.println("---------------- checking " + m_rootDN + " for " + "(" + m_idattrGroup + ":dn:=" + name + ")");
//            LDAPSearchResults result = connection.search(m_rootGroupsDN, LDAPConnection.SCOPE_SUB, "(" + m_idattrGroup + "=" + name + ")", null, false);
            LDAPSearchResults result = connection.search(m_rootDN, LDAPConnection.SCOPE_SUB, "(" + m_idattrGroup + ":dn:=" + name + ")", null, false);
            System.out.println("---------------- hasMore groups - count = : " + result.getCount());
            while (result.hasMore()) {
                System.out.println("---------------- hasMore - reading group ");
                LDAPEntry entry = result.next();
                System.out.println("---------------- hasMore groups: " + entry);
                return createRole(factory, entry);
            }
//            if (result.getCount() > 0) {
//                if (result.getCount() > 1) {
//                    throw new StorageException("More than one group found with name '" + name + "'");
//                }
//                return createRole(factory, result.next());
//            }
            // search groups
            System.out.println("---------------- checking " + m_rootUsersDN + " for " + "(" + m_idattrUser + ":dn:=" + name + ")");
            // TODO: check for != 1 results
            result = connection.search(m_rootUsersDN, LDAPConnection.SCOPE_SUB, "(" + m_idattrUser + ":dn:=" + name + ")", null, false);
            while (result.hasMore()) {
                LDAPEntry entry = result.next();
                System.out.println("---------------- hasMore: " + entry);
                return createRole(factory, entry);
            }
//            if (result.getCount() > 0) {
//                if (result.getCount() > 1) {
//                    throw new StorageException("More than one user found with name '" + name + "'");
//                }
//            }
            return null;
        } catch (LDAPException e) {
            throw new StorageException(  "Error finding role with name '" + name + "': "
                                       + e.getMessage());
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
                                       + e.getMessage());
        } finally {
            closeConnection();
        }
    }
}

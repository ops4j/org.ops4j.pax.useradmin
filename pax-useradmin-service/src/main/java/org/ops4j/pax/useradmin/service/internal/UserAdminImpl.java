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

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.ops4j.pax.useradmin.service.UserAdminConstants;
import org.ops4j.pax.useradmin.service.UserAdminTools;
import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.ops4j.pax.useradmin.service.spi.UserAdminFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.service.useradmin.UserAdminEvent;
import org.osgi.service.useradmin.UserAdminListener;
import org.osgi.service.useradmin.UserAdminPermission;
import org.osgi.util.tracker.ServiceTracker;

/**
 * An implementation of the OSGi UserAdmin service. Allows to administer user
 * and group/role data using pluggable storage providers that connect to an
 * underlying datastore.
 * 
 * @see <a href="http://www.osgi.org/javadoc/r4v42/org/osgi/service/useradmin/UserAdmin.html" />
 * 
 * @author Matthias Kuespert
 * @since 02.07.2009
 */
public class UserAdminImpl implements UserAdmin, ManagedService, UserAdminUtil, UserAdminFactory {

    /**
     * The context of the OSGi bundle containing this service. 
     */
    private BundleContext       m_context         = null;

    /**
     * The administrative permission used to verify access to restricted functionality.
     */
    private UserAdminPermission m_adminPermission = null;

    /**
     * The ServiceTracker which monitors the service used to store data.
     */
    private ServiceTracker      m_storageService  = null;

    /**
     * The ServiceTracker which monitors the service used for logging.
     */
    private ServiceTracker      m_logService      = null;

    /**
     * The ServiceTracker which monitors the service used for firing events.
     */
    private ServiceTracker      m_eventService    = null;

    /**
     * The ServiceTracker which tracks the registered UserAdminListeners.
     */
    private ServiceTracker      m_eventListeners  = null;

    /**
     * The encryptor that is used for encrypting sensible data (e.g. user credentials).
     */
    private EncryptorImpl       m_encryptor       = null;

    /**
     * Constructor - creates and initializes a <code>UserAdminImpl</code> instance.
     * 
     * @param context The <code>BundleContext</code>
     * @param storageService A <code>ServiceTracker</code> to locate the <code>StorageProvider</code> service to use.
     * @param logService A <code>ServiceTracker</code> to locate the <code>LogService</code> to use.
     * @param eventService A <code>ServiceTracker</code> to locate the <code>EventAdmin</code> service to use.
     */
    protected UserAdminImpl(BundleContext context,
                            ServiceTracker storageService,
                            ServiceTracker logService,
                            ServiceTracker eventService) {
        if (null == storageService) {
            throw new IllegalArgumentException("No StorageProvider ServiceTracker specified.");
        }
        if (null == logService) {
            throw new IllegalArgumentException("No LogService ServiceTracker specified.");
        }
        if (null == eventService) {
            throw new IllegalArgumentException("No EventAdmin ServiceTracker specified.");
        }
        m_storageService = storageService;
        m_storageService.open();
        m_logService = logService;
        m_logService.open();
        m_eventService = eventService;
        m_eventService.open();
        m_context = context;
        //
        m_eventListeners = new ServiceTracker(context, UserAdminListener.class.getName(), null);
        m_eventListeners.open();
        //
        m_encryptor = null;
    }

    /**
     * Maps event codes to strings.
     * 
     * @param type The type of the event
     * @return The event code as string
     */
    private String getEventTypeName(int type) {
        String typeName;
        switch (type) {
            case UserAdminEvent.ROLE_CHANGED:
                typeName = "ROLE_CHANGED";
                break;
            case UserAdminEvent.ROLE_CREATED:
                typeName = "ROLE_CREATED";
                break;
            case UserAdminEvent.ROLE_REMOVED:
                typeName = "ROLE_REMOVED";
                break;
            default:
                // TODO: shouldn't that result in an exception?
                typeName = "Event" + type;
        }
        return typeName;
    }

    /**
     * Checks if the caller has admin permissions when security is enabled. If
     * security is not enabled nothing happens here.
     * 
     * @throws SecurityException if security is enabled, a security
     *         manager exists and the caller does not have the
     *         UserAdminPermission with name admin.
     */
    protected void checkAdminPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (null != sm) {
            if (null == m_adminPermission) {
                m_adminPermission = new UserAdminPermission(UserAdminPermission.ADMIN, null);
            }
            sm.checkPermission(m_adminPermission);
        }
    }
    
    /**
     * Creates an appropriate encryptor.
     * 
     * @param encryptionAlgorithm The encryption algorithm to use.
     * @param encryptionRandomAlgorithm The random number algorithm to use.
     * @param encryptionRandomAlgorithmSaltLength The klength of the salt to use for random number generation.
     * @return An implementation of the encryptor.
     * 
     * @throws ConfigurationException if the given algorithm doesn't exist
     */
    private EncryptorImpl createEncryptor(String encryptionAlgorithm,
                                          String encryptionRandomAlgorithm,
                                          String encryptionRandomAlgorithmSaltLength) throws ConfigurationException {
        EncryptorImpl encryptor;
        try {
            encryptor = new EncryptorImpl(encryptionAlgorithm,
                                            encryptionRandomAlgorithm,
                                            encryptionRandomAlgorithmSaltLength);
        } catch (NoSuchAlgorithmException e) {
            throw new ConfigurationException(UserAdminConstants.PROP_ENCRYPTION_ALGORITHM
                                               + " or " + UserAdminConstants.PROP_ENCRYPTION_RANDOM_ALGORITHM,
                                             "Encryption algorithm not supported: " + e.getMessage(), e);
        }
        return encryptor;
    }

    // ManagedService interface
    
    /**
     * Copies all properties to an internal Map.
     * 
     * @see ManagedService#updated(Dictionary)
     */
    @SuppressWarnings(value = "unchecked")
    public void updated(Dictionary properties) throws ConfigurationException {
        if (null == properties) {
            // ignore empty properties
            return;
        }
        String encryptionAlgorithm = UserAdminTools.getOptionalProperty(properties,
                                                                        UserAdminConstants.PROP_ENCRYPTION_ALGORITHM,
                                                                        UserAdminConstants.ENCRYPTION_ALGORITHM_NONE);
        if (UserAdminConstants.ENCRYPTION_ALGORITHM_NONE.equals(encryptionAlgorithm)) {
            // set no encryption
            m_encryptor = null;
        }
        else {
            // create encryptor ...
            String encryptionRandomAlgorithm = UserAdminTools.getOptionalProperty(properties,
                                                                                  UserAdminConstants.PROP_ENCRYPTION_RANDOM_ALGORITHM,
                                                                                  UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM);
            String encryptionRandomAlgorithmSaltLength = UserAdminTools.getOptionalProperty(properties,
                                                                                            UserAdminConstants.PROP_ENCRYPTION_RANDOM_SALTLENGTH,
                                                                                            UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
            m_encryptor = createEncryptor(encryptionAlgorithm,
                                          encryptionRandomAlgorithm,
                                          encryptionRandomAlgorithmSaltLength);
        }
    }
    
    // UserAdmin interface

    /**
     * @see UserAdmin#createRole(String, int)
     */
    public Role createRole(String name, int type) {
        if (null == name) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_NAME);
        }
        if (   (type != Role.GROUP)
            && (type != Role.USER)) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_ROLE_TYPE);
        }
        if (null != getRole(name)) {
            logMessage(this, LogService.LOG_WARNING, "role already exists: " + name);
            return null;
        }
        checkAdminPermission();
        //
        Role role = null;
        try {
            StorageProvider storageProvider = getStorageProvider();
            switch (type) {
                case Role.USER:
                    role = storageProvider.createUser(this, name);
                    break;

                case Role.GROUP:
                    role = storageProvider.createGroup(this, name);
                    break;

                default:
                    // never reached b/o previous checks
                    break;
            }
            fireEvent(UserAdminEvent.ROLE_CREATED, role);
        } catch (StorageException e) {
            logMessage(this, LogService.LOG_ERROR, e.getMessage());
        }
        return role;
    }

    /**
     * @see UserAdmin#getAuthorization(User)
     */
    public Authorization getAuthorization(User user) {
        if (null == user) {
            throw (new IllegalArgumentException(UserAdminMessages.MSG_INVALID_USER));
        }
        return new AuthorizationImpl(this, user);
    }

    /**
     * @see UserAdmin#getRole(String)
     */
    public Role getRole(String name) {
        if (null == name) {
            throw (new IllegalArgumentException(UserAdminMessages.MSG_INVALID_NAME));
        }
        if ("".equals(name)) {
            name = Role.USER_ANYONE;
        }
        //
        try {
            StorageProvider storage = getStorageProvider();
            return storage.getRole(this, name);
        } catch (StorageException e) {
            logMessage(this, LogService.LOG_ERROR, e.getMessage());
        }
        return null;
    }

    /**
     * @see UserAdmin#getRoles(String)
     */
    public Role[] getRoles(String filter) throws InvalidSyntaxException {
        try {
            StorageProvider storage = getStorageProvider();
            Collection<Role> roles = storage.findRoles(this, filter);
            if (!roles.isEmpty()) {
                return roles.toArray(new Role[roles.size()]);
            }
        } catch (StorageException e) {
            logMessage(this, LogService.LOG_ERROR, e.getMessage());
        }
        return null;
    }

    /**
     * @see UserAdmin#getUser(String, String)
     */
    public User getUser(String key, String value) {
        if (null == key) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY);
        }
        if (null == value) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_VALUE);
        }
        try {
            StorageProvider storage = getStorageProvider();
            return storage.getUser(this, key, value);
        } catch (StorageException e) {
            logMessage(this, LogService.LOG_ERROR, e.getMessage());
        }
        return null;
    }

    /**
     * @see UserAdmin#removeRole(String)
     */
    public boolean removeRole(String name) {
        if (null == name) {
            throw (new IllegalArgumentException(UserAdminMessages.MSG_INVALID_NAME));
        }
        if (!"".equals(name) && !Role.USER_ANYONE.equals(name)) {
            Role role = getRole(name);
            if (null != role) {
                checkAdminPermission();
                try {
                    StorageProvider storageProvider = getStorageProvider();
                    if (storageProvider.deleteRole(role)) {
                        fireEvent(UserAdminEvent.ROLE_REMOVED, role);
                        return true;
                    } else {
                        logMessage(this, LogService.LOG_ERROR, "Role '" + name + "' could not be deleted");
                    }
                } catch (StorageException e) {
                    logMessage(this, LogService.LOG_ERROR, e.getMessage());
                }
            } else {
                logMessage(this, LogService.LOG_ERROR, "Role '" + name + "' does not exist.");
            }
        } else {
            logMessage(this, LogService.LOG_ERROR, "Standard user '" + Role.USER_ANYONE + "' cannot be removed.");
        }
        return false;
    }

    // UserAdminUtil interface

    /**
     * @see UserAdminUtil#getStorageProvider()
     */
    public StorageProvider getStorageProvider() throws StorageException {
        StorageProvider storageProvider = (StorageProvider) m_storageService.getService();
        if (null == storageProvider) {
            throw new StorageException(UserAdminMessages.MSG_MISSING_STORAGE_SERVICE);
        }
        return storageProvider;
    }

    /**
     * @see UserAdminUtil#logMessage(Object, int, String)
     * 
     * TODO: do we need a check for valid levels? What to do then: exception or ignore?
     */
    public void logMessage(Object source, int level, String message) {
        LogService log = (LogService) m_logService.getService();
        if (null != log) {
            log.log(level, "[" + (source != null ? source.getClass().getName() : "none") + "] " + message);
        }
    }

    /**
     * @see UserAdminUtil#fireEvent(int, Role)
     */
    public void fireEvent(int type, Role role) {
        if (null == role) {
            throw new IllegalArgumentException("parameter role must not be null");
        }
        ServiceReference ref = m_context.getServiceReference(UserAdmin.class.getName());
        final UserAdminEvent uaEvent = new UserAdminEvent(ref, type, role);
        //
        // send event to all listeners, asynchronously - in a separate thread!!
        // 
        Object[] eventListeners = m_eventListeners.getServices();
        if (null != eventListeners) {
            for (Object listenerObject : eventListeners) {
                final UserAdminListener listener = (UserAdminListener) listenerObject;
                Thread notifyThread = new Thread() {
                    @Override
                    public void run() {
                        listener.roleChanged(uaEvent);
                    }
                };
                notifyThread.start();
            }
        }
        //
        // send event to EventAdmin if present
        //
        EventAdmin eventAdmin = (EventAdmin) m_eventService.getService();
        if (null != eventAdmin) {
            Dictionary<String, Object> properties = new Hashtable<String, Object>();
            properties.put("event", uaEvent);
            properties.put("role", role);
            properties.put("role.name", role.getName());
            properties.put("role.type", role.getType());
            properties.put("service", ref);
            properties.put("service.id", ref.getProperty(Constants.SERVICE_ID));
            properties.put("service.objectClass", ref.getProperty(Constants.OBJECTCLASS));
            properties.put("service.pid", ref.getProperty(Constants.SERVICE_PID));
            //
            Event event = new Event(UserAdminConstants.EVENT_TOPIC_PREFIX + getEventTypeName(type), properties);
            eventAdmin.postEvent(event);
        } else {
            String message =   "No event service available - cannot send event of type '"
                             + getEventTypeName(type) + "' for role '" + role.getName() + "'";
            logMessage(this, LogService.LOG_DEBUG,
                       message);
        }
    }

    /**
     * @see UserAdminUtil#checkPermission(String, String)
     */
    public void checkPermission(String name, String action) {
        SecurityManager sm = System.getSecurityManager();
        if (null != sm) {
            sm.checkPermission(new UserAdminPermission(name, action));
        }
    }
    
    /**
     * @see UserAdminUtil#encrypt(Object)
     */
    public byte[] encrypt(Object value) {
        byte[] valueBytes;
        if (value instanceof String) {
            valueBytes = ((String)value).getBytes();
        } else if (value instanceof byte[]) {
            valueBytes = (byte[]) value;
        } else {
            throw new IllegalArgumentException("Illegal value type: " + value.getClass().getName());
        }
        //
        if (null != m_encryptor) {
            valueBytes = m_encryptor.encrypt(valueBytes);
        }
        return valueBytes;
    }
    
    /**
     * @see UserAdminUtil#compareToEncryptedValue(Object, byte[])
     */
    public boolean compareToEncryptedValue(Object inputValue,
                                           byte[] storedValue) {
        byte[] valueBytes;
        if (inputValue instanceof String) {
            valueBytes = ((String)inputValue).getBytes();
        } else if (inputValue instanceof byte[]) {
            valueBytes = (byte[]) inputValue;
        } else {
            throw new IllegalArgumentException("Illegal value type: " + inputValue.getClass().getName());
        }
        //
        if (null != m_encryptor) {
            return m_encryptor.compare(valueBytes, storedValue);
        }
        return Arrays.equals(valueBytes, storedValue);
    }
    
    // UserAdminFactory interface
    
    /**
     * @see UserAdminFactory#createUser(String, Map, Map)
     */
    public User createUser(String name,
                           Map<String, Object> properties,
                           Map<String, Object> credentials) {
        return new UserImpl(name, this, properties, credentials);
    }

    /**
     * @see UserAdminFactory#createGroup(String, Map, Map)
     */
    public Group createGroup(String name,
                             Map<String, Object> properties,
                             Map<String, Object> credentials) {
        return new GroupImpl(name, this, properties, credentials);
    }
}

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
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
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
 * @see <a
 *      href="http://www.osgi.org/javadoc/r4v42/org/osgi/service/useradmin/UserAdmin.html">http://www.osgi.org/javadoc/r4v42/org/osgi/service/useradmin/UserAdmin.html</a>
 * @author Matthias Kuespert
 * @since 02.07.2009
 */
public class PaxUserAdmin implements UserAdmin, UserAdminUtil, UserAdminFactory {

    /**
     * The administrative permission used to verify access to restricted
     * functionality.
     */
    private UserAdminPermission                                        m_adminPermission = null;

    /**
     * The ServiceTracker which monitors the service used for logging.
     */
    private final ServiceTracker<LogService, LogService>               m_logService;

    /**
     * The ServiceTracker which monitors the service used for firing events.
     */
    private final ServiceTracker<EventAdmin, EventAdmin>               m_eventService;

    /**
     * The encryptor that is used for encrypting sensible data (e.g. user
     * credentials).
     */
    private EncryptorImpl                                              m_encryptor;

    private final StorageProvider                                      storageProvider;

    private ServiceRegistration<?>                                     userAdminRegistration;

    private final ServiceTracker<UserAdminListener, UserAdminListener> listenerService;

    /**
     * Constructor - creates and initializes a <code>UserAdminImpl</code>
     * instance.
     * 
     * @param context
     *            The <code>BundleContext</code>
     * @param storageService
     *            A <code>ServiceTracker</code> to locate the
     *            <code>StorageProvider</code> service to use.
     * @param logService
     *            A <code>ServiceTracker</code> to locate the
     *            <code>LogService</code> to use.
     * @param eventService
     *            A <code>ServiceTracker</code> to locate the
     *            <code>EventAdmin</code> service to use.
     */
    protected PaxUserAdmin(StorageProvider storageProvider, ServiceTracker<LogService, LogService> logService,
            ServiceTracker<EventAdmin, EventAdmin> eventService, ServiceTracker<UserAdminListener, UserAdminListener> listenerService) {
        if (null == storageProvider) {
            throw new IllegalArgumentException("No StorageProvider ServiceTracker specified.");
        }
        if (null == logService) {
            throw new IllegalArgumentException("No LogService ServiceTracker specified.");
        }
        if (null == eventService) {
            throw new IllegalArgumentException("No EventAdmin ServiceTracker specified.");
        }
        this.storageProvider = storageProvider;
        this.listenerService = listenerService;
        m_logService = logService;
        m_eventService = eventService;
    }

    /**
     * Maps event codes to strings.
     * 
     * @param type
     *            The type of the event
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
     * @throws SecurityException
     *             if security is enabled, a security manager exists and the
     *             caller does not have the UserAdminPermission with name admin.
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
     * @param encryptionAlgorithm
     *            The encryption algorithm to use.
     * @param encryptionRandomAlgorithm
     *            The random number algorithm to use.
     * @param encryptionRandomAlgorithmSaltLength
     *            The klength of the salt to use for random number generation.
     * @return An implementation of the encryptor.
     * @throws ConfigurationException
     *             if the given algorithm doesn't exist
     */
    private EncryptorImpl createEncryptor(String encryptionAlgorithm, String encryptionRandomAlgorithm, String encryptionRandomAlgorithmSaltLength) {
        try {
            return new EncryptorImpl(encryptionAlgorithm, encryptionRandomAlgorithm, encryptionRandomAlgorithmSaltLength);
        } catch (NoSuchAlgorithmException e) {
            String msg = encryptionAlgorithm + " or " + encryptionRandomAlgorithm + " Encryption algorithm not supported: " + e.getMessage();
            throw new IllegalArgumentException(msg, e);
        }
    }

    // ManagedService interface

    public void configurationUpdated(Map<String, ?> properties) {
        if (null == properties) {
            // ignore empty properties
            return;
        }
        String encryptionAlgorithm = UserAdminTools.getOptionalProperty(properties, UserAdminConstants.PROP_ENCRYPTION_ALGORITHM, UserAdminConstants.ENCRYPTION_ALGORITHM_NONE);
        if (UserAdminConstants.ENCRYPTION_ALGORITHM_NONE.equals(encryptionAlgorithm)) {
            // set no encryption
            m_encryptor = null;
        } else {
            // create encryptor ...
            String encryptionRandomAlgorithm = UserAdminTools.getOptionalProperty(properties, UserAdminConstants.PROP_ENCRYPTION_RANDOM_ALGORITHM, UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_ALGORITHM);
            String encryptionRandomAlgorithmSaltLength = UserAdminTools.getOptionalProperty(properties, UserAdminConstants.PROP_ENCRYPTION_RANDOM_SALTLENGTH, UserAdminConstants.DEFAULT_ENCRYPTION_RANDOM_SALTLENGTH);
            m_encryptor = createEncryptor(encryptionAlgorithm, encryptionRandomAlgorithm, encryptionRandomAlgorithmSaltLength);
        }
    }

    // UserAdmin interface

    /**
     * @see UserAdmin#createRole(String, int)
     */
    public Role createRole(String name, int type) {
        checkAdminPermission();
        //
        if (null == name || name.trim().length() == 0) {
            // logMessage(this, LogService.LOG_ERROR, UserAdminMessages.MSG_INVALID_NAME); // TODO: check if necessary/useful
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_NAME);
        }
        if (!((type == Role.GROUP) || (type == Role.USER))) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_ROLE_TYPE);
        }
        //
        // return null if the role already exists (see chapter 107.8.6.1)
        //
        if (null != getRole(name)) {
            logMessage(this, LogService.LOG_INFO, "createRole() - role already exists: " + name);
            return null;
        }
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
            }
            fireEvent(UserAdminEvent.ROLE_CREATED, role);
            logMessage(this, LogService.LOG_INFO, "role created: " + name + " - " + role);
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
        return storageProvider;
    }

    /**
     * @see UserAdminUtil#logMessage(Object, int, String) TODO: do we need a
     *      check for valid levels? What to do then: exception or ignore?
     */
    public void logMessage(Object source, int level, String message) {
        LogService log = m_logService.getService();
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
        ServiceReference<?> reference = userAdminRegistration.getReference();
        final UserAdminEvent uaEvent = new UserAdminEvent(reference, type, role);
        //
        // send event to all listeners, asynchronously - in a separate thread!!
        //
        UserAdminListener[] eventListeners = listenerService.getServices(new UserAdminListener[0]);
        if (null != eventListeners) {
            //FIXME dont you a thread for each listener!
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
        EventAdmin eventAdmin = m_eventService.getService();
        if (null != eventAdmin) {
            Dictionary<String, Object> properties = new Hashtable<String, Object>();
            properties.put("event", uaEvent);
            properties.put("role", role);
            properties.put("role.name", role.getName());
            properties.put("role.type", role.getType());
            properties.put("service", reference);
            properties.put("service.id", reference.getProperty(Constants.SERVICE_ID));
            properties.put("service.objectClass", reference.getProperty(Constants.OBJECTCLASS));
            properties.put("service.pid", reference.getProperty(Constants.SERVICE_PID));
            //
            Event event = new Event(UserAdminConstants.EVENT_TOPIC_PREFIX + getEventTypeName(type), properties);
            eventAdmin.postEvent(event);
        } else {
            String message = "No event service available - cannot send event of type '" + getEventTypeName(type) + "' for role '" + role.getName() + "'";
            logMessage(this, LogService.LOG_DEBUG, message);
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
    public Object encrypt(Object value) {
        if (null != m_encryptor) {
            byte[] valueBytes;
            if (value instanceof String) {
                //FIXME: This relies on platform encoding!
                valueBytes = ((String) value).getBytes();
            } else if (value instanceof byte[]) {
                valueBytes = (byte[]) value;
            } else {
                throw new IllegalArgumentException("Illegal value type: " + value.getClass().getName());
            }
            //
            return m_encryptor.encrypt(valueBytes);
        }
        return value;
    }

    /**
     * @see UserAdminUtil#compareToEncryptedValue(Object, byte[])
     */
    public boolean compareToEncryptedValue(Object inputValue, Object storedValue) {
        byte[] inputValueBytes;
        if (inputValue instanceof String) {
            inputValueBytes = ((String) inputValue).getBytes();
        } else if (inputValue instanceof byte[]) {
            inputValueBytes = (byte[]) inputValue;
        } else {
            throw new IllegalArgumentException("Illegal value type: " + inputValue.getClass().getName());
        }

        byte[] storedValueBytes;
        if (storedValue instanceof String) {
            storedValueBytes = ((String) storedValue).getBytes();
        } else if (storedValue instanceof byte[]) {
            storedValueBytes = (byte[]) storedValue;
        } else {
            throw new IllegalArgumentException("Illegal value type: " + storedValue.getClass().getName());
        }

        if (null != m_encryptor) {
            return m_encryptor.compare(inputValueBytes, storedValueBytes);
        }
        return Arrays.equals(inputValueBytes, storedValueBytes);
    }

    // UserAdminFactory interface

    /**
     * @see UserAdminFactory#createUser(String, Map, Map)
     */
    public User createUser(String name, Map<String, Object> properties, Map<String, Object> credentials) {
        return new UserImpl(name, this, properties, credentials);
    }

    /**
     * @see UserAdminFactory#createGroup(String, Map, Map)
     */
    public Group createGroup(String name, Map<String, Object> properties, Map<String, Object> credentials) {
        return new GroupImpl(name, this, properties, credentials);
    }

    /**
     * @param context
     */
    public synchronized void register(BundleContext context, String type, Long spi_service_id) {
        if (userAdminRegistration != null) {
            throw new IllegalStateException("This object is already registered under another bundle context!");
        }
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(UserAdminConstants.STORAGEPROVIDER_TYPE, type);
        properties.put(UserAdminConstants.STORAGEPROVIDER_SPI_SERVICE_ID, spi_service_id);
        userAdminRegistration = context.registerService(UserAdmin.class, this, properties);
    }

    /**
     * 
     */
    public synchronized void unregister() {
        if (userAdminRegistration == null) {
            throw new IllegalStateException("This object is not registered!");
        }
        userAdminRegistration.unregister();

    }
}

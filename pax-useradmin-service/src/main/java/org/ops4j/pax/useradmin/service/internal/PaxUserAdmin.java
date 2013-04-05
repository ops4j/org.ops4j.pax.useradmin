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
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.ops4j.pax.useradmin.service.PaxUserAdminConstants;
import org.ops4j.pax.useradmin.service.internal.encryption.EncryptorContext;
import org.ops4j.pax.useradmin.service.internal.encryption.PaxUserAdminDecryptor;
import org.ops4j.pax.useradmin.service.internal.encryption.PaxUserAdminEncryptor;
import org.ops4j.pax.useradmin.service.spi.Decryptor;
import org.ops4j.pax.useradmin.service.spi.Encryptor;
import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.ops4j.pax.useradmin.service.spi.UserAdminFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
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
    private UserAdminPermission                                        m_adminPermission;

    /**
     * The ServiceTracker which monitors the service used for logging.
     */
    private final ServiceTracker<LogService, LogService>               m_logService;

    /**
     * The ServiceTracker which monitors the service used for firing events.
     */
    private final ServiceTracker<EventAdmin, EventAdmin>               m_eventService;

    private final StorageProvider                                      storageProvider;

    private ServiceRegistration<?>                                     userAdminRegistration;

    private final ServiceTracker<UserAdminListener, UserAdminListener> listenerService;

    private Map<String, ?>                                             properties;

    private Encryptor                                                  encryptor;

    private final PaxUserAdminDecryptor                                decryptor = new PaxUserAdminDecryptor();

    private final ExecutorService                                      eventExecutor;

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
            ServiceTracker<EventAdmin, EventAdmin> eventService, ServiceTracker<UserAdminListener, UserAdminListener> listenerService,
            ExecutorService eventExecutor) {
        this.eventExecutor = eventExecutor;
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
        switch (type) {
            case UserAdminEvent.ROLE_CHANGED:
                return "ROLE_CHANGED";
            case UserAdminEvent.ROLE_CREATED:
                return "ROLE_CREATED";
            case UserAdminEvent.ROLE_REMOVED:
                return "ROLE_REMOVED";
        }
        return null;
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
        if (sm != null) {
            UserAdminPermission pm;
            synchronized (PaxUserAdmin.this) {
                if (m_adminPermission == null) {
                    m_adminPermission = new UserAdminPermission(UserAdminPermission.ADMIN, null);
                }
                pm = m_adminPermission;
            }
            sm.checkPermission(pm);
        }
    }

    // ManagedService interface

    public void configurationUpdated(Map<String, ?> properties) {
        synchronized (this) {
            this.properties = properties;
            encryptor = null;
        }
    }

    // UserAdmin interface

    /**
     * @see UserAdmin#createRole(String, int)
     */
    @Override
    public Role createRole(String name, int type) {
        checkAdminPermission();
        //
        if (null == name || name.trim().length() == 0) {
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
    @Override
    public Authorization getAuthorization(User user) {
        if (null == user) {
            throw (new IllegalArgumentException(UserAdminMessages.MSG_INVALID_USER));
        }
        return new AuthorizationImpl(this, user);
    }

    /**
     * @see UserAdmin#getRole(String)
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public StorageProvider getStorageProvider() throws StorageException {
        return storageProvider;
    }

    /**
     * @see UserAdminUtil#logMessage(Object, int, String)
     */
    @Override
    public void logMessage(Object source, int level, String message) {
        LogService log = m_logService.getService();
        if (null != log) {
            log.log(level, "[" + (source != null ? source.getClass().getName() : "none") + "] " + message);
        }
    }

    /**
     * @see UserAdminUtil#fireEvent(int, Role)
     */
    @Override
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
            for (Object listenerObject : eventListeners) {
                final UserAdminListener listener = (UserAdminListener) listenerObject;
                eventExecutor.execute(new Runnable() {

                    @Override
                    public void run() {
                        listener.roleChanged(uaEvent);
                    }
                });
            }
        }
        //
        // send event to EventAdmin if present
        //
        EventAdmin eventAdmin = m_eventService.getService();
        String name = getEventTypeName(type);
        if (null != eventAdmin && name != null) {
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
            Event event = new Event(PaxUserAdminConstants.EVENT_TOPIC_PREFIX + name, properties);
            eventAdmin.postEvent(event);
        } else {
            String message = "No event service available or incompatible type - cannot send event of type '" + name + "' for role '" + role.getName() + "'";
            logMessage(this, LogService.LOG_DEBUG, message);
        }
    }

    /**
     * @see UserAdminUtil#checkPermission(String, String)
     */
    @Override
    public void checkPermission(String name, String action) {
        SecurityManager sm = System.getSecurityManager();
        if (null != sm) {
            sm.checkPermission(new UserAdminPermission(name, action));
        }
    }

    // UserAdminFactory interface

    /**
     * @see UserAdminFactory#createUser(String, Map)
     */
    @Override
    public User createUser(String name, Map<String, Object> properties) {
        return new UserImpl(name, this, properties);
    }

    /**
     * @see UserAdminFactory#createGroup(String, Map)
     */
    @Override
    public Group createGroup(String name, Map<String, Object> properties) {
        return new GroupImpl(name, this, properties);
    }

    /**
     * @param context
     */
    public synchronized void register(BundleContext context, String type, Long spi_service_id) {
        if (userAdminRegistration != null) {
            throw new IllegalStateException("This object is already registered under another bundle context!");
        }
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(PaxUserAdminConstants.STORAGEPROVIDER_TYPE, type);
        properties.put(PaxUserAdminConstants.STORAGEPROVIDER_SPI_SERVICE_ID, spi_service_id);
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

    @Override
    public Decryptor getDecryptor() {
        return decryptor;
    }

    @Override
    public Encryptor getEncryptor() {
        synchronized (this) {
            if (encryptor == null) {
                Map<String, ?> p = properties;
                if (p == null) {
                    p = new HashMap<String, Object>();
                }
                try {
                    encryptor = new PaxUserAdminEncryptor(new EncryptorContext(p));
                } catch (NoSuchAlgorithmException e) {
                    throw new IllegalStateException("an algorithm needed for encryption is not avaiable", e);
                }
            }
            return encryptor;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getStorageProvider() + "]";
    }
}

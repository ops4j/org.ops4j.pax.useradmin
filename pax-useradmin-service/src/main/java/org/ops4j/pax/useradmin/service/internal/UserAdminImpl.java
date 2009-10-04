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
package org.ops4j.pax.useradmin.service.internal;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.ops4j.pax.useradmin.service.UserAdminConstants;
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
 * @see UserAdmin
 * 
 * @author Matthias Kuespert
 * @since 02.07.2009
 */
public class UserAdminImpl implements UserAdmin, ManagedService, UserAdminUtil, UserAdminFactory {

    private BundleContext       m_context          = null;

    private UserAdminPermission m_adminPermission  = null;
    
    /**
     * The ServiceTracker which monitors the service used to store data.
     */
    private ServiceTracker      m_storageService   = null;

    /**
     * The ServiceTracker which monitors the service used for logging.
     */
    private ServiceTracker      m_logService       = null;

    /**
     * The ServiceTracker which monitors the service used for firing events.
     */
    private ServiceTracker      m_eventService     = null;

    /**
     * The ServiceTracker which tracks the registered UserAdminListeners.
     */
    private ServiceTracker      m_eventListeners   = null;

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
    }

    /**
     * Maps event codes to strings.
     * 
     * @param type The type of the event
     * @return The event code as string
     */
    private String getEventTypeName(int type) {
        String typeName = null;
        switch (type) {
            case UserAdminEvent.ROLE_CHANGED:
                typeName = "ROLE_CHANGED";
                break;
            case UserAdminEvent.ROLE_CREATED:
                typeName = "ROLE_CREATED";
                break;
            case UserAdminEvent.ROLE_REMOVED:
                typeName = "ROLE_REMOVED";
            default:
                typeName = "Event" + type;
        }
        return typeName;
    }

    /**
     * Checks if the caller has admin permissions when security is enabled. If
     * security is not enabled nothing happens here.
     * 
     * @throws <code>SecurityException</code> If security is enabled, a security
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

    // ManagedService interface
    
    /**
     * @see ManagedService#updated(Dictionary)
     */
    @SuppressWarnings(value = "unchecked")
    public void updated(Dictionary properties) throws ConfigurationException {
        // defaults
        //
        if (null !=properties) {
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
            logMessage(this, "role already exists: " + name, LogService.LOG_WARNING);
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
            logMessage(this, e.getMessage(), LogService.LOG_ERROR);
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
        AuthorizationImpl authorization = new AuthorizationImpl(this, user);
        return authorization;
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
            logMessage(this, e.getMessage(), LogService.LOG_ERROR);
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
                return roles.toArray(new Role[0]);
            }
        } catch (StorageException e) {
            logMessage(this, e.getMessage(), LogService.LOG_ERROR);
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
            User user = storage.getUser(this, key, value);
            return user;
        } catch (StorageException e) {
            logMessage(this, e.getMessage(), LogService.LOG_ERROR);
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
                        logMessage(this, "Role '" + name + "' could not be deleted", LogService.LOG_ERROR);
                    }
                } catch (StorageException e) {
                    logMessage(this, e.getMessage(), LogService.LOG_ERROR);
                }
            } else {
                logMessage(this, "Role '" + name + "' does not exist.", LogService.LOG_ERROR);
            }
        } else {
            logMessage(this, "Standard user '" + Role.USER_ANYONE + "' cannot be removed.", LogService.LOG_ERROR);
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
     * @see UserAdminUtil#logMessage(Object, String, int)
     * 
     * TODO: do we need a check for valid levels? What to do then: exception or ignore?
     */
    public void logMessage(Object source, String message, int level) {
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
            logMessage(this, message,
                       LogService.LOG_ERROR);
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
    
    // UserAdminFactory interface
    
    /**
     * @see UserAdminFactory#createUser(String, Map, Map)
     */
    public User createUser(String name,
                           Map<String, Object> properties,
                           Map<String, Object> credentials) {
        UserImpl user = new UserImpl(name, this, properties, credentials);
        return user;
    }

    /**
     * @see UserAdminFactory#createGroup(String, Map, Map)
     */
    public Group createGroup(String name,
                             Map<String, Object> properties,
                             Map<String, Object> credentials) {
        GroupImpl group = new GroupImpl(name, this, properties, credentials);
        return group;
    }
}

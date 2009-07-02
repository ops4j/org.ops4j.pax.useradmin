package org.ops4j.pax.useradmin.service.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.ops4j.pax.useradmin.service.spi.UserAdminFactory;
import org.osgi.framework.BundleContext;
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
import org.osgi.service.useradmin.UserAdminPermission;
import org.osgi.util.tracker.ServiceTracker;

/**
 * A service to administer user and group/role data using a pluggable storage
 * implementation.
 * 
 */
public class UserAdminImpl implements UserAdmin, ManagedService, UserAdminUtil, UserAdminFactory {

    public static String        PROP_SECURITY      = "org.ops4j.pax.useradmin.security";

    // private implementation details

    private static String       EVENT_TOPIC_PREFIX = "org/osgi/service/useradmin/UserAdmin/";

    private BundleContext       m_context          = null;

    private UserAdminPermission m_adminPermission  = null;
    
    private boolean             m_checkSecurity    = true;

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

    protected UserAdminImpl(BundleContext context) {
        m_storageService = new ServiceTracker(context, StorageProvider.class.getName(), null);
        m_logService = new ServiceTracker(context, LogService.class.getName(), null);
        m_eventService = new ServiceTracker(context, EventAdmin.class.getName(), null);
        m_context = context;
    }

    private String getEventTypeName(int type) {
        switch (type) {
            case UserAdminEvent.ROLE_CHANGED:
                return "ROLE_CHANGED";

            case UserAdminEvent.ROLE_CREATED:
                return "ROLE_CREATED";

            case UserAdminEvent.ROLE_REMOVED:
                return "ROLE_REMOVED";

            default:
                return null;
        }
    }

    private void checkAdminPermission() {
        if (m_checkSecurity) {
            SecurityManager securityManager = System.getSecurityManager();
            if (null != securityManager) {
                if (null == m_adminPermission) {
                    m_adminPermission = new UserAdminPermission(UserAdminPermission.ADMIN, null);
                }
                securityManager.checkPermission(m_adminPermission);
            }
        }
    }

    // ManagedService interface

    public void updated(Dictionary properties) throws ConfigurationException {
        // defaults
        m_checkSecurity = true;
        //
        if (null !=properties) {
            String checkSecurity = (String) properties.get(PROP_SECURITY);
            if (null != checkSecurity) {
                m_checkSecurity =    "yes".equalsIgnoreCase(checkSecurity)
                                  || "true".equalsIgnoreCase(checkSecurity);
            }
        }
    }

    // UserAdmin interface

    public Role createRole(String name, int type) {
        if (name == null) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_NAME);
        }
        if (   (type != org.osgi.service.useradmin.Role.GROUP)
            && (type != org.osgi.service.useradmin.Role.USER)) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_ROLE_TYPE);
        }
        if (null != getRole(name)) {
            logMessage(this, "role already exists: " + name, LogService.LOG_WARNING);
            return null;
        }
        checkAdminPermission();
        //
        try {
            StorageProvider storageProvider = getStorageProvider();
            Role role = null;
            switch (type) {
                case org.osgi.service.useradmin.Role.USER:
                    role = storageProvider.createUser(this, name);
                    break;

                case org.osgi.service.useradmin.Role.GROUP:
                    role = storageProvider.createGroup(this, name);
                    break;

                default:
                    // never reached b/o previous checks
                    break;
            }
            fireEvent(UserAdminEvent.ROLE_CREATED, role);
            return role;
        } catch (StorageException e) {
            logMessage(this, "error when creating role: ", LogService.LOG_ERROR);
        }
        return null;
    }

    public Authorization getAuthorization(User user) {
        String userName = User.USER_ANYONE;
        if (null != user) {
            userName = user.getName();
        }
        try {
            StorageProvider storage = getStorageProvider();
            Collection<String> roleData = storage.getImpliedRoles(userName);
            String[] roles = (String[]) Collections.unmodifiableCollection(roleData).toArray();
            AuthorizationImpl authorization = new AuthorizationImpl(user.getName(), roles);
            return authorization;
        } catch (StorageException e) {
            logMessage(this, "error when authorizing user: " + e.getMessage(), LogService.LOG_ERROR);
        }
        return null;
    }

    public Role getRole(String name) {
        if (name == null) {
            throw (new IllegalArgumentException(UserAdminMessages.MSG_INVALID_NAME));
        }
        //
        try {
            StorageProvider storage = getStorageProvider();
            return storage.getRole(this, name);
        } catch (StorageException e) {
            logMessage(this, "error when looking up role: " + e.getMessage(), LogService.LOG_ERROR);
        }
        return null;
    }

    public Role[] getRoles(String filter) throws InvalidSyntaxException {
        if (filter == null || filter.equals("")) {
            filter = "*"; // all roles filter
        }
        try {
            StorageProvider storage = getStorageProvider();
            Collection<Role> roles = storage.findRoles(this, filter);
            if (!roles.isEmpty()) {
                return (Role[]) Collections.unmodifiableCollection(roles).toArray();
            }
        } catch (StorageException e) {
            logMessage(this, "error when looking up roles: " + e.getMessage(), LogService.LOG_ERROR);
        }
        return null;
    }

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
            logMessage(this,   "error when looking up user with attribute '" + key + "' = '" + value
                             + "': " + e.getMessage(), LogService.LOG_ERROR);
        }
        return null;
    }

    public boolean removeRole(String name) {
        Role role = getRole(name);
        if (null == role) {
            return false;
        }
        checkAdminPermission();
        try {
            StorageProvider storageProvider = getStorageProvider();
            storageProvider.deleteRole(role);
            fireEvent(UserAdminEvent.ROLE_REMOVED, role);
            return true;
        } catch (StorageException e) {
            logMessage(this, "error when deleting role: " + e.getMessage(), LogService.LOG_ERROR);
        }
        return false;
    }

    // UserAdminUtil interface

    public StorageProvider getStorageProvider() throws StorageException {
        StorageProvider storageProvider = (StorageProvider) m_storageService.getService();
        if (null == storageProvider) {
            throw new StorageException(UserAdminMessages.MSG_MISSING_STORAGE_SERVICE);
        }
        return storageProvider;
    }

    public void logMessage(Object source, String message, int level) {
        LogService log = (LogService) m_logService.getService();
        if (null != log) {
            log.log(level, "[" + source.getClass().getName() + "] " + message);
        }
    }

    public void fireEvent(int type, Role role) {
        EventAdmin eventAdmin = (EventAdmin) m_eventService.getService();
        if (null != eventAdmin) {
            ServiceReference ref = m_context.getServiceReference(UserAdmin.class.getName());
            UserAdminEvent uaEvent = new UserAdminEvent(ref, type, role);
            //
            Dictionary<String, Object> properties = new Hashtable<String, Object>();
            properties.put("event", uaEvent);
            properties.put("role", role);
            properties.put("role.name", role.getName());
            properties.put("role.type", role.getType());
            properties.put("service", uaEvent.getServiceReference());
            // TODO: properties.put("service.id",
            // m_context.getService(uaEvent.getServiceReference().getProperty(B)));
            // TODO: properties.put("service.objectClass", uaEvent);
            // TODO: properties.put("service.pid", uaEvent);
            //
            Event event = new Event(EVENT_TOPIC_PREFIX + getEventTypeName(type), properties);
            eventAdmin.postEvent(event);
        } else {
            logMessage(this, "No event service available - cannot send event of type: "
                            + getEventTypeName(type) + " for role: " + role.getName(),
                       LogService.LOG_ERROR);
        }
    }

    public void checkPermission(String name, String action) {
        if (m_checkSecurity) {
            SecurityManager securityManager = System.getSecurityManager();
            if (null != securityManager) {
                securityManager.checkPermission(new UserAdminPermission(name, action));
            }
        }
    }
    
    // UserAdminFactory interface
    
    public User createUser(String name,
                           Map<String, String> properties,
                           Map<String, String> credentials) {
        UserImpl user = new UserImpl(name, this, properties, credentials);
        return user;
    }

    public Group createGroup(String name,
                             Map<String, String> properties,
                             Map<String, String> credentials) {
        GroupImpl group = new GroupImpl(name, this, properties, credentials);
        return group;
    }
}

package org.ops4j.pax.useradmin.service.internal;

import java.util.Map;
import java.util.Hashtable;

import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.service.log.LogService;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.UserAdminEvent;

public abstract class AbstractProperties extends Hashtable {

    private static final long serialVersionUID = 1L;

    private Role              m_role           = null;

    private UserAdminUtil     m_util           = null;

    protected Role getRole() {
        return m_role;
    }

    protected UserAdminUtil getUtil() {
        return m_util;
    }

    protected abstract void store(StorageProvider storageProvider, String key, String value)
        throws StorageException;

    protected abstract void remove(StorageProvider storageProvider, String key)
        throws StorageException;

    protected abstract void clear(StorageProvider storageProvider) throws StorageException;

    public AbstractProperties(Role role, UserAdminUtil util, Map<String, String> properties) {
        if (null == role) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_ROLE);
        }
        if (null == util) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_USERADMIN);
        }
        m_role = role;
        m_util = util;
        //
        // initialize from storage
        //
        for (String key : properties.keySet()) {
            String value = (String) properties.get(key);
            super.put(key, value);
        }
    }
    
    /**
     * To be overridden by implementations that need security checks on the get
     * method(s), e.g. for access to credentials.
     * 
     * @param key The key for permission checks.
     */
    protected void checkGetPermission(String key) {
    }

    @Override
    public synchronized Object get(Object key) {
        if (!(key instanceof String)) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY_TYPE);
        }
        checkGetPermission((String) key);
        return super.get(key);
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        if (null == key) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY);
        }
        if (!(key instanceof String)) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY_TYPE);
        }
        if (!(value instanceof String || value instanceof byte[])) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_VALUE_TYPE);
        }
        try {
            StorageProvider storageProvider = m_util.getStorageProvider();
            store(storageProvider, (String) key, (String) value);
            m_util.fireEvent(UserAdminEvent.ROLE_CHANGED, m_role);
            return super.put(key, value);
        } catch (StorageException e) {
            m_util.logMessage(this, e.getMessage(), LogService.LOG_ERROR);
        }
        return null;
    }

    @Override
    public synchronized Object remove(Object key) {
        if (key == null) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY);
        }
        if (!(key instanceof String)) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY_TYPE);
        }
        try {
            StorageProvider storageProvider = m_util.getStorageProvider();
            remove(storageProvider, (String) key);
            m_util.fireEvent(UserAdminEvent.ROLE_CHANGED, m_role);
            return super.remove(key);
        } catch (StorageException e) {
            m_util.logMessage(this, e.getMessage(), LogService.LOG_ERROR);
        }
        return null;
    }

    @Override
    public synchronized void clear() {
        try {
            StorageProvider storageProvider = m_util.getStorageProvider();
            clear(storageProvider);
            m_util.fireEvent(UserAdminEvent.ROLE_CHANGED, m_role);
            super.clear();
        } catch (StorageException e) {
            m_util.logMessage(this, e.getMessage(), LogService.LOG_ERROR);
        }
    }
}

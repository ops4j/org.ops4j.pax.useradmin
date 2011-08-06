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

import java.util.Map;
import java.util.Hashtable;

import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.service.log.LogService;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.UserAdminEvent;

/**
 * Abstract base class for properties that need to synchronize and communicate
 * changes.
 * 
 * @author Matthias Kuespert
 * @since 02.07.2009
 */
@SuppressWarnings(value = "unchecked")
public abstract class AbstractProperties extends Hashtable {

    private static final long serialVersionUID = 1L;

    /**
     * The role these properties belong to.
     */
    private Role              m_role           = null;

    /**
     * The interface used to connect to the UserAdmin service.
     */
    private UserAdminUtil     m_util           = null;

    /**
     * @return The role these properties belong to.
     */
    protected Role getRole() {
        return m_role;
    }

    /**
     * @return The UserAdminTools implementation used here.
     */
    protected UserAdminUtil getUtil() {
        return m_util;
    }

    // abstract interface

    /**
     * Stores a String value.
     * 
     * @param storageProvider The StorageProvider to use.
     * @param key The key to access the value.
     * @param value The value to store with the given key.
     * @return The value that was stored.
     * @throws StorageException if an error occurs when storing the data
     */
    protected abstract Object store(StorageProvider storageProvider, String key, Object value) throws StorageException;

    /**
     * Removes an entry.
     * 
     * @param storageProvider The StorageProvider to use.
     * @param key The key to remove.
     * @throws StorageException if an error occurs when deleting the data
     */
    protected abstract void remove(StorageProvider storageProvider, String key)
    throws StorageException;

    /*
     * Activate when OSGi finally moves to Map
     * 
    protected abstract void clear(StorageProvider storageProvider) throws StorageException;
     */

    /**
     * Initializing constructor.
     *  
     * @param role The role these properties belong to.
     * @param util A UserAdmin utility interface.
     * @param properties Initial data - maybe null
     */
    protected AbstractProperties(Role role, UserAdminUtil util, Map<String, Object> properties) {
        m_role = role;
        m_util = util;
        //
        // initialize from storage
        //
        if (null != properties) {
            for (String key : properties.keySet()) {
                super.put(key, properties.get(key));
            }
        }
    }
    
    /**
     * Optionally overridden by implementations that need security checks on the get
     * method(s), e.g. for access to credentials.
     * 
     * @param key The key for permission checks.
     */
    protected void checkGetPermission(String key) {}

    /**
     * @see Hashtable#get(Object)
     */
    @Override
    public synchronized Object get(Object key) {
        if (null == key) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY);
        }
        if (!(key instanceof String)) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY_TYPE);
        }
        if ("".equals(key)) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_EMPTY_KEY);
        }
        checkGetPermission((String) key);
        return super.get(key);
    }

    /**
     * @see Hashtable#put(Object, Object)
     */
    @Override
    public synchronized Object put(Object key, Object value) {
        if (null == key) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY);
        }
        if (!(key instanceof String)) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY_TYPE);
        }
        if ("".equals(key)) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_EMPTY_KEY);
        }
        if (null == value) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_VALUE);
        }
        if (!(value instanceof String || value instanceof byte[])) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_VALUE_TYPE);
        }
        try {
            StorageProvider storageProvider = m_util.getStorageProvider();
            Object storedValue = store(storageProvider, (String) key, value);
            m_util.fireEvent(UserAdminEvent.ROLE_CHANGED, m_role);
            return super.put(key, storedValue);
        } catch (StorageException e) {
            m_util.logMessage(this, LogService.LOG_ERROR, e.getMessage());
        }
        return null;
    }

    /**
     * @see Hashtable#remove(Object)
     */
    @Override
    public synchronized Object remove(Object key) {
        if (key == null) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY);
        }
        if (!(key instanceof String)) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY_TYPE);
        }
        if ("".equals(key)) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_EMPTY_KEY);
        }
        try {
            StorageProvider storageProvider = m_util.getStorageProvider();
            remove(storageProvider, (String) key);
            m_util.fireEvent(UserAdminEvent.ROLE_CHANGED, m_role);
            return super.remove(key);
        } catch (StorageException e) {
            m_util.logMessage(this, LogService.LOG_ERROR, e.getMessage());
        }
        return null;
    }

    /**
     * @see Hashtable#clear()
     */
    @Override
    public synchronized void clear() {
        throw new IllegalStateException("AbstractProperties.clear() not yet implemented");
//        * Activate when OSGi finally moves to Map
//        * 
//        try {
//            StorageProvider storageProvider = m_util.getStorageProvider();
//            clear(storageProvider);
//            m_util.fireEvent(UserAdminEvent.ROLE_CHANGED, m_role);
//            super.clear();
//        } catch (StorageException e) {
//            m_util.logMessage(this, e.getMessage(), LogService.LOG_ERROR);
//        }
    }
}

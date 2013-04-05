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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

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
public abstract class AbstractProperties<R extends Role> extends Dictionary<String, Object> {

    private static final long               serialVersionUID = 1L;

    /**
     * The role these properties belong to.
     */
    private final R                         m_role;

    /**
     * The interface used to connect to the UserAdmin service.
     */
    private final UserAdminUtil             m_util;

    private final Hashtable<String, Object> hashtable;

    /**
     * @return The role these properties belong to.
     */
    protected R getRole() {
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
     * @param storageProvider
     *            The StorageProvider to use.
     * @param key
     *            The key to access the value.
     * @param value
     *            The value to store with the given key.
     * @return The value that was stored.
     * @throws StorageException
     *             if an error occurs when storing the data
     */
    protected abstract Object store(StorageProvider storageProvider, String key, Object value) throws StorageException;

    /**
     * Removes an entry.
     * 
     * @param storageProvider
     *            The StorageProvider to use.
     * @param key
     *            The key to remove.
     * @throws StorageException
     *             if an error occurs when deleting the data
     */
    protected abstract void remove(StorageProvider storageProvider, String key) throws StorageException;

    /**
     * Initializing constructor.
     * 
     * @param role
     *            The role these properties belong to.
     * @param util
     *            A UserAdmin utility interface.
     * @param properties
     *            Initial data - maybe null
     */
    protected AbstractProperties(R role, UserAdminUtil util, Map<String, Object> properties) {
        m_role = role;
        m_util = util;
        if (properties != null) {
            hashtable = new Hashtable<String, Object>(properties);
        } else {
            hashtable = new Hashtable<String, Object>();
        }
    }

    /**
     * Optionally overridden by implementations that need security checks on the
     * get method(s), e.g. for access to credentials.
     * 
     * @param key
     *            The key for permission checks.
     */
    protected abstract void checkGetPermission(String key);

    /**
     * @see Hashtable#get(Object)
     */
    @Override
    public synchronized Object get(Object key) {
        checkKeyValid(key);
        checkGetPermission((String) key);
        return hashtable.get(key);
    }

    protected void checkKeyValid(Object key) throws IllegalArgumentException {
        if (null == key) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY);
        }
        if (!(key instanceof String)) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_KEY_TYPE);
        }
        if ("".equals(key)) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_EMPTY_KEY);
        }
    }

    @Override
    public synchronized Object put(String key, Object value) {
        checkKeyValid(key);
        if (null == value) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_VALUE);
        }
        if (!(value instanceof String || value instanceof byte[])) {
            throw new IllegalArgumentException(UserAdminMessages.MSG_INVALID_VALUE_TYPE);
        }
        try {
            StorageProvider storageProvider = m_util.getStorageProvider();
            Object oldValue = get(key);
            Object storedValue = store(storageProvider, key, value);
            m_util.fireEvent(UserAdminEvent.ROLE_CHANGED, m_role);
            return putInternal(key, storedValue, oldValue);
        } catch (StorageException e) {
            m_util.logMessage(this, LogService.LOG_ERROR, e.getMessage());
        }
        return null;
    }

    protected Object putInternal(String key, Object storedValue, Object oldValue) {
        return hashtable.put(key, storedValue);
    }

    @Override
    public synchronized Object remove(Object key) {
        checkKeyValid(key);
        try {
            StorageProvider storageProvider = m_util.getStorageProvider();
            remove(storageProvider, (String) key);
            m_util.fireEvent(UserAdminEvent.ROLE_CHANGED, m_role);
            return hashtable.remove(key);
        } catch (StorageException e) {
            m_util.logMessage(this, LogService.LOG_ERROR, e.getMessage());
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return hashtable.isEmpty();
    }

    @Override
    public Enumeration<Object> elements() {
        return hashtable.elements();
    }

    @Override
    public Enumeration<String> keys() {
        return hashtable.keys();
    }

    @Override
    public int size() {
        return hashtable.size();
    }

}

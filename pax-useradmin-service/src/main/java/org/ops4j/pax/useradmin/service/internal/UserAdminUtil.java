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

import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.service.useradmin.Role;

/**
 * Collection of utility methods.
 * 
 * @author Matthias Kuespert
 * @since 02.07.2009
 */
public interface UserAdminUtil {

    /**
     * Checks if the caller has the specified permission.
     * 
     * @param permission The name of the permission, e.g. the name of a property
     *            to change.
     * @param credential The <code>UserAdminPermission</code> code.
     * 
     * @throws <code>SecurityException</code> If a security manager exists and
     *         the caller does not have the UserAdminPermission with name admin.
     */
    void checkPermission(String permission, String credential);

    /**
     * Provides access to the <code>StorageProvider</code> used to persist data.
     * 
     * @return An instance of the <code>StorageProvider</code> service.
     * @throws StorageException If the <code>StorageProvider</code> service is
     *             not available.
     */
    StorageProvider getStorageProvider() throws StorageException;

    /**
     * Prints the given message to the logger. If no logging service is
     * available the call is silently ignored.
     * 
     * @param source The object which sends the message.
     * @param message The message text.
     * @param level The log level.
     */
    void logMessage(Object source, String message, int level);

    /**
     * Publish an event of the given type related to the role specified.
     * 
     * @param type The type of event - see <code>UserAdminEvent</code>.
     * @param role The role which is related to the event.
     */
    void fireEvent(int type, Role role);
}

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

package org.ops4j.pax.useradmin.command.spi;

import java.util.Collection;
import java.util.Map;

import org.ops4j.pax.useradmin.command.CommandException;
import org.osgi.service.useradmin.Role;

/**
 * Interface which abstracts writing UserAdmin data.
 * 
 * @author Matthias Kuespert
 * @since 04.08.2009
 */
public interface UserAdminDataWriter {

    /**
     * Closes this writer. Writers are initially open and may relay some or all
     * data-storage actions to the close() call.
     * 
     * Writers cannot be re-opened.
     */
    void close() throws CommandException;

    /**
     * Creates a role. Changes to the returned object may not be synchronized to
     * the underlying storage.
     * 
     * @param type
     * @param name
     * @param properties
     * @param credentials
     * @return
     * @throws CommandException
     */
    Role createRole(int type,
                    String name,
                    Map<String, Object> properties,
                    Map<String, Object> credentials) throws CommandException;

    /**
     * Adds members to a role.
     *  
     * @param role
     * @param basicMembers
     * @param requiredMembers
     * @throws CommandException
     */
    void addMembers(Role role, Collection<String> basicMembers, Collection<String> requiredMembers) throws CommandException;
}

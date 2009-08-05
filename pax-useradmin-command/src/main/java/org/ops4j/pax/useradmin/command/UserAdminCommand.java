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
package org.ops4j.pax.useradmin.command;

/**
 * The public interface of the UserAdmin command.
 * 
 * @author Matthias Kuespert
 * @since  05.08.2009
 */
public interface UserAdminCommand {

    /**
     * Copies UserAdmin relevant data from the source-uri to the target-uri.
     * URI's may specify file, service or other endpoints which implement either
     * the UserAdminDataReader or UserAdminDataWriter interface.
     * 
     * @param sourceUri The URI to read data.
     * @param targetUri The URI to which data is stored.
     * @throws CommandException If source-reading or target-writing is not available.
     */
    public void copyData(String sourceUri, String targetUri)  throws CommandException;
}

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

package org.ops4j.pax.useradmin.command.internal.service;

import org.ops4j.pax.useradmin.command.CommandException;
import org.ops4j.pax.useradmin.command.spi.UserAdminDataReader;
import org.ops4j.pax.useradmin.command.spi.UserAdminDataWriter;
import org.osgi.framework.BundleContext;

/**
 * @author Matthias Kuespert
 * @since  05.08.2009
 */
public class ServiceDataReader implements UserAdminDataReader {

    public ServiceDataReader(BundleContext context) {
        
    }
    
    /**
     * @see UserAdminDataReader#copy(String, UserAdminDataWriter)
     */
    public void copy(String sourceId, UserAdminDataWriter targetWriter) throws CommandException {
        // TODO Auto-generated method stub

    }

}

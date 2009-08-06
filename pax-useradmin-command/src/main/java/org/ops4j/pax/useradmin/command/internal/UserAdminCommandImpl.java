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
package org.ops4j.pax.useradmin.command.internal;

import org.ops4j.pax.useradmin.command.CommandConstants;
import org.ops4j.pax.useradmin.command.CommandException;
import org.ops4j.pax.useradmin.command.UserAdminCommand;
import org.ops4j.pax.useradmin.command.internal.service.ServiceDataReader;
import org.ops4j.pax.useradmin.command.internal.service.ServiceDataWriter;
import org.ops4j.pax.useradmin.command.internal.xml.XMLDataReader;
import org.ops4j.pax.useradmin.command.internal.xml.XMLDataWriter;
import org.ops4j.pax.useradmin.command.spi.UserAdminDataReader;
import org.ops4j.pax.useradmin.command.spi.UserAdminDataWriter;
import org.osgi.framework.BundleContext;

/**
 * Main UserAdmin command implementation. This should eveolve to the main
 * interface to various OSGi frameworks.
 * 
 * @author Matthias Kuespert
 * @since 03.08.2009
 */
public class UserAdminCommandImpl implements UserAdminCommand {

    private static final String PROTOCOL_SUFFIX       = "://";
    private static final String PROTOCOL_FILE         = CommandConstants.PROTOCOL_FILE + PROTOCOL_SUFFIX;
    private static final String PROTOCOL_USERADMIN    = CommandConstants.PROTOCOL_USERADMIN + PROTOCOL_SUFFIX;

    private BundleContext       m_context             = null;
    
    public UserAdminCommandImpl(BundleContext context) {
        m_context = context;
    }

    /**
     * @see UserAdminCommand#copyData(String, String)
     * 
     *      TODO: we should introduce a factory pattern (dynamic plugins?) to
     *      manage UserAdminWriter/Reader implementations, shouldn't we?
     */
    public void copyData(String sourceUri, String targetUri)  throws CommandException {
        // create the writer
        //
        UserAdminDataWriter writer = null;
        if (targetUri.startsWith(PROTOCOL_USERADMIN)) {
            String targetId = targetUri.substring(PROTOCOL_USERADMIN.length());
            writer = new ServiceDataWriter(m_context, targetId);
        } else if (targetUri.startsWith(PROTOCOL_FILE)) {
            String targetId = targetUri.substring(PROTOCOL_FILE.length());
            writer = new XMLDataWriter(targetId);
        } else {
            throw new CommandException("Unsupported protocol in target URI: " + targetUri);
        }
        //
        // create the reader
        //
        UserAdminDataReader reader = null;
        String sourceId = null;
        if (sourceUri.startsWith(PROTOCOL_USERADMIN)) {
            sourceId = targetUri.substring(PROTOCOL_USERADMIN.length());
            reader = new ServiceDataReader(m_context);
        } else if (sourceUri.startsWith(PROTOCOL_FILE)) {
            sourceId = targetUri.substring(PROTOCOL_FILE.length());
            reader = new XMLDataReader();
        } else {
            throw new CommandException("Unsupported protocol in source URI: " + sourceUri);
        }
        //
        // and copy the data
        //
        reader.copy(sourceId, writer);
        writer.close();
    }
}

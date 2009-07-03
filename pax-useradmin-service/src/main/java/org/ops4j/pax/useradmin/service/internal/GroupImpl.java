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
import java.util.Collections;
import java.util.Map;

import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.service.log.LogService;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;

/**
 * 
 * @author Matthias Kuespert
 * @since  02.07.2009
 */
public class GroupImpl extends UserImpl implements Group {

    protected GroupImpl(String name,
                        UserAdminImpl admin,
                        Map<String, String> properties,
                        Map<String, String> credentials) {
        super(name, admin, properties, credentials);
    }

    public boolean addMember(Role role) {
        if (role != null) {
            try {
                StorageProvider storageProvider = getAdmin().getStorageProvider();
                storageProvider.addMember(this, role);
                return true;
            } catch (StorageException e) {
                getAdmin().logMessage(this,
                                        "error when adding basic member to group '" + getName() + "':"
                                      + e.getMessage(), LogService.LOG_ERROR);
            }
        }
        return false;
    }

    public boolean addRequiredMember(Role role) {
        if (role != null) {
            try {
                StorageProvider storageProvider = getAdmin().getStorageProvider();
                storageProvider.addRequiredMember(this, role);
                return true;
            } catch (StorageException e) {
                getAdmin().logMessage(this,
                                        "error when adding required member to group '" + getName() + "':"
                                      + e.getMessage(), LogService.LOG_ERROR);
            }
        }
        return false;
    }

    public boolean removeMember(Role role) {
        if (role != null) {
            try {
                StorageProvider storageProvider = getAdmin().getStorageProvider();
                storageProvider.removeMember(this, role);
                return true;
            } catch (StorageException e) {
                getAdmin().logMessage(this,
                                        "error when removing member from group '" + getName() + "':"
                                      + e.getMessage(), LogService.LOG_ERROR);
            }
        }
        return false;
    }

    public Role[] getMembers() {
        try {
            StorageProvider storageProvider = getAdmin().getStorageProvider();
            Collection<Role> roles = storageProvider.getMembers(getAdmin(), this);
            return (Role[]) Collections.unmodifiableCollection(roles).toArray();
        } catch (StorageException e) {
            getAdmin().logMessage(
                                  this,
                                   "error when retrieving basic members of group '" + getName() + "':"
                                  + e.getMessage(), LogService.LOG_ERROR);
        }
        return null;
    }

    public Role[] getRequiredMembers() {
        try {
            StorageProvider storageProvider = getAdmin().getStorageProvider();
            Collection<Role> roles = storageProvider.getRequiredMembers(getAdmin(), this);
            return (Role[]) Collections.unmodifiableCollection(roles).toArray();
        } catch (StorageException e) {
            getAdmin().logMessage(
                                  this,
                                    "error when retrieving required members of group '" + getName()
                                  + "':" + e.getMessage(), LogService.LOG_ERROR);
        }
        return null;
    }

    public int getType() {
        return Role.GROUP;
    }
}

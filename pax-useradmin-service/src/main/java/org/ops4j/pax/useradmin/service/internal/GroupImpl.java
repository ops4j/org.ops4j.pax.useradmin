package org.ops4j.pax.useradmin.service.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.service.log.LogService;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;

public class GroupImpl extends UserImpl implements Group {

    protected GroupImpl(String name,
                        UserAdminImpl admin,
                        Map<String, String> properties,
                        Map<String, String> credentials) {
        super(name, admin, properties, credentials);
    }

    public boolean addMember(Role role) {
        if (role == null) {
            return (false);
        }
        try {
            StorageProvider storageProvider = getAdmin().getStorageProvider();
            storageProvider.addMember(this, role);
        } catch (StorageException e) {
            getAdmin().logMessage(this,
                                  "error when adding basic member to group: " + e.getMessage(),
                                  LogService.LOG_ERROR);
            return false;
        }
        return true;
    }

    public boolean addRequiredMember(Role role) {
        if (role == null) {
            return (false);
        }
        try {
            StorageProvider storageProvider = getAdmin().getStorageProvider();
            storageProvider.addRequiredMember(this, role);
        } catch (StorageException e) {
            getAdmin().logMessage(this,
                                  "error when adding required member to group: " + e.getMessage(),
                                  LogService.LOG_ERROR);
            return false;
        }
        return true;
    }

    public boolean removeMember(Role role) {
        if (role == null) {
            return (false);
        }
        try {
            StorageProvider storageProvider = getAdmin().getStorageProvider();
            storageProvider.removeMember(this, role);
        } catch (StorageException e) {
            getAdmin().logMessage(this, "error when removing member from group: " + e.getMessage(),
                                  LogService.LOG_ERROR);
            return false;
        }
        return true;
    }

    public Role[] getMembers() {
        ArrayList<Role> roles = new ArrayList<Role>();
        try {
            StorageProvider storageProvider = getAdmin().getStorageProvider();
            Collection<String> roleNames = storageProvider.getMembers(this);
            //
            for (String roleName : roleNames) {
                Map<String, String> properties = storageProvider.getRole(roleName);
                roles.add(getAdmin().createRole(properties));
            }
        } catch (StorageException e) {
            getAdmin().logMessage(this, "error when retrieving members of group: " + e.getMessage(),
                                  LogService.LOG_ERROR);
            return null;
        }
        return (Role[]) Collections.unmodifiableCollection(roles).toArray();
    }

    public Role[] getRequiredMembers() {
        ArrayList<Role> roles = new ArrayList<Role>();
        try {
            StorageProvider storageProvider = getAdmin().getStorageProvider();
            Collection<String> roleNames = storageProvider.getRequiredMembers(this);
            //
            for (String roleName : roleNames) {
                Map<String, String> properties = storageProvider.getRole(roleName);
                roles.add(getAdmin().createRole(properties));
            }
        } catch (StorageException e) {
            getAdmin().logMessage(this, "error when retrieving members of group: " + e.getMessage(),
                                  LogService.LOG_ERROR);
            return null;
        }
        return (Role[]) Collections.unmodifiableCollection(roles).toArray();
    }

    public int getType() {
        return Role.GROUP;
    }
}

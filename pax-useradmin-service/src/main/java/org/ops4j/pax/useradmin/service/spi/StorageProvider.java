package org.ops4j.pax.useradmin.service.spi;

import java.util.Collection;
import java.util.Map;

import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/**
 * The interface used to maintain persistent UserAdmin data.
 */
public interface StorageProvider {

    // these properties are guaranteed to be set to valid values by the
    // StorageProvider implementation

    public static String PROP_ROLE_NAME = "org.ops4j.pax.useradmin.role.name";
    public static String PROP_ROLE_TYPE = "org.ops4j.pax.useradmin.role.type";

    Map<String, String> createUser(String name) throws StorageException;

    Map<String, String> createGroup(String name) throws StorageException;

    Collection<String> getMembers(Group group) throws StorageException;

    Collection<String> getRequiredMembers(Group group) throws StorageException;

    void addMember(Group group, Role role) throws StorageException;

    void addRequiredMember(Group group, Role role) throws StorageException;

    void removeMember(Group group, Role user) throws StorageException;
    
    Collection<String> getImpliedRoles(String userName);

    Map<String, String> getRoleAttributes(Role role) throws StorageException;

    void setRoleAttribute(Role role, String key, String value) throws StorageException;

    void removeRoleAttribute(Role role, String key) throws StorageException;

    void clearRoleAttributes(Role role) throws StorageException;

    Map<String, String> getUserCredentials(String userName) throws StorageException;

    void setUserCredential(User user, String key, String value) throws StorageException;

    void removeUserCredential(User user, String key) throws StorageException;

    void clearUserCredentials(User user) throws StorageException;

    // returned map must contain the standard properties PROP_ROLE_NAME and PROP_ROLE_TYPE

    Map<String, String> getRole(String name) throws StorageException;

    Collection<Map<String, String>> findRoles(String filter) throws StorageException;

    void deleteRole(Role role) throws StorageException;

    Map<String, String> getUser(String key, String value) throws StorageException;
}

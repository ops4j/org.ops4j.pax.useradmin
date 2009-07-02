package org.ops4j.pax.useradmin.service.spi;

import java.util.Collection;

import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/**
 * The interface used to maintain persistent UserAdmin data.
 */
public interface StorageProvider {

    // these role properties are guaranteed to be set to valid values by the
    // StorageProvider implementation

    public static String PROP_ROLE_NAME = "org.ops4j.pax.useradmin.role.name";
    public static String PROP_ROLE_TYPE = "org.ops4j.pax.useradmin.role.type";

    // role management
    
    User createUser(UserAdminFactory factory, String name) throws StorageException;
    Group createGroup(UserAdminFactory factory, String name) throws StorageException;
    void deleteRole(Role role) throws StorageException;

    // group management
    
    Collection<Role> getMembers(UserAdminFactory factory, Group group) throws StorageException;

    Collection<Role> getRequiredMembers(UserAdminFactory factory, Group group) throws StorageException;

    void addMember(Group group, Role role) throws StorageException;

    void addRequiredMember(Group group, Role role) throws StorageException;

    void removeMember(Group group, Role user) throws StorageException;
    
    // retrieving groups a member belongs to
    
    Collection<String> getImpliedRoles(String userName);

    // property management
    
    void setRoleAttribute(Role role, String key, String value) throws StorageException;

    void removeRoleAttribute(Role role, String key) throws StorageException;

    void clearRoleAttributes(Role role) throws StorageException;

    // credential management
    
    void setUserCredential(User user, String key, String value) throws StorageException;

    void removeUserCredential(User user, String key) throws StorageException;

    void clearUserCredentials(User user) throws StorageException;

    // getters & finders
    
    Role getRole(UserAdminFactory factory, String name) throws StorageException;
    User getUser(UserAdminFactory factory, String key, String value) throws StorageException;

    Collection<Role> findRoles(UserAdminFactory factory, String filter) throws StorageException;
}

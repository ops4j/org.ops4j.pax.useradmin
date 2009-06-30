package org.ops4j.pax.useradmin.service.internal;

import java.util.Map;

import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdminPermission;

public class UserCredentials extends AbstractProperties {

    private static final long serialVersionUID = 1L;

    public UserCredentials(User user, UserAdminUtil util, Map<String, String> properties) {
        super(user, util, properties);
    }

    private User getUser() {
        return (User) getRole();
    }

    @Override
    protected void store(StorageProvider storageProvider, String key, String value)
        throws StorageException {
        getUtil().checkPermission(key, UserAdminPermission.CHANGE_CREDENTIAL);
        storageProvider.setUserCredential(getUser(), key, value);
    }

    @Override
    protected void remove(StorageProvider storageProvider, String key) throws StorageException {
        getUtil().checkPermission(key, UserAdminPermission.CHANGE_CREDENTIAL);
        storageProvider.removeUserCredential(getUser(), key);
    }

    @Override
    protected void clear(StorageProvider storageProvider) throws StorageException {
        for (Object key : keySet()) {
            getUtil().checkPermission((String) key, UserAdminPermission.CHANGE_CREDENTIAL);
        }
        storageProvider.clearUserCredentials(getUser());
    }
    
    @Override
    protected void checkGetPermission(String key) {
        getUtil().checkPermission((String) key, UserAdminPermission.GET_CREDENTIAL);
    }
}

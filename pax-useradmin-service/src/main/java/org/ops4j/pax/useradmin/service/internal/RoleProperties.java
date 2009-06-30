package org.ops4j.pax.useradmin.service.internal;

import java.util.Map;

import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.UserAdminPermission;

public class RoleProperties extends AbstractProperties {

	private static final long serialVersionUID = 1L;

	public RoleProperties(Role role, UserAdminUtil util, Map<String, String> properties) {
		super(role, util, properties);
	}
	
	@Override
	protected void store(StorageProvider storageProvider, String key, String value) throws StorageException {
	    getUtil().checkPermission(key, UserAdminPermission.CHANGE_PROPERTY);
		storageProvider.setRoleAttribute(getRole(), key, value);
	}

	@Override
	protected void remove(StorageProvider storageProvider, String key) throws StorageException {
        getUtil().checkPermission(key, UserAdminPermission.CHANGE_PROPERTY);
		storageProvider.removeRoleAttribute(getRole(), key);
	}
	
	@Override
	protected void clear(StorageProvider storageProvider) throws StorageException {
        for (Object key : keySet()) {
            getUtil().checkPermission((String) key, UserAdminPermission.CHANGE_PROPERTY);
        }
		storageProvider.clearRoleAttributes(getRole());
	}
}

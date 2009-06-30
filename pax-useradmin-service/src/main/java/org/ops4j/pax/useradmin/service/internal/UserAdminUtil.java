package org.ops4j.pax.useradmin.service.internal;

import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.osgi.service.useradmin.Role;

public interface UserAdminUtil {

	void checkPermission(String permission, String credential);
	StorageProvider getStorageProvider() throws StorageException;
	void logMessage(Object source, String message, int level);
	void fireEvent(int type, Role role);
}

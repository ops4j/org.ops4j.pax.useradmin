package org.ops4j.pax.useradmin.service.spi;

import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.User;

public interface AuthenticationProvider {

	Authorization authenticate(User user);
}

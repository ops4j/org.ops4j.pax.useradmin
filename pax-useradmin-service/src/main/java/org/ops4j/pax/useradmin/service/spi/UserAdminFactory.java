package org.ops4j.pax.useradmin.service.spi;

import java.util.Map;

import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.User;

public interface UserAdminFactory {

    User createUser(String name,
                    Map<String, String> properties,
                    Map<String, String> credentials);

    Group createGroup(String name,
                      Map<String, String> properties,
                      Map<String, String> credentials);
}

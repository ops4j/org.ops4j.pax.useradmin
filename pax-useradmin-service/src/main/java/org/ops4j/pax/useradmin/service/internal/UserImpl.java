package org.ops4j.pax.useradmin.service.internal;

import java.util.Dictionary;
import java.util.Map;

import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

public class UserImpl extends RoleImpl implements User {

    private UserCredentials m_credentials = null;

    protected UserImpl(String name,
                       UserAdminImpl admin,
                       Map<String, String> properties,
                       Map<String, String> credentials) {
        super(name, admin, properties);
        //
        m_credentials = new UserCredentials(this, admin, credentials);
    }

    public Dictionary getCredentials() {
        return m_credentials;
    }

    public boolean hasCredential(String key, Object value) {
        if (null == key || null == value || !(value instanceof String || value instanceof byte[])) {
            return false;
        }
        //
        for (Object credential : m_credentials.keySet()) {
            if (((String) credential).equals(key)) {

                Object credentialValue = m_credentials.get(key);
                if (null != credentialValue && credentialValue.equals(value)) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public int getType() {
        return Role.USER;
    }
}

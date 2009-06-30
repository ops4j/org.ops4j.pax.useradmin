package org.ops4j.pax.useradmin.provider.ldap.internal;

import java.util.Dictionary;
import java.util.Map;

import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

// TODO: remove abstract as soon as StorageProvider interface is stable

public abstract class StorageProviderImpl implements StorageProvider, ManagedService {

	public static String PROP_LDAP_SERVER_URL = "org.ops4j.pax.user.ldap.server.url";
	public static String PROP_LDAP_ROOT_DN = "org.ops4j.pax.user.ldap.rootdn";
	public static String PROP_LDAP_ACCESS_USER = "org.ops4j.pax.user.ldap.access.user";
	public static String PROP_LDAP_ACCESS_PWD = "org.ops4j.pax.user.ldap.access.pwd";

	/**
	 * The Spring LdapTemplate which is used for access.
	 */
	private LdapTemplate m_template = null;
	
	/**
	 * Constructor.
	 */
	public StorageProviderImpl() {
	}

	public void updated(Dictionary properties) throws ConfigurationException {
		LdapContextSource ctx = new LdapContextSource();
		ctx.setUrl(getMandatoryProperty(properties,PROP_LDAP_SERVER_URL));
		ctx.setBase(getMandatoryProperty(properties,PROP_LDAP_ROOT_DN));
		ctx.setUserDn(getMandatoryProperty(properties,PROP_LDAP_ACCESS_USER));
		ctx.setPassword(getMandatoryProperty(properties,PROP_LDAP_ACCESS_PWD));
		m_template = new LdapTemplate(ctx);
	}
	
	private String getMandatoryProperty(Dictionary properties, String name) throws ConfigurationException {
		String value = (String) properties.get(name);
		if (null == value) {
			throw new ConfigurationException(name,  "no value given for property - please check the configuration");
		}
		return value;
	}
	
	private LdapTemplate getTemplate() throws StorageException {
		if (null == m_template) {
			throw new StorageException("no Ldap template available - check your configuration");
		}
		return m_template;
	}
	
	public Map<String, String> createUser(String name) throws StorageException {
	    LdapTemplate template = getTemplate();
//	    template.bind(dn, obj, attributes)
	    // TODO Auto-generated method stub
	    return null;
	}
}

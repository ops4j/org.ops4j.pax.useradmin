package org.ops4j.pax.useradmin.service.internal;

public class UserAdminMessages {

	public static String MSG_INVALID_NAME = "The 'name' parameter is not allowed to be null";
	public static String MSG_INVALID_ROLE_TYPE = "Only GROUP or USER is allowed as role type";
	public static String MSG_MISSING_STORAGE_SERVICE = "No StorageProvider service available ... check your setup";
	
	public static String MSG_INVALID_KEY = "The 'key' parameter must not be null";
	public static String MSG_INVALID_KEY_TYPE = "Only String is allowed as key for role properties";
    public static String MSG_INVALID_VALUE = "The 'value' parameter must not be null";
	public static String MSG_INVALID_VALUE_TYPE = "Only String or byte[] is allowed as value for role properties";
	public static String MSG_INVALID_ROLE = "The 'role' parameter must not be null";
	public static String MSG_INVALID_USERADMIN = "The 'userAdmin' parameter must not be null";
	public static String MSG_INVALID_STORAGE = "The 'storageProvider' parameter must not be null";

}

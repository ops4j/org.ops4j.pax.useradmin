/*
 * Copyright 2009 Matthias Kuespert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.useradmin.command.internal.xml;

/**
 * Constants used for interacting with XML documents. See UserAdminData.dtd for details.
 * 
 * @author Matthias Kuespert
 * @since 05.08.2009
 */
public interface XMLConstants {

    static String ELEMENT_ROOT                     = "UserAdminData";

    static String ELEMENT_USERS                    = "Users";
    static String ELEMENT_GROUPS                   = "Groups";
    static String ELEMENT_ROLE                     = "Role";
    static String ELEMENT_ATTRIBUTE                = "Attribute";

    static String ATTRIBUTE_NAME                   = "name";
    static String ATTRIBUTE_TYPE                   = "type";
    static String ATTRIBUTE_KEY                    = "key";
    static String ATTRIBUTE_VALUE                  = "value";

    static String ELEMENT_ATT_TYPE_PROPERTY        = "property";
    static String ELEMENT_ATT_TYPE_CREDENTIAL      = "credential";
    static String ELEMENT_ATT_TYPE_MEMBER          = "member";

    static String ELEMENT_ATT_TYPE_MEMBER_BASIC    = "basic";
    static String ELEMENT_ATT_TYPE_MEMBER_REQUIRED = "required";
}

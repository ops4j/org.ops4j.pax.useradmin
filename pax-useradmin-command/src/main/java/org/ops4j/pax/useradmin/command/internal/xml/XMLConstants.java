/*
 * Copyright 2009 OPS4J
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ops4j.pax.useradmin.command.internal.xml;

/**
 * Constants used for interacting with XML documents. See UserAdminData.dtd for details.
 */
public interface XMLConstants {

    String ELEMENT_ROOT                     = "UserAdminData";

    String ELEMENT_USERS                    = "Users";
    String ELEMENT_GROUPS                   = "Groups";
    String ELEMENT_ROLE                     = "Role";
    String ELEMENT_ATTRIBUTE                = "Attribute";

    String ATTRIBUTE_NAME                   = "name";
    String ATTRIBUTE_TYPE                   = "type";
    String ATTRIBUTE_KEY                    = "key";
    String ATTRIBUTE_VALUE                  = "value";

    String ELEMENT_ATT_TYPE_PROPERTY        = "property";
    String ELEMENT_ATT_TYPE_CREDENTIAL      = "credential";
    String ELEMENT_ATT_TYPE_MEMBER          = "member";

    String ELEMENT_ATT_TYPE_MEMBER_BASIC    = "basic";
    String ELEMENT_ATT_TYPE_MEMBER_REQUIRED = "required";
}

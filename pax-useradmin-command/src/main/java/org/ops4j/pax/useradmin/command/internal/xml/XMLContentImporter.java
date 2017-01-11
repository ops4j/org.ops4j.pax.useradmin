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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.ops4j.pax.useradmin.command.CommandException;
import org.ops4j.pax.useradmin.command.spi.UserAdminDataWriter;
import org.osgi.service.useradmin.Role;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class XMLContentImporter implements ContentHandler {

    /**
     * States while reading roles.
     */
    private enum State {
        initial, readingUser, readingGroup
    };

    private State               m_state                  = State.initial;
    private UserAdminDataWriter m_writer                 = null;
    private String              m_currentRoleName        = null;
    private Map<String, Object> m_currentProperties      = new HashMap<String, Object>();
    private Map<String, Object> m_currentCredentials     = new HashMap<String, Object>();
    private Collection<String>  m_currentBasicMembers    = new ArrayList<String>();
    private Collection<String>  m_currentRequiredMembers = new ArrayList<String>();

    protected XMLContentImporter(UserAdminDataWriter writer) {
        m_writer = writer;
    }

    /** 
     * @see ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (XMLConstants.ELEMENT_GROUPS.equals(localName)) {
            m_state = State.readingGroup;
        } else if (XMLConstants.ELEMENT_USERS.equals(localName)) {
            m_state = State.readingUser;
        } else if (XMLConstants.ELEMENT_ROLE.equals(localName)) {
            m_currentRoleName = atts.getValue(XMLConstants.ATTRIBUTE_NAME);
            m_currentProperties.clear();
            m_currentCredentials.clear();
            m_currentBasicMembers.clear();
            m_currentRequiredMembers.clear();
        } else if (XMLConstants.ELEMENT_ATTRIBUTE.equals(localName)) {
            //
            if (XMLConstants.ELEMENT_ATT_TYPE_PROPERTY.equals(atts.getValue(XMLConstants.ATTRIBUTE_TYPE))) {
                m_currentProperties.put(atts.getValue(XMLConstants.ATTRIBUTE_KEY),
                                        atts.getValue(XMLConstants.ATTRIBUTE_VALUE));
            }
            if (XMLConstants.ELEMENT_ATT_TYPE_CREDENTIAL.equals(atts.getValue(XMLConstants.ATTRIBUTE_TYPE))) {
                m_currentCredentials.put(atts.getValue(XMLConstants.ATTRIBUTE_KEY),
                                         atts.getValue(XMLConstants.ATTRIBUTE_VALUE));
            }
            if (XMLConstants.ELEMENT_ATT_TYPE_MEMBER.equals(atts.getValue(XMLConstants.ATTRIBUTE_TYPE))) {
                String memberId = atts.getValue(XMLConstants.ATTRIBUTE_KEY);
                if (XMLConstants.ELEMENT_ATT_TYPE_MEMBER_BASIC.equals(atts.getValue(XMLConstants.ATTRIBUTE_VALUE))) {
                    m_currentBasicMembers.add(memberId);
                } else if (XMLConstants.ELEMENT_ATT_TYPE_MEMBER_REQUIRED.equals(atts.getValue(XMLConstants.ATTRIBUTE_VALUE))) {
                    m_currentRequiredMembers.add(memberId);
                } else {
                    // TODO: error handling
                }
            }
        }
    }

    /** 
     * @see ContentHandler#endElement(String, String, String)
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (XMLConstants.ELEMENT_GROUPS.equals(localName)) {
            m_state = State.initial;
        } else if (XMLConstants.ELEMENT_USERS.equals(localName)) {
            m_state = State.initial;
        } else if (XMLConstants.ELEMENT_ROLE.equals(localName)) {
            //
            if (!(m_state == State.readingGroup || m_state == State.readingUser)) {
                throw new SAXException("Illegal state: not reading users or groups.");
            }
            try {
                Role role = m_writer.createRole(m_state == State.readingGroup ? Role.GROUP : Role.USER,
                                                m_currentRoleName,
                                                m_currentProperties,
                                                m_currentCredentials);
                if (null != role && m_state == State.readingGroup) {
                    m_writer.addMembers(role,
                                        m_currentBasicMembers,
                                        m_currentRequiredMembers);
                }
            } catch (CommandException e) {
                throw new SAXException("Could not create role '" + m_currentRoleName + "'", e);
            }
        }
    }

    // not implemented ...
    
    /**
     * @see ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {}

    /** 
     * @see ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException {}

    /** 
     * @see ContentHandler#endPrefixMapping(String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {}

    /** 
     * @see ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}

    /** 
     * @see ContentHandler#processingInstruction(String, String)
     */
    public void processingInstruction(String target, String data) throws SAXException {}

    /** 
     * @see ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator locator) {}

    /** 
     * @see ContentHandler#skippedEntity(String)
     */
    public void skippedEntity(String name) throws SAXException {}

    /** 
     * @see ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {}

    /** 
     * @see ContentHandler#startPrefixMapping(String, String)
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {}
}

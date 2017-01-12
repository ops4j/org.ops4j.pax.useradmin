/*
 * Copyright 2013 OPS4J
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
package org.ops4j.pax.useradmin.provider.jpa.internal.dao;

import java.io.UnsupportedEncodingException;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;

import org.osgi.service.useradmin.Role;

/**
 * This DAO represents a single {@link Role} property in the database
 */
@Embeddable
public class DBProperty {

    public static final short TYPE_STRING = 1;

    private static final short TYPE_BYTE   = 2;
    public static final byte[] EMPTY_ARRAY = new byte[0];

    @Column(name = "ckey")
    private String            key;

    @Lob
    @Column(name = "cdata")
    private byte[]            data;

    private short             type;

    /**
     * @return the current value of data
     */
    public byte[] getData() {
        if (data == null) {
            return EMPTY_ARRAY;
        }
        return data;
    }

    public String getDataAsString() {
        if (data == null) {
            return "";
        }
        try {
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param data
     *            the new value for data
     */
    public void setData(byte[] data) {
        this.type = TYPE_BYTE;
        this.data = data;
    }

    public void setData(String data) {
        try {
            this.type = TYPE_STRING;
            this.data = data.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the current value of type
     */
    public short getType() {
        return type;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DBProperty)) {
            return false;
        }
        DBProperty other = (DBProperty) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }
}

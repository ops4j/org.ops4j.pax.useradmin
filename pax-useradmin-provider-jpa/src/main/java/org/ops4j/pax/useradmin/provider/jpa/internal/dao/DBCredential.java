/**
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

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.osgi.service.useradmin.Role;

/**
 * This DAO represents a single {@link Role} property in the database
 */
@Embeddable
public class DBCredential {

    @Column(name = "ckey")
    private String key;
    @Column(name = "params")
    private byte[] algorithmParameter;
    @Column(name = "salt")
    private byte[] salt;
    private byte[] verificationBytes;
    private byte[] encryptedBytes;

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
        if (!(obj instanceof DBCredential)) {
            return false;
        }
        DBCredential other = (DBCredential) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }

    /**
     * @param key
     *            the new value for key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @param algorithmParameter
     */
    public void setParameter(byte[] algorithmParameter) {
        this.algorithmParameter = algorithmParameter;
    }

    /**
     * @param salt
     */
    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    /**
     * @return the current value of salt
     */
    public byte[] getSalt() {
        return salt;
    }

    /**
     * @return the current value of algorithmParameter
     */
    public byte[] getAlgorithmParameter() {
        return algorithmParameter;
    }

    /**
     * @param verificationBytes
     */
    public void setVerificationBytes(byte[] verificationBytes) {
        this.verificationBytes = verificationBytes;
    }

    /**
     * @param encryptedBytes
     */
    public void setData(byte[] encryptedBytes) {
        this.encryptedBytes = encryptedBytes;
    }

    /**
     * @return the current value of encryptedBytes
     */
    public byte[] getEncryptedBytes() {
        return encryptedBytes;
    }

    /**
     * @return the current value of verificationBytes
     */
    public byte[] getVerificationBytes() {
        return verificationBytes;
    }

}

/**
 * 
 */
package org.ops4j.pax.useradmin.service.spi;

/**
 * @author Christoph Läubrich
 */
public interface EncryptedValue {

    /**
     * @return a byte array representing the encrypted value, or
     *         <code>null</code> if encrypting of the value was not performed
     */
    byte[] getEncryptedBytes();

    /**
     * This is used for two purposes:
     * <ol>
     * <li>Check if two Encrypted values encode the same information without
     * decoding it (e.g. password verification)</li>
     * <li>Check if the encoded bytes where encoded differently or to check if
     * decoded values are correct (e.g. wrong password used while decoding)</li>
     * </ol>
     * 
     * @return a byte array that can be used to veryfy the encrypted value for
     */
    byte[] getVerificationBytes();

    /**
     * @return the salt used, or <code>null</code> if salt is not present
     */
    byte[] getSalt();

    /**
     * @return an array that represents parameters used to generate this
     *         encrypted value, this is used in verification and/or decryption
     *         phase to recreated used parameter values
     */
    byte[] getAlgorithmParameter();
}

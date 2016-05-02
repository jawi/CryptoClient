/* 
 * Copyright (c) 2016, Serphentas
 * All rights reserved.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/ or send a letter
 * to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */
package internal.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.SecureRandom;
import org.bouncycastle.crypto.generators.SCrypt;

/**
 * General purpose cryptographic class
 * <p>
 * Methods which are used for cryptographic yet general purpose are defined
 * here, so that the GCMCipher class only contains encryption/decryption-related
 * code.
 *
 * @author Serphentas
 */
public abstract class GPCrypto {

    private static final SecureRandom rand = new SecureRandom();
    private static final int KDF_N = (int) Math.pow(2, 19),
            KDF_p = 8,
            KDF_r = 1;

    /**
     * Generates a random array of bytes
     *
     * @param size width of the byte array
     * @return a random array of bytes, of length size
     */
    public static byte[] randomGen(int size) {
        byte[] randBytes = new byte[size];
        rand.nextBytes(randBytes);
        return randBytes;
    }

    /**
     * Fills a byte array with random values to prevent future retrieval of its
     * original state
     *
     * @param array byte array to sanitize
     * @param passCount number of passes to make
     */
    public static void sanitize(byte[] array, int passCount) {
        for (int i = 0; i < passCount; i++) {
            rand.nextBytes(array);
        }
    }

    /**
     * Overwrites a file with random bytes to prevent future retrieval of its
     * original state
     *
     * @param input file to sanitize
     * @param passCount number of passes to make
     * @throws Exception
     */
    public static void sanitize(File input, int passCount) throws Exception {
        final FileOutputStream fos = new FileOutputStream(input);

        if (input.length() > Math.pow(2, 20)) {
            final FileInputStream fis = new FileInputStream(input);
            byte[] buffer = new byte[1024];
            int r;
            while ((r = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, r);
            }
            fis.close();
        } else {
            for (int i = 0; i < passCount; i++) {
                rand.nextBytes(Files.readAllBytes(input.toPath()));
            }
        }
        fos.close();
    }

    /**
     * Hashes a given string with SHA384
     * <p>
     * The input is read as a byte array, using UTF-8 as character encoding.
     *
     * @param input message
     * @return message digest
     * @throws Exception
     */
    public static byte[] SHA384(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA384", "BC");
        return md.digest(input.getBytes("UTF-8"));
    }

    /**
     * Derives a given string (usually a password) with scrypt, using the
     * following default values:
     *
     * @param password password to derive key from
     * @param salt salt to use for this invocation
     * @param dkLen output size, in bytes
     * @return key derived from supplied password
     * @throws java.io.UnsupportedEncodingException
     */
    public static byte[] scrypt(String password, byte[] salt, int dkLen) throws UnsupportedEncodingException  {
        return SCrypt.generate(password.getBytes("UTF-8"), salt, KDF_N, KDF_p, KDF_r, dkLen);
    }
}

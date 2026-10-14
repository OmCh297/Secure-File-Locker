/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

public class KeyService {

    private static final String KEY_DIRECTORY =
            System.getProperty("user.home")
            + java.io.File.separator
            + ".securefilelocker";

    private static final Path KEY_FILE =
            Paths.get(KEY_DIRECTORY, "master.key");

    public static SecretKey getSecretKey() throws Exception {

        Files.createDirectories(KEY_FILE.getParent());

        if (Files.exists(KEY_FILE)) {
            byte[] keyBytes = Files.readAllBytes(KEY_FILE);

            if (keyBytes.length != 32) {
                throw new IOException(
                        "Invalid encryption key file. "
                        + "Key must be 32 bytes."
                );
            }

            return new SecretKeySpec(keyBytes, "AES");
        }

        KeyGenerator keyGenerator =
                KeyGenerator.getInstance("AES");

        keyGenerator.init(256);

        SecretKey secretKey = keyGenerator.generateKey();

        try {
            Files.write(
                    KEY_FILE,
                    secretKey.getEncoded(),
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            );
        } catch (java.nio.file.FileAlreadyExistsException ex) {
            // Another operation created the key first.
            byte[] keyBytes = Files.readAllBytes(KEY_FILE);

            if (keyBytes.length != 32) {
                throw new IOException("Invalid encryption key file.");
            }

            return new SecretKeySpec(keyBytes, "AES");
        }

        setOwnerOnlyPermissions();

        return secretKey;
    }

    private static void setOwnerOnlyPermissions() {

        try {
            Set<PosixFilePermission> permissions =
                    EnumSet.of(
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE
                    );

            Files.setPosixFilePermissions(
                    KEY_FILE, permissions
            );

        } catch (UnsupportedOperationException
                 | IOException ex) {
            // POSIX permissions are not supported on every OS.
        }
    }
    public static void main(String[] args) {
    try {
        javax.crypto.SecretKey key = getSecretKey();

        System.out.println("AES Key Generated/Loaded Successfully!");
        System.out.println("Key Length: "
                + key.getEncoded().length + " bytes");

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}


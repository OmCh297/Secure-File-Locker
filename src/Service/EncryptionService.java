/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.UUID;

public class EncryptionService {

    private static final String STORAGE_DIRECTORY =
            System.getProperty("user.home")
            + File.separator
            + ".securefilelocker"
            + File.separator
            + "storage";

    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    // Stores the IV generated during the latest encryption.
    private static byte[] lastGeneratedIV = new byte[0];

    public static Path encryptFile(File inputFile)
            throws Exception {

        if (inputFile == null
                || !inputFile.isFile()
                || !inputFile.canRead()) {
            throw new IOException(
                    "Selected file does not exist or cannot be read."
            );
        }

        // Get the AES-256 key from KeyService.
        SecretKey key = KeyService.getSecretKey();

        Path storagePath = Paths.get(STORAGE_DIRECTORY);
        Files.createDirectories(storagePath);

        // Generate a fresh 12-byte IV for every encryption.
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        lastGeneratedIV = iv.clone();

        Cipher cipher =
                Cipher.getInstance("AES/GCM/NoPadding");

        cipher.init(
                Cipher.ENCRYPT_MODE,
                key,
                new GCMParameterSpec(TAG_LENGTH, iv)
        );

        // Temporary file is created inside the storage folder.
        Path tempFile = Files.createTempFile(
                storagePath, "encrypt_", ".tmp"
        );

        try {
            // Read the original file.
            byte[] originalData =
                    Files.readAllBytes(inputFile.toPath());

            // Encrypt the data.
            byte[] encryptedData = cipher.doFinal(originalData);

            // Write encrypted data to the temporary file.
            Files.write(tempFile, encryptedData);

            // Verify by decrypting the saved encrypted data.
            Cipher verifyCipher =
                    Cipher.getInstance("AES/GCM/NoPadding");

            verifyCipher.init(
                    Cipher.DECRYPT_MODE,
                    key,
                    new GCMParameterSpec(TAG_LENGTH, iv)
            );

            byte[] savedEncryptedData =
                    Files.readAllBytes(tempFile);

            byte[] verifiedData =
                    verifyCipher.doFinal(savedEncryptedData);

            if (!Arrays.equals(originalData, verifiedData)) {
                throw new IOException(
                        "Encryption verification failed."
                );
            }

            // Give the encrypted file a random name.
            String encryptedName =
                    UUID.randomUUID().toString() + ".enc";

            Path encryptedFile =
                    storagePath.resolve(encryptedName);

            // Move the verified file into its final location.
            Files.move(
                    tempFile,
                    encryptedFile,
                    StandardCopyOption.ATOMIC_MOVE
            );

            return encryptedFile;

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
     
public static Path decryptFile(
        Path encryptedFile,
        Path outputFile,
        byte[] iv
) throws Exception {

    if (encryptedFile == null
            || !Files.isRegularFile(encryptedFile)
            || !Files.isReadable(encryptedFile)) {
        throw new IOException("Encrypted file cannot be read.");
    }

    if (outputFile == null || iv == null || iv.length != IV_LENGTH) {
        throw new IllegalArgumentException("Invalid output path or IV.");
    }

    if (Files.exists(outputFile)) {
        throw new IOException(
                "Output file already exists. Choose another name."
        );
    }

    Path outputDirectory = outputFile.toAbsolutePath().getParent();

    if (outputDirectory == null) {
        throw new IOException("Invalid output directory.");
    }

    Files.createDirectories(outputDirectory);

    SecretKey key = KeyService.getSecretKey();

    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

    cipher.init(
            Cipher.DECRYPT_MODE,
            key,
            new GCMParameterSpec(TAG_LENGTH, iv)
    );

    Path tempFile = Files.createTempFile(
            outputDirectory, "decrypt_", ".tmp"
    );

    try {
        try (
            java.io.InputStream in = Files.newInputStream(encryptedFile);
            java.io.OutputStream out = Files.newOutputStream(tempFile)
        ) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                byte[] decryptedChunk = cipher.update(
                        buffer, 0, bytesRead
                );

                if (decryptedChunk != null
                        && decryptedChunk.length > 0) {
                    out.write(decryptedChunk);
                }
            }

            // GCM authentication is verified here.
            // Wrong key, IV, or modified data causes failure.
            byte[] finalChunk = cipher.doFinal();

            if (finalChunk.length > 0) {
                out.write(finalChunk);
            }
        }

        // Publish the recovered file only after verification succeeds.
        Files.move(tempFile, outputFile);

        return outputFile;

    } finally {
        Files.deleteIfExists(tempFile);
    }
}

    // Return a copy of the latest IV for later database integration.
    public static byte[] getLastGeneratedIV() {
        return lastGeneratedIV.clone();
    }

    // Testing main method.
    public static void main(String[] args) {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a file to encrypt");

        int result = chooser.showOpenDialog(null);

        if (result != JFileChooser.APPROVE_OPTION) {
            System.out.println("No file selected.");
            return;
        }

        File selectedFile = chooser.getSelectedFile();

        try {
            Path encryptedFile = encryptFile(selectedFile);

            JOptionPane.showMessageDialog(
                    null,
                    "File encrypted and verified successfully!\n\n"
                    + "Original: " + selectedFile.getName()
                    + "\nEncrypted file: "
                    + encryptedFile.toAbsolutePath()
                    + "\nIV length: "
                    + getLastGeneratedIV().length + " bytes"
                    + "\n\nOriginal file has NOT been deleted."
            );

            System.out.println(
                    "Encrypted file saved at: " + encryptedFile
            );

        } catch (Exception e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    null,
                    "Encryption failed:\n" + e.getMessage(),
                    "Encryption Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}

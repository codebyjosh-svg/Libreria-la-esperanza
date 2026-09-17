package org.esperanza.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    public static String hashSHA256(String textoPlano) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashBytes =
                    digest.digest(
                            textoPlano.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder sb =
                    new StringBuilder();

            for (byte b : hashBytes) {
                sb.append(
                        String.format("%02x", b)
                );
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new RuntimeException(
                    "Error al generar hash de contraseña",
                    e
            );
        }
    }
}
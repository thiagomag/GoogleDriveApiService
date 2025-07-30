package br.com.thiagomagdalena.googledriveapiservice.utils;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class PrivateKeyUtil {

    public static PrivateKey getPrivateKeyFromString(String privateKeyPem) throws Exception {
        // Remove as marcações "BEGIN" e "END" da chave
        String privateKeyContent = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\n", "");

        // Decodifica a chave Base64
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);

        // Cria um objeto `PrivateKey` a partir dos bytes
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePrivate(keySpec);
    }
}

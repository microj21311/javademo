package com.wbzf.demo;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @Title: RSAEncrypt.java
 * @Description: RSA加解密
 */
public class RSAEncrypt {

    private final Base64.Encoder mimeEncoder = Base64.getMimeEncoder(64, new byte[]{'\r', '\n'});
    private final Base64.Encoder encoder = Base64.getEncoder();
    private final Base64.Decoder decoder = Base64.getDecoder();
    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    private static final RSAEncrypt rsa = new RSAEncrypt();

    public static RSAEncrypt getRSAEncrypt() {
        return rsa;
    }

    /**
     * 随机生成密钥对
     */
    public KeyEntity genKeyPair() {
        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(1024, new SecureRandom());
            KeyPair keyPair = keyPairGen.generateKeyPair();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            return new KeyEntity(publicKey, privateKey);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("生成密钥异常");
        }
    }

    /**
     * 从字符串中加载公钥
     *
     * @param publicKeyStr 公钥数据字符串
     */
    private RSAPublicKey getPublicKey(String publicKeyStr) {
        try {

            byte[] buffer = decoder.decode(publicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取公钥失败");
        }
    }


    /**
     * 从字符串中加载私钥
     *
     * @param privateKeyStr
     * @throws Exception
     */
    private RSAPrivateKey getPrivateKey(String privateKeyStr) {
        try {
            byte[] buffer = decoder.decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取私钥失败");
        }
    }

    /**
     * 加密过程
     *
     * @param key           公钥或私钥
     * @param plainTextData 明文数据
     * @return
     */
    private byte[] encrypt(Key key, byte[] plainTextData) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            int inputLen = plainTextData.length;
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                int offSet = 0;
                byte[] cache;
                int i = 0;
                // 对数据分段解密
                while (inputLen - offSet > 0) {
                    if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                        cache = cipher.doFinal(plainTextData, offSet, MAX_ENCRYPT_BLOCK);
                    } else {
                        cache = cipher.doFinal(plainTextData, offSet, inputLen - offSet);
                    }
                    out.write(cache, 0, cache.length);
                    offSet = ++i * MAX_ENCRYPT_BLOCK;
                }
                return out.toByteArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("加密失败");
        }
    }

    /**
     * 使用公钥加密
     *
     * @param data
     * @return base64
     */
    public String encryptByPublicKey(String publicKey, String data) {
        try {
            return new String(encoder.encode(encrypt(getPublicKey(publicKey), data.getBytes("UTF-8"))), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("加密失败");
        }
    }

    /**
     * 使用私钥加密
     *
     * @param data
     * @return base64
     */
    public String encryptByPrivateKey(String privateKey, String data) {
        try {
            return new String(encoder.encode(encrypt(getPrivateKey(privateKey), data.getBytes("UTF-8"))), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("加密失败");
        }
    }

    /**
     * 解密过程
     *
     * @param key        公钥或私钥
     * @param cipherData 密文数据
     * @return 明文
     */
    private byte[] decrypt(Key key, byte[] cipherData) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);

            int inputLen = cipherData.length;
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                int offSet = 0;
                byte[] cache;
                int i = 0;
                // 对数据分段解密
                while (inputLen - offSet > 0) {
                    if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                        cache = cipher.doFinal(cipherData, offSet, MAX_DECRYPT_BLOCK);
                    } else {
                        cache = cipher.doFinal(cipherData, offSet, inputLen - offSet);
                    }
                    out.write(cache, 0, cache.length);
                    i++;
                    offSet = i * MAX_DECRYPT_BLOCK;
                }
                return out.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("解密失败");
        }
    }

    /**
     * 私钥解密
     *
     * @param privateKey
     * @param base64Cipher
     * @return
     */
    public String decryptByPrivateKey(String privateKey, String base64Cipher) {
        try {
            return new String(decrypt(getPrivateKey(privateKey), decoder.decode(base64Cipher)), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("解密失败");
        }
    }

    /**
     * 公钥解密
     *
     * @param publicKey
     * @param base64Cipher
     * @return
     */
    public String decryptByPublicKey(String publicKey, String base64Cipher) {
        try {
            return new String(decrypt(getPublicKey(publicKey), decoder.decode(base64Cipher)), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("解密失败");
        }
    }

    /**
     * 转换为pem格式
     *
     * @param publicKey
     * @return
     */
    public String formatPublicKeyToPem(String publicKey) {
        try {
            RSAPublicKey rsaPublicKey = getPublicKey(publicKey);
            byte[] data = rsaPublicKey.getEncoded();
            String base64encoded = mimeEncoder.encodeToString(data);
            String br = System.getProperty("line.separator");
            return "-----BEGIN PUBLIC KEY-----" + br + base64encoded + br + "-----END PUBLIC KEY-----";
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("格式转换失败");
        }
    }

    public class KeyEntity {

        /**
         * 私钥
         */
        private RSAPrivateKey privateKey;

        /**
         * 公钥
         */
        private RSAPublicKey publicKey;

        private KeyEntity(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        /**
         * 获取私钥
         *
         * @return
         */
        public String getPrivateKey() {
            return encoder.encodeToString(privateKey.getEncoded());
        }

        /**
         * 获取公钥
         *
         * @return
         */
        public String getPublicKey() {
            return encoder.encodeToString(publicKey.getEncoded());
        }
    }
}

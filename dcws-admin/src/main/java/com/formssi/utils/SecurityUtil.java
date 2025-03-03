package com.formssi.utils;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SecurityUtil {

    // 生成密钥对
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // 选择密钥长度
        return keyPairGenerator.generateKeyPair();
    }

    // 加密
    public static String encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    // 解密
    public static String decrypt(String encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedData);
    }

    public static PrivateKey getPrivateKeyFromString(String privateKeyStr) throws Exception {
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    public static PublicKey getPublicKeyFromString(String publicKeyStr) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    // 签名
    public static String sign(String data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes());
        byte[] signedData = signature.sign();
        return Base64.getEncoder().encodeToString(signedData);
    }

    // 验证签名
    public static boolean verify(String data, String signedData, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes());
        byte[] signatureBytes = Base64.getDecoder().decode(signedData);
        return signature.verify(signatureBytes);
    }

    public static void main(String[] args) throws Exception {
        // 生成密钥对
/*        KeyPair keyPair = generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded()); // 将公钥编码为字符串
        String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded()); // 将私钥编码为字符串*/
        PublicKey publicKey = getPublicKeyFromString("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0fRXpkE/VFatpLqJqWpK3YdZUX8/U5WpYz7FEi+BSbvlWlmdqH+50DUHgwrngrOlQAs+s+dzyjQoeze9IlnOdkKY1qqxg4F0EVO76ZwMrBw2UcvHtRDbWGWq/L1v7kZt8lKt4M3paNYz93VRBOxOjiZcgt/gi5epNQoDd8ptSCnsKxtK35S3r2w2YeBUKYKHlIV6ihYfp32xOwkjCDrHY0pmlqGbBN0hAQUW4dTxyFZlCAd4SrYdKjccQ1fbh6OzpRzxAzdmStuPhF2yDK3MmA4SW5p/Cg6pA3QnVkWZIljJBtTaxY461fq+wroQ1GzIkQ/zDHiEPyq4AuYC11921QIDAQAB");
        PrivateKey privateKey = getPrivateKeyFromString("MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDR9FemQT9UVq2kuompakrdh1lRfz9TlaljPsUSL4FJu+VaWZ2of7nQNQeDCueCs6VACz6z53PKNCh7N70iWc52QpjWqrGDgXQRU7vpnAysHDZRy8e1ENtYZar8vW/uRm3yUq3gzelo1jP3dVEE7E6OJlyC3+CLl6k1CgN3ym1IKewrG0rflLevbDZh4FQpgoeUhXqKFh+nfbE7CSMIOsdjSmaWoZsE3SEBBRbh1PHIVmUIB3hKth0qNxxDV9uHo7OlHPEDN2ZK24+EXbIMrcyYDhJbmn8KDqkDdCdWRZkiWMkG1NrFjjrV+r7CuhDUbMiRD/MMeIQ/KrgC5gLXX3bVAgMBAAECggEACFJuYpIltixk+Wl4otHD27anAfwQhzntVJg8pbH8jQiMGKho5ePOLZd0gt62eq+vn9nyQQPhfitsF8ChWBCF02bKk4CBK1/A7IQZQWZXxqV41IXGlptvV+uLuzUWqZpHQ5QmyglCmuiARDfbuSpVGmOHeWgk7xOMKvFVkUF7H6GRtDh0elkGEH7wsz0GQoJ/IgHWQFjpwdKTbaKjmQ6C8FZvvSQa2RwTgqC4bw0n+loUPy+zCxxjukb0toLW3ZLLBk4xvxpRjRF4lRNFdr6I4ydf1vkAvAtZK6+2mjvBaSbGXtWyyPC1k6swdqBzGo3JKCLPtDfLLioLO3um2+fqrwKBgQDSlvkvh8BWvAYAX+ciaytPZUV69R1omdiJsu8Zy4zavaSmKuPclE6+5LKk1Ca151z/Oc6obr0in+TKtWLwKLDd3e9RR55R+Gm9kmLaQnr2ClhcYkp/B3w6ec4B6FaNH+d07Ax7gLqhXdU5TDJ/ktwLS2nZQvct6nFaY8tvxFy3CwKBgQD/OkzeBBUu4te1vSJ8hNeBiLSlC/j9lMMUItshEQf/lVIEYdy/l1y+BtDRfaSNs/ijlEzfoES9N3uuMY1i7c8SaktPwsQSJTpwypWrQQ7vBjqTKW382hH2UxWL+YCrAeHjgwLKNcnhgrbeIC4Nd3Fo32/vNhEUn3D7ZTU/XH21nwKBgQCJaP+htveW4MsdtXYw7DLvdIo4p/YPictUVlBTyZDYLkRgNL5H8PHM95dlnBTCPvxcgVDKcK+zBxgX+PFc+YAm1SjSJWQ14lzE2N7twdFP+AIeDfjEGJND6LS2Y+8N2NKDZX7jm2Sr5Hk8EO8mdSJlsEiZ/mshJ8fdDh7xh/RjbwKBgQCPUG1ZPXGnojj+E/YJdY6NbfYBt3dY7O+dnvTs3GNhYLdtPoZ2DshE7A7Vk3eTGjvDnsKLz7LJjR4l8i0yH9bmwEkJwJPYnI70Rs1EHIQGM7kwaVMZaFottvmiX7egTq5I0of+g7WYq42DrQ4vAaLtAIoaCIIO0njesTX1Hjp4gQKBgCJTWFleiiTnx9+B6UcvU3ZCUnrHU/qPd8jqf7pwpkeT5M/8v5gb/Ck0sc/rvgQVY0izZdFhK/ueYKTlO1wka/auy/h5CVdCJpNdeLPo75mAvjWdtCMe0C+eHv0ZfNPKXVrYjz6AJSm3/8nUn1g+ZMC16P64Wv1p7uIdEFhXDsMV");
/*        System.out.println("公钥: " + publicKeyString); // 输出公钥
        System.out.println("私钥: " + privateKeyString);*/

        // 待加密数据
        String data = "{\"empName\":\"杨昌旭\", \"empNo\":\"yangchangxun\"}";

        // 加密
        String encryptedData = encrypt(data, publicKey);
        System.out.println("Encrypted Data: " + encryptedData);

        // 签名
        String signedData = sign(data, privateKey);
        System.out.println("Signed Data: " + signedData);

        // 验证签名
        boolean isVerified = verify(data, signedData, publicKey);
        System.out.println("Signature Verified: " + isVerified);

        // 解密
        String decryptedData = decrypt(encryptedData, privateKey);
        System.out.println("Decrypted Data: " + decryptedData);
    }

}

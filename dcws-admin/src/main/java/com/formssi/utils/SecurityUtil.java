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
        keyPairGenerator.initialize(4096); // 选择密钥长度
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
//       KeyPair keyPair = generateKeyPair();
//        PublicKey publicKey = keyPair.getPublic();
//        PrivateKey privateKey = keyPair.getPrivate();
//        String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded()); // 将公钥编码为字符串
//        String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded()); // 将私钥编码为字符串*/
        PublicKey publicKey = SecurityUtil.getPublicKeyFromString("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA0hP0zJbG3+2LzdUoD/YlUXHB6WNDtqMvlhzxlEqmikvBKhpZklDsKJsYCiOQGySxKARAIPxqwE9tT5cTaODF1z5pK+JBhXqQ6tObg3S+/TRpibpQrpZLvdQ5kdPcBks1kCfljKYvqixuQ/cJXo/UMyDR3Pv85NiESqaaRJ8gnYkW2dC0PXXqGnWplZUfmCFCdBiwftVpMOLlq9z2JAq7Q6U+rFHUI5AyYDqOVDEdXQWHfBBDg91VmgwNJ4yEErSLA8LOwpBrBvZOZbGmE/YhmiYsLJCPlDFf2A2aZechZNsVK7sbqStrqntm00DpT8zSIOu/v0XkFpm5bbh36s4Ooz755QEq5IEtZOoFImHq4sOTQI8SS7f+H8ihZTUseX+m/PzHVZFIHUVHXUuhM99QYW0VSBY01LeJg3OzV/YPu1MeGoK32pvwAGgsM3/JKPZiw4hi10bmtjfQ09j3jv0S0gz8S3ewt9YqlkOyOsJVB5eEA/NoLGAbFSIozU2U39mmv+K++I32gmaaMTY6ugf/7SNtWKWizQBsgBGwhER2fUSewtbxufP9unpMMJxcXkbFbICX06mLan3DqwX+TzKnVtmSB19qFxX8cUfVAWLywdxj4lBMGAcej4SHP3jeg1StnmIFqU3a+Uww5WgPjb5OiVFQ6Ul7TO21nDR4tprVaSkCAwEAAQ==");
        PrivateKey privateKey = getPrivateKeyFromString("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");//        System.out.println("公钥: " + publicKeyString); // 输出公钥
//        System.out.println("私钥: " + privateKeyString);

        // 待加密数据
        String data = "{\"empName\":\"杨昌旭\", \"empNo\":\"yangchangxun\"}";

        // 加密
        String encryptedData = encrypt(data, publicKey);
        System.out.println("Encrypted Data: " + encryptedData);

        // 签名
        String signedData = sign(encryptedData, privateKey);
        System.out.println("Signed Data: " + signedData);

        // 验证签名
        boolean isVerified = verify(encryptedData, signedData, publicKey);
        System.out.println("Signature Verified: " + isVerified);

        // 解密
        String decryptedData = decrypt(encryptedData, privateKey);
        System.out.println("Decrypted Data: " + decryptedData);
    }

}

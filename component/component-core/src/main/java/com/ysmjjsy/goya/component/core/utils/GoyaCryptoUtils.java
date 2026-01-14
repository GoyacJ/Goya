package com.ysmjjsy.goya.component.core.utils;

import com.ysmjjsy.goya.component.core.exception.CryptoException;
import com.ysmjjsy.goya.component.core.pojo.DTO;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Random;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/7 23:43
 */
@Slf4j
@UtilityClass
public class GoyaCryptoUtils {

    private static final String PKCS8_PUBLIC_KEY_BEGIN = "-----BEGIN PUBLIC KEY-----";
    private static final String PKCS8_PUBLIC_KEY_END = "-----END PUBLIC KEY-----";
    public static final String RSA_ECB_PKCS_1_PADDING = "RSA/ECB/PKCS1Padding";
    public static final String AES_ECB_PKCS_5_PADDING = "AES/ECB/PKCS5Padding";

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public record KeyDTO(
            /*
              公钥
             */
            String publicKey,

            /*
                私钥
             */
            String privateKey
    ) implements DTO {
    }

    /* -------------------- 随机密钥生成 -------------------- */

    /**
     * 生成随机 AES 密钥（16 字符）
     *
     * @return AES 密钥（ASCII 字符串）
     */
    public static String createAesKey() {
        // 生成 16 个大写字母或数字（匹配你原来 randomStringUpper(16) 的习惯）
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random r = new SecureRandom();
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成 RSA 密钥对，并以 Base64 编码返回（privateKey: PKCS8 base64, publicKey: X509 base64 并带 PKCS8 padding）
     */
    public static KeyDTO createRsaKey() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            byte[] priv = kp.getPrivate().getEncoded();
            byte[] pub = kp.getPublic().getEncoded();

            String privB64 = Base64.getEncoder().encodeToString(priv);
            String pubB64 = Base64.getEncoder().encodeToString(pub);
            // 为兼容原来代码，返回的 publicKey 包含 PKCS8_BEGIN/END 包裹
            return new KeyDTO(appendPkcs8Padding(pubB64), privB64);
        } catch (Exception e) {
            throw new CryptoException("createRsaKey fail", e);
        }
    }

    /**
     * 生成 SM2 密钥（privateKey hex, publicKey hex (uncompressed)）
     */
    public static KeyDTO createSm2Key() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("sm2p256v1");
            kpg.initialize(ecSpec, new SecureRandom());
            KeyPair kp = kpg.generateKeyPair();
            PrivateKey priv = kp.getPrivate();
            PublicKey pub = kp.getPublic();

            // ---- 私钥 raw D ----
            BigInteger d;
            if (priv instanceof ECPrivateKey privv) {
                d = privv.getS();
            } else {
                PKCS8EncodedKeySpec pkcs8 = new PKCS8EncodedKeySpec(priv.getEncoded());
                KeyFactory kf = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
                ECPrivateKeySpec privSpec = kf.getKeySpec(kf.generatePrivate(pkcs8), ECPrivateKeySpec.class);
                d = privSpec.getS();
            }
            String privateHex = Hex.toHexString(asFixedLength(d.toByteArray(), 32));

            // ---- 公钥 uncompressed point ----
            if (!(pub instanceof ECPublicKey pubb)) {
                throw new IllegalStateException("Not EC public key");
            }
            java.security.spec.ECPoint w = pubb.getW();
            byte[] x = asFixedLength(w.getAffineX().toByteArray(), 32);
            byte[] y = asFixedLength(w.getAffineY().toByteArray(), 32);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(0x04);
            bos.write(x);
            bos.write(y);
            byte[] pubEncoded = bos.toByteArray();

            String publicHex = Hex.toHexString(pubEncoded);

            return new KeyDTO(publicHex, privateHex);
        } catch (Exception e) {
            throw new CryptoException("createSm2Key fail", e);
        }
    }

    /**
     * 生成 SM4 hex key（128-bit hex）
     */
    public static String createSm4key() {
        byte[] key = new byte[16];
        new SecureRandom().nextBytes(key);
        return Hex.toHexString(key);
    }

    /* -------------------- AES -------------------- */

    /**
     * 使用 AES/ECB/PKCS5Padding 解密，输入 data 为 Base64 编码
     *
     * @param data Base64 密文
     * @param key  AES 密钥字符串（将取 UTF-8 bytes）
     */
    public static String decryptAes(String data, String key) {
        try {
            byte[] keyBytes = toAesKeyBytes(key);
            Cipher cipher = Cipher.getInstance(AES_ECB_PKCS_5_PADDING);
            SecretKeySpec spec = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.DECRYPT_MODE, spec);
            byte[] decoded = Base64.getDecoder().decode(data);
            byte[] result = cipher.doFinal(decoded);
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException("decryptAes fail", e);
        }
    }

    /**
     * 使用 AES/ECB/PKCS5Padding 加密，返回 Base64 编码字符串
     *
     * @param data 明文
     * @param key  AES 密钥（UTF-8）
     */
    public static String encryptAes(String data, String key) {
        try {
            byte[] keyBytes = toAesKeyBytes(key);
            Cipher cipher = Cipher.getInstance(AES_ECB_PKCS_5_PADDING);
            SecretKeySpec spec = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, spec);
            byte[] result = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new CryptoException("encryptAes fail", e);
        }
    }

    private static byte[] toAesKeyBytes(String key) {
        byte[] b = key.getBytes(StandardCharsets.UTF_8);
        if (b.length == 16 || b.length == 24 || b.length == 32) {
            return b;
        }
        // pad or trim to 16
        byte[] out = new byte[16];
        System.arraycopy(b, 0, out, 0, Math.min(b.length, out.length));
        return out;
    }

    /* -------------------- RSA -------------------- */

    /**
     * 使用 RSA 私钥解密（content 为 Base64），privateKey 为 Base64 编码的 PKCS#8 私钥
     */
    public static String decryptRsa(String content, String privateKey) {
        try {
            byte[] priv = Base64.getDecoder().decode(privateKey);
            PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(priv);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey pk = kf.generatePrivate(privSpec);
            Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS_1_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, pk);
            byte[] decoded = Base64.getDecoder().decode(content);
            byte[] result = cipher.doFinal(decoded);
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException("decryptRsa fail", e);
        }
    }

    /**
     * 使用 RSA 公钥加密（publicKey 可以包含 -----BEGIN PUBLIC KEY----- 包装或只是 base64）
     * content 返回 Base64 编码密文
     */
    public static String encryptRsa(String content, String publicKey) {
        try {
            String key = removePkcs8Padding(publicKey);
            byte[] pub = Base64.getDecoder().decode(key);
            X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pub);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey p = kf.generatePublic(pubSpec);
            Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS_1_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, p);
            byte[] encryptedData = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            throw new CryptoException("encryptRsa fail", e);
        }
    }

    /* -------------------- SM2 -------------------- */

    /**
     * SM2 解密
     */
    public static String decryptSm2(String content, String privateKeyHex) {
        try {
            BigInteger d = new BigInteger(1, Hex.decode(privateKeyHex));
            var params = GMNamedCurves.getByName("sm2p256v1");
            ECDomainParameters domain = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
            ECPrivateKeyParameters priKey = new ECPrivateKeyParameters(d, domain);

            SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
            engine.init(false, priKey);

            byte[] cipherBytes = Hex.decode(content);
            byte[] dec = engine.processBlock(cipherBytes, 0, cipherBytes.length);
            return new String(dec, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException("decryptSm2 fail", e);
        }
    }

    /**
     * SM2 加密
     */
    public static String encryptSm2(String content, String publicKeyHex) {
        try {
            byte[] pubHex = Hex.decode(publicKeyHex);
            var params = GMNamedCurves.getByName("sm2p256v1");
            ECDomainParameters domain = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
            ECCurve curve = params.getCurve();
            ECPoint point = curve.decodePoint(pubHex);
            ECPublicKeyParameters pubKey = new ECPublicKeyParameters(point, domain);

            SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
            engine.init(true, new ParametersWithRandom(pubKey, new SecureRandom()));

            byte[] cipher = engine.processBlock(content.getBytes(StandardCharsets.UTF_8), 0, content.getBytes(StandardCharsets.UTF_8).length);
            return Hex.toHexString(cipher);
        } catch (Exception e) {
            throw new CryptoException("encryptSm2 fail", e);
        }
    }

    /* -------------------- SM4 -------------------- */

    /**
     * 使用 SM4 解密 content（hex），publicKey 为 hex key（16 字节 hex）
     */
    public static String decryptSm4(String content, String publicKey) {
        try {
            byte[] key = Hex.decode(publicKey);
            byte[] input = Hex.decode(content);
            byte[] out = sm4EcbDecrypt(input, key);
            return new String(out, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException("decryptSm4 fail", e);
        }
    }

    /**
     * 使用 SM4 加密 content（明文），publicKey 为 hex key（16 字节 hex），返回 hex
     */
    public static String encryptSm4(String content, String publicKey) {
        try {
            byte[] key = Hex.decode(publicKey);
            byte[] out = sm4EcbEncrypt(content.getBytes(StandardCharsets.UTF_8), key);
            return Hex.toHexString(out);
        } catch (Exception e) {
            throw new CryptoException("encryptSm4 fail", e);
        }
    }

    private static byte[] sm4EcbEncrypt(byte[] data, byte[] key) throws Exception {
        PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new SM4Engine());
        cipher.init(true, new KeyParameter(key));
        return processCipher(cipher, data);
    }

    private static byte[] sm4EcbDecrypt(byte[] data, byte[] key) throws Exception {
        PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new SM4Engine());
        cipher.init(false, new KeyParameter(key));
        return processCipher(cipher, data);
    }

    private static byte[] processCipher(PaddedBufferedBlockCipher cipher, byte[] in) throws Exception {
        int minSize = cipher.getOutputSize(in.length);
        byte[] outBuf = new byte[minSize];
        int length1 = cipher.processBytes(in, 0, in.length, outBuf, 0);
        int length2 = cipher.doFinal(outBuf, length1);
        int actualLen = length1 + length2;
        byte[] result = new byte[actualLen];
        System.arraycopy(outBuf, 0, result, 0, actualLen);
        return result;
    }

    /* -------------------- PKCS8 padding helpers -------------------- */

    /**
     * 去除 RSA 公钥包装（-----BEGIN PUBLIC KEY-----）和换行，如果没有包装则直接返回
     */
    private static String removePkcs8Padding(String key) {
        if (key == null) {
            return null;
        }
        String tmp = key.replace(PKCS8_PUBLIC_KEY_BEGIN, "")
                .replace(PKCS8_PUBLIC_KEY_END, "")
                .replaceAll("\\r?\\n", "")
                .trim();
        // 可能直接传了 base64 或者带 ----- 分割后的中间部分
        if (tmp.startsWith("-----")) {
            // split by ----- and pick middle
            String[] arr = tmp.split("-----");
            if (ArrayUtils.isNotEmpty(arr) && arr.length >= 2) {
                return arr[1].trim();
            }
        }
        return tmp;
    }

    /**
     * 将 base64 公钥字符串包装为 PEM 风格（BEGIN/END）
     */
    private static String appendPkcs8Padding(String key) {
        return PKCS8_PUBLIC_KEY_BEGIN + "\n" + key + "\n" + PKCS8_PUBLIC_KEY_END;
    }

    /* -------------------- SM3 摘要 -------------------- */

    public static String digestSm3(String content) {
        SM3Digest digest = new SM3Digest();
        byte[] in = content.getBytes(StandardCharsets.UTF_8);
        digest.update(in, 0, in.length);
        byte[] out = new byte[digest.getDigestSize()];
        digest.doFinal(out, 0);
        return Hex.toHexString(out);
    }

    /* -------------------- SM2 签名与验签 -------------------- */

    /**
     * SM2 私钥签名
     */
    public static String signSm2(String privateKeyHex, String content) {
        try {
            BigInteger d = new BigInteger(1, Hex.decode(privateKeyHex));
            var params = GMNamedCurves.getByName("sm2p256v1");
            ECDomainParameters domain = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
            ECPrivateKeyParameters priKey = new ECPrivateKeyParameters(d, domain);

            SM2Signer signer = new SM2Signer();
            signer.init(true, new ParametersWithRandom(priKey, new SecureRandom()));
            byte[] msg = content.getBytes(StandardCharsets.UTF_8);
            signer.update(msg, 0, msg.length);
            byte[] sig = signer.generateSignature();
            return Base64.getEncoder().encodeToString(sig);
        } catch (Exception e) {
            throw new CryptoException("signSm2 fail", e);
        }
    }

    /**
     * SM2 公钥验签
     */
    public static boolean verifySm2(String publicKeyHex, String content, String signature) {
        try {
            byte[] pubHex = Hex.decode(publicKeyHex);
            var params = GMNamedCurves.getByName("sm2p256v1");
            ECDomainParameters domain = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
            ECPoint point = params.getCurve().decodePoint(pubHex);
            ECPublicKeyParameters pubKey = new ECPublicKeyParameters(point, domain);

            SM2Signer signer = new SM2Signer();
            signer.init(false, pubKey);
            byte[] msg = content.getBytes(StandardCharsets.UTF_8);
            signer.update(msg, 0, msg.length);
            byte[] sig = Base64.getDecoder().decode(signature);
            return signer.verifySignature(sig);
        } catch (Exception e) {
            throw new CryptoException("verifySm2 fail", e);
        }
    }

    /* -------------------- Helper util -------------------- */

    // 确保返回数组长度为 len（在头部补 0 或截断高位）
    private static byte[] asFixedLength(byte[] src, int len) {
        if (src.length == len) {
            return src;
        }
        byte[] out = new byte[len];
        if (src.length > len) {
            System.arraycopy(src, src.length - len, out, 0, len);
        } else {
            System.arraycopy(src, 0, out, len - src.length, src.length);
        }
        return out;
    }

    private static byte[] asFixedLength(BigInteger b, int len) {
        return asFixedLength(b.toByteArray(), len);
    }

    public static SecretKey generateAesKey(final int keySize) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");

            if (keySize > 0) {
                keyGenerator.init(keySize);
            } else {
                keyGenerator.init(128);
            }

            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("AES algorithm not available", e);
        }
    }
}

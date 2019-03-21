package com.example.myapp.commons.utils;

import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.*;

public class AESUtil {
    private static final String ENCRYPT_ALGORTHM_AES = "AES";
    private static final int KEY_SIZE = 128;

    public void fileDecrypt(String sourceFile, String outputFile, String privateKey) throws IOException,GeneralSecurityException {
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(mkdirFiles(outputFile))){
            Key key = getKey(privateKey);
            Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORTHM_AES);
            cipher.init(Cipher.DECRYPT_MODE, key);

            crypt(fis, fos, cipher);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (GeneralSecurityException e) {
            throw new GeneralSecurityException("An error occurred while the file was decrypted. It is possible that the key has changed",e);
        }
    }

    /**
     * 文件加密
     * @param sourceFile
     * @param outputFile
     * @param privateKey
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void fileEncrypt(String sourceFile, String outputFile, String privateKey) throws GeneralSecurityException, IOException {
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(mkdirFiles(outputFile))){
            Key key = getKey(privateKey);
            Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORTHM_AES);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            crypt(fis, fos, cipher);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (GeneralSecurityException e){
            throw new GeneralSecurityException("An error occurred while the file was decrypted. It is possible that the key has changed",e);
        }
    }

    /**
     * 加密解密流
     * @param in 加密解密前的流
     * @param out 加密解密后的流
     * @param cipher 加密解密
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private static void crypt(InputStream in, OutputStream out, Cipher cipher) throws IOException, GeneralSecurityException {
        int blockSize = cipher.getBlockSize() * 1024;
        int outputSize = cipher.getOutputSize(blockSize);

        byte[] inBytes = new byte[blockSize];
        byte[] outBytes = new byte[outputSize];

        int inLength = 0;
        boolean more = true;
        while (more) {
            inLength = in.read(inBytes);
            if (inLength == blockSize) {
                int outLength = cipher.update(inBytes, 0, blockSize, outBytes);
                out.write(outBytes, 0, outLength);
            } else {
                more = false;
            }
        }
        if (inLength > 0)
            outBytes = cipher.doFinal(inBytes, 0, inLength);
        else
            outBytes = cipher.doFinal();
        out.write(outBytes);
    }

    /**
     * 根据filePath创建相应的目录
     * @param filePath 要创建的文件路经
     * @return
     * @throws IOException
     */
    private File mkdirFiles(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        return file;
    }

    /**
     * 生成指定字符串的密钥
     * @param secret 要生成密钥的字符串
     * @return secretKey 生成后的密钥
     * @throws GeneralSecurityException
     */
    private static Key getKey(String secret) throws GeneralSecurityException {
        KeyGenerator keyGenerator  = KeyGenerator.getInstance(ENCRYPT_ALGORTHM_AES);
        SecureRandom secureRandom = null;
        if (!StringUtils.isEmpty(secret)){
            secureRandom = new SecureRandom(secret.getBytes());
        }else{
            secureRandom = new SecureRandom();
        }
        keyGenerator.init(KEY_SIZE, secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey;
    }


}

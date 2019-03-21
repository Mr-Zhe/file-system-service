package com.example.myapp;

import com.example.myapp.commons.utils.StringUtils;
import com.example.myapp.commons.utils.ZIPUtil;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Provider;
import java.security.Security;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipTest {
    private static final Logger logger = LoggerFactory.getLogger(ZipTest.class);

    public static void main(String[] args){
        String str = "";
        System.out.println(str.getBytes());
        for (Provider provider:Security.getProviders()) {
            System.out.println(provider.getName());
        }
        File outputFile = new File("E:\\Eureka.txt");

        System.out.println(outputFile.getPath());
        System.out.println(outputFile.getAbsolutePath());

        try {
            //ZIPUtil.compress("e:\\monthUserReport_201708_5_e2862f1b4dc8a122a8bc5ae26556b0ed.xlsx");

            //ZIPUtil.decompress("e:\\monthUserReport_201708_5_e2862f1b4dc8a122a8bc5ae26556b0ed1.rar");

            //ZIPUtil.decompress("e:\\monthUserReport_201708_5_e2862f1b4dc8a122a8bc5ae26556b0ed1.rar","e:\\bbbbbbbbb\\bbbaaa");

            ZIPUtil.decompressFileForRAR("E:\\bbb.rar","E:\\bbbbbbbbb\\bbbaaa");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (RarException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*String sourceFile ="E:\\QQ截图20171124085535.png";
        String outputFile ="E:\\QQ截图20171124085535Copy.png";
        String outputFile1 ="E:\\QQ截图20171124085535Copy1.png";*/

        /*try {
            new AESUtil().fileEncrypt(sourceFile, outputFile, "Eureka");

            new AESUtil().fileDecrypt(outputFile, outputFile1, "Eureka");

        } catch (GeneralSecurityException e) {
            logger.error("文件加解密发生错误，密钥可能发生变化");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

}

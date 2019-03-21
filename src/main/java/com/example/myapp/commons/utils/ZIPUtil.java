package com.example.myapp.commons.utils;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.*;

public class ZIPUtil {
    private static final Logger logger = LoggerFactory.getLogger(ZIPUtil.class);
    private static final String EXT = ".zip";
    private static final String BASE_DIR = "";
    private static final int BUFFER_SIZE = 1024;
    private static final int START_LEN = 0;

    /**
     * 压缩文件
     *
     * @param sourceFilePath 源文件路径
     * @throws Exception
     */
    public static void compress(String sourceFilePath) {
        File sourceFile = new File(sourceFilePath);
        compress(sourceFile);
    }

    /**
     * 压缩文件
     *
     * @param sourceFilePath
     * @param outputFilePath
     */
    public static void compress(String sourceFilePath, String outputFilePath) {
        File sourceFile = new File(sourceFilePath);
        File outputFile = new File(outputFilePath);
        compress(sourceFile, outputFile);
    }

    /**
     * 压缩文件
     *
     * @param sourceFile 源文件
     */
    public static void compress(File sourceFile) {
        String fileName = sourceFile.getName();
        String parentPath = sourceFile.getParent();
        String outputFilePath = sourceFile.isDirectory() ? parentPath + fileName + EXT : parentPath + fileName.substring(0, fileName.lastIndexOf(".")) + EXT;
        File outputFile = new File(outputFilePath);
        compress(sourceFile, outputFile);
    }

    /**
     * 压缩文件
     *
     * @param sourceFile
     * @param outputFile
     */
    public static void compress(File sourceFile, File outputFile) {
        // 对输出文件做CRC32校验
        CRC32 crc32 = new CRC32();
        try (CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(outputFile), crc32);
             ZipOutputStream zos = new ZipOutputStream(cos)) {
            zos.setLevel(9);
            compress(sourceFile, zos, BASE_DIR);
            zos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {

        }
    }

    public static void compress(File[] sourceFile, File outputFile) {
        // 对输出文件做CRC32校验
        CRC32 crc32 = new CRC32();
        try (CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(outputFile), crc32);
             ZipOutputStream zos = new ZipOutputStream(cos)) {
            compressFile(sourceFile, zos, BASE_DIR);
            zos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {

        }
    }

    /**
     * 压缩文件
     *
     * @param sourceFile
     * @param zos
     * @param basePath
     * @throws Exception
     */
    private static void compress(File sourceFile, ZipOutputStream zos, String basePath) throws IOException {
        if (sourceFile.isDirectory()) {//判断是否压缩文件夹
            compressDir(sourceFile, zos, basePath);
        } else {
            compressFile(sourceFile, zos, basePath);
        }
    }

    private static void compressDir(File dir, ZipOutputStream zos, String basePath) throws IOException {
        File[] files = dir.listFiles();
        // 构建空目录
        if (files.length < 1) {
            ZipEntry entry = new ZipEntry(basePath + dir.getName() + "/");
            zos.putNextEntry(entry);
            zos.closeEntry();
        }
        for (File file : files) {
            // 递归压缩
            compress(file, zos, basePath + dir.getName() + "/");
        }
    }


    private static void compressFile(File sourceFile, ZipOutputStream zos, String dir) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile));) {
            logger.info("the name of source file is：{}", sourceFile.getName());
            ZipEntry entry = new ZipEntry(dir + sourceFile.getName());
            zos.putNextEntry(entry);
            int length;
            byte data[] = new byte[BUFFER_SIZE];
            while ((length = bis.read(data, START_LEN, BUFFER_SIZE)) != -1) {
                zos.write(data, START_LEN, length);
            }
            zos.closeEntry();
        } catch (IOException e) {
            throw e;
        }
    }

    private static void compressFile(File[] sourceFile, ZipOutputStream zos, String dir) throws IOException {
        for (int i = 0; i < sourceFile.length; i++) {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile[i]))) {
                logger.info("the name of source file is：{}", sourceFile[i].getName());
                ZipEntry entry = new ZipEntry(dir + sourceFile[i].getName());
                zos.putNextEntry(entry);
                int length;
                byte data[] = new byte[BUFFER_SIZE];
                while ((length = bis.read(data, START_LEN, BUFFER_SIZE)) != -1) {
                    zos.write(data, START_LEN, length);
                }
                zos.closeEntry();
            } catch (IOException e) {
                throw e;
            }
        }
    }

    public static void decompress(String sourceFilePath) throws IOException {
        File sourceFile = new File(sourceFilePath);
        decompress(sourceFile);
    }

    public static void decompress(String sourceFilePath, String outputFilePath) throws IOException {
        File sourceFile = new File(sourceFilePath);
        File outputFile = new File(outputFilePath);
        decompress(sourceFile, outputFile);
    }

    public static void decompress(File sourceFile) throws IOException {
        String outputFilePath = sourceFile.getParent();
        File outputFile = new File(outputFilePath);
        decompress(sourceFile, outputFile);
    }

    public static void decompress(File sourceFile, File outputFile) throws IOException {
        try (CheckedInputStream cis = new CheckedInputStream(new FileInputStream(sourceFile), new CRC32());
             ZipInputStream zis = new ZipInputStream(cis)){
            decompress(outputFile, zis);
        }catch (IOException e){
            throw e;
        }
    }

    private static void decompress(File outputFile, ZipInputStream zis) throws IOException{
        ZipEntry entry = null;
        while ((entry = zis.getNextEntry()) != null) {
            // 文件
            String dir = outputFile.getPath() + File.separator + entry.getName();
            File dirFile = new File(dir);
            // 文件检查
            fileProber(dirFile);
            if (entry.isDirectory()) {
                dirFile.mkdirs();
            } else {
                decompressFile(dirFile, zis);
            }
            zis.closeEntry();
        }
    }

    private static void decompressFile(File outputFile, ZipInputStream zis) throws IOException {
        try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))){
            int length;
            byte data[] = new byte[BUFFER_SIZE];
            while ((length = zis.read(data, START_LEN, BUFFER_SIZE)) != -1) {
                bos.write(data, START_LEN, length);
            }
        }catch (IOException e){
            throw e;
        }
    }

    public static void decompressFileForRAR(String sourceFilePath, String outputFilePath) throws IOException, RarException {
        File sourceFile = new File(sourceFilePath);
        outputFilePath = StringUtils.isEmpty(outputFilePath)? sourceFile.getParent() : outputFilePath;
        FileHeader fileHeader = null;
        try(Archive archive = new Archive(sourceFile)){
            while ((fileHeader = archive.nextFileHeader()) != null){
                String compressFileName = fileHeader.getFileNameString().trim();
                String FilePath = outputFilePath + File.separator + compressFileName;
                File file = new File(FilePath);
                if(!fileHeader.isDirectory()){
                    fileProber(file);
                    try (FileOutputStream fos = new FileOutputStream(file)){
                        archive.extractFile(fileHeader, fos);
                    }catch (RarException e){
                        throw e;
                    }
                }else {
                    file.mkdirs();
                }
            }
        }catch (Exception e){
            throw e;
        }
    }

    private static void fileProber(File fileDir) {
        File parentFile = fileDir.getParentFile();
        if (!parentFile.exists()) {
            // 递归寻找上级目录
            fileProber(parentFile);
            parentFile.mkdir();
        }
    }
}

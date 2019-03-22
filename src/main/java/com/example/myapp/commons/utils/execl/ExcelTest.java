package com.example.myapp.commons.utils.execl;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

/**
 * @author ：zhangzhe
 * @description：TODO
 * @date ：Created in 2019/3/21 15:48
 * @version: $version$
 */
public class ExcelTest {
    public static void main(String[] args) {
        String filePath = "G:\\aabb.xls";
        Excel2003Reader<TestVO> reader = new Excel2003Reader();
        try {
            ExcelParseResponse response = reader.process(filePath, false, new TestRowReader());
            //String result = (String) response.getDatas().stream().collect(Collectors.joining(", "));
            System.out.println("aaa");
        } catch (Exception e) {
            e.printStackTrace();
        }
       /* File file = new File(filePath);
        try (OPCPackage pkg = OPCPackage.open(filePath)) {
            XSSFReader reader = new XSSFReader(pkg);
            InputStream sheet = reader.getSheet("rId1");
            byte[] buf = new byte[1024];
            int len;
            while ((len = sheet.read(buf)) != -1) {
                System.out.write(buf, 0, len);
            }

        } catch (InvalidFormatException | IOException e) {
            e.printStackTrace();
        } catch (OpenXML4JException e) {
            e.printStackTrace();
        }*/
       /*for (int i=0; i<10; i++){
           String filePath = "G:\\data"+i+".xlsx";
           new Thread(() -> {
               System.out.println(String.format("线程号：%s，文件路径：%s", Thread.currentThread().getName(),  filePath));
               long startTime = System.currentTimeMillis();
               Excel2007Reader<TestVO> reader = new Excel2007Reader<>();
               try {
                   ExcelParseResponse response = reader.process(filePath, new TestRowReader(), 0, true);
                   long needTime = System.currentTimeMillis() - startTime;
                   System.out.println(String.format("线程号：%s，解析数据条数：%s，耗时：%s秒",Thread.currentThread().getName(), response.getDatas().size(), (double) needTime / 1000));
               } catch (IOException e) {
                   e.printStackTrace();
               } catch (InvalidFormatException e) {
                   e.printStackTrace();
               }
           }).start();
       }*/
    }
}

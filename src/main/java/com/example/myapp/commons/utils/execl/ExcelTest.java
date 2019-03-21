package com.example.myapp.commons.utils.execl;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author ：zhangzhe
 * @description：TODO
 * @date ：Created in 2019/3/21 15:48
 * @version: $version$
 */
public class ExcelTest {
    public static void main(String[] args) {
        String filePath = "G:\\test.xlsx";
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
        Excel2007Reader<TestVO> reader = new Excel2007Reader<>();
        try {
            ExcelParseResponse response = reader.process(filePath, new TestRowReader(), 0, true);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
    }
}

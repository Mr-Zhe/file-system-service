package com.example.myapp.commons.utils.execl;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author ：zhangzhe
 * @description: 抽象Excel2007读取器，excel2007的底层数据结构是xml文件，采用SAX的事件驱动的方法解析xml，
 * 需要继承DefaultHandler，在遇到文件内容时，事件会触发，这种做法可以大大降低内存的耗费，特别使用于大数据量的文件。
 * @date ：Created in 2019/3/21 14:41
 * @version: v.1.1
 */
@Slf4j
public class Excel2007Reader<T> {
    private final String CLASS_NAME = "org.apache.xerces.parsers.SAXParser";

    private XMLReader fetchSheetParser(ExcelParseHandler parseHandler) throws SAXException {
        XMLReader parser = XMLReaderFactory.createXMLReader(CLASS_NAME);
        parser.setContentHandler(parseHandler);
        return parser;
    }

    public ExcelParseResponse<T> process(InputStream is, IRowReader rowReader, int headCount, boolean oneSheet) throws IOException, InvalidFormatException {
        try (OPCPackage pkg = OPCPackage.open(is)) {
            return doProcess(pkg, rowReader, headCount, oneSheet);
        }
    }

    /**
     * 遍历工作簿中所有的电子表格
     *
     * @param filePath  文件路径
     * @param rowReader 行数据解析器
     * @param headCount 表头开始行号
     * @param oneSheet  只读第一个sheet；
     * @return
     * @throws IOException
     */
    public ExcelParseResponse<T> process(String filePath, IRowReader rowReader, int headCount, boolean oneSheet) throws IOException, InvalidFormatException {
        try (OPCPackage pkg = OPCPackage.open(filePath)) {
            return doProcess(pkg, rowReader, headCount, oneSheet);
        }
    }

    /**
     * 遍历工作簿中所有的电子表格
     *
     * @param pkg       数据源
     * @param rowReader 行数据解析器
     * @param headCount 表头开始行号
     * @param oneSheet  只读第一个sheet；
     * @return
     * @throws IOException
     */
    private ExcelParseResponse<T> doProcess(OPCPackage pkg, IRowReader rowReader, int headCount, boolean oneSheet) {
        ExcelParseResponse response = new ExcelParseResponse();
        try {
            XSSFReader reader = new XSSFReader(pkg);
            SharedStringsTable sst = reader.getSharedStringsTable();
            ExcelParseHandler<T> parseHandler = new ExcelParseHandler(rowReader, headCount, sst);
            XMLReader parser = fetchSheetParser(parseHandler);
            Iterator<InputStream> sheets = reader.getSheetsData();
            int sheetNo = 0;
            while (sheets.hasNext()) {
                try (InputStream sheet = sheets.next()) {
                    parseHandler.setSheetNo(sheetNo);
                    parser.parse(new InputSource(sheet));
                    sheetNo++;
                }
                if (oneSheet) {
                    break;
                }
            }
            List<T> exceptionDatas = parseHandler.getExceptionDataList();
            List<T> allDatas = parseHandler.getDataList();
            if(exceptionDatas.size()>0){
                response.setDatas(exceptionDatas);
            }else {
                response.setDatas(allDatas);
                response.setDataTime(parseHandler.getRowReader().getDataTime());
            }
        } catch (SAXException | IOException | OpenXML4JException e) {
            log.error("");
            response.setMessage(e.getMessage());
            response.setDatas(Collections.emptyList());
        }
        return response;
    }
}

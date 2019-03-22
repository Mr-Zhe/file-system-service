package com.example.myapp.commons.utils.execl;

import org.apache.poi.hssf.eventusermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author ：zhangzhe
 * @description：TODO
 * @date ：Created in 2019/3/22 13:43
 * @version: $version$
 */
public class Excel2003Reader<T> {
    //总行数
    private int totalRows=0;

    /**
     * 遍历excel下所有的sheet
     * @param fileName
     * @param outputFormulaValues 是否输出公式，还是它对应的值
     * @throws Exception
     */
    public int process(String fileName, boolean outputFormulaValues) throws Exception {
        InputStream is = new FileInputStream(fileName);
        POIFSFileSystem fs = new POIFSFileSystem(is);
        ExcelParseListener listener = new ExcelParseListener();
        MissingRecordAwareHSSFListener recordListener = new MissingRecordAwareHSSFListener(listener);
        FormatTrackingHSSFListener formatListener = new FormatTrackingHSSFListener(recordListener);
        HSSFRequest request = new HSSFRequest();
        if (outputFormulaValues) {
            request.addListenerForAllRecords(formatListener);
        } else {
            EventWorkbookBuilder.SheetRecordCollectingListener workbookBuildingListener =
                    new EventWorkbookBuilder.SheetRecordCollectingListener(formatListener);
            request.addListenerForAllRecords(workbookBuildingListener);
            listener.setWorkbookBuildingListener(workbookBuildingListener);
        }

        listener.setFormatListener(formatListener);
        listener.setOutputFormulaValues(outputFormulaValues);
        HSSFEventFactory factory = new HSSFEventFactory();
        factory.processWorkbookEvents(request, fs);

        return totalRows; //返回该excel文件的总行数，不包括首列和空行
    }
}


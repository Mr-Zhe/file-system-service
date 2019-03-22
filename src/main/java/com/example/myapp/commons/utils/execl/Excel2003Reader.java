package com.example.myapp.commons.utils.execl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.hssf.eventusermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

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
    public ExcelParseResponse process(String fileName, boolean outputFormulaValues, IRowReader rowReader) throws Exception {
        ExcelParseResponse response = new ExcelParseResponse();
        InputStream is = new FileInputStream(fileName);
        POIFSFileSystem fs = new POIFSFileSystem(is);
        ExcelParseListener<T> listener = new ExcelParseListener(outputFormulaValues, rowReader);
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
        HSSFEventFactory factory = new HSSFEventFactory();
        factory.processWorkbookEvents(request, fs);
        List<T> data = listener.getDataList();
        if (!CollectionUtils.isEmpty(data)){
            response.setDatas(data);
        }
        return response;
    }
}


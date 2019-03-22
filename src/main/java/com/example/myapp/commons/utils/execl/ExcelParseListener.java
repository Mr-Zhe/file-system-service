package com.example.myapp.commons.utils.execl;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.eventusermodel.EventWorkbookBuilder;
import org.apache.poi.hssf.eventusermodel.FormatTrackingHSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ：zhangzhe
 * @description：TODO
 * @date ：Created in 2019/3/22 14:06
 * @version: v.1.1
 */
public class ExcelParseListener<T> implements HSSFListener {
    //起始行
    private int headRow = 0;

    //总行数
    private int totalRows=0;

    //上一行row的序号
    private int lastRowNumber;

    //上一单元格的序号
    private int lastColumnNumber;

    //是否输出公式，还是它对应的值
    private boolean outputFormulaValues;

    //用于转换formulas
    private EventWorkbookBuilder.SheetRecordCollectingListener workbookBuildingListener;

    //
    private FormatTrackingHSSFListener formatListener;

    //excel2003工作簿
    private HSSFWorkbook stubWorkbook;

    private SSTRecord sstRecord;

    private final HSSFDataFormatter formatter = new HSSFDataFormatter();

    //表索引
    private int sheetNo = 0;

    private String sheetName = "";

    private BoundSheetRecord[] boundSheetRecordArr;

    private List boundSheetRecords = new ArrayList<>();

    private int nextRow;

    private int nextColumn;

    private boolean outputNextStringRecord;

    //当前行
    private int curRow = 0;

    //存储一行记录所有单元格的容器
    private List<String> cellList = new ArrayList<>();

    //判断整行是否为空行的标记
    private boolean isNotNullRow = false;


    //数据读取器
    private IRowReader rowReader;

    //数据集
    private List<T> dataList = new ArrayList<>();

    public ExcelParseListener(boolean outputFormulaValues, IRowReader rowReader) {
        this.outputFormulaValues = outputFormulaValues;
        this.rowReader = rowReader;
    }

    /**
     * SSFListener 监听方法，处理Record处理每个单元格
     * @author zhangzhe
     * @date 2019/3/22 15:56
     * @param record
     * @return void
     * @version
     */
    @Override
    public void processRecord(Record record) {
        int currentRow = -1;
        int currentColumn = -1;
        String element = null;
        String value;
        switch (record.getSid()) {
            case BoundSheetRecord.sid:
                boundSheetRecords.add(record);
                break;
            case BOFRecord.sid: //开始处理每个sheet
                BOFRecord br = (BOFRecord) record;
                if (br.getType() == BOFRecord.TYPE_WORKSHEET) {
                    //如果有需要，则建立子工作簿
                    if (workbookBuildingListener != null && stubWorkbook == null) {
                        stubWorkbook = workbookBuildingListener.getStubHSSFWorkbook();
                    }
                    if (boundSheetRecordArr == null) {
                        boundSheetRecordArr = BoundSheetRecord.orderByBofPosition(boundSheetRecords);
                    }
                    sheetName = boundSheetRecordArr[sheetNo].getSheetname();
                    sheetNo++;
                }
                break;
            case SSTRecord.sid:
                sstRecord = (SSTRecord) record;
                break;
            case BlankRecord.sid: //单元格为空白
                BlankRecord blr = (BlankRecord) record;
                currentRow = blr.getRow();
                currentColumn = blr.getColumn();
                cellList.add(currentColumn, "");
                break;
            case BoolErrRecord.sid: //单元格为布尔类型
                BoolErrRecord ber = (BoolErrRecord) record;
                currentRow = ber.getRow();
                currentColumn = ber.getColumn();
                element = ber.getBooleanValue() + "";
                cellList.add(currentColumn, element);
                checkRowIsNull(element);  //如果里面某个单元格含有值，则标识该行不为空行
                break;
            case FormulaRecord.sid://单元格为公式类型
                FormulaRecord fr = (FormulaRecord) record;
                currentRow = fr.getRow();
                currentColumn = fr.getColumn();
                if (outputFormulaValues) {
                    if (Double.isNaN(fr.getValue())) {
                        outputNextStringRecord = true;
                        nextRow = fr.getRow();
                        nextColumn = fr.getColumn();
                    } else {
                        element = '"' + HSSFFormulaParser.toFormulaString(stubWorkbook, fr.getParsedExpression()) + '"';
                    }
                } else {
                    element = '"' + HSSFFormulaParser.toFormulaString(stubWorkbook, fr.getParsedExpression()) + '"';
                }
                cellList.add(currentColumn, element);
                checkRowIsNull(element);  //如果里面某个单元格含有值，则标识该行不为空行
                break;
            case StringRecord.sid: //单元格中公式的字符串
                if (outputNextStringRecord) {
                    StringRecord sr = (StringRecord) record;
                    element = sr.getString();
                    currentRow = nextRow;
                    currentColumn = nextColumn;
                    outputNextStringRecord = false;
                }
                break;
            case LabelRecord.sid:
                LabelRecord lr = (LabelRecord) record;
                currentRow = lr.getRow();
                currentColumn = lr.getColumn();
                element = lr.getValue().trim();
                cellList.add(currentColumn, element);
                checkRowIsNull(element);  //如果里面某个单元格含有值，则标识该行不为空行
                break;
            case LabelSSTRecord.sid: //单元格为字符串类型
                LabelSSTRecord lsr = (LabelSSTRecord) record;
                currentRow = lsr.getRow();
                currentColumn = lsr.getColumn();
                if (sstRecord == null) {
                    cellList.add(currentColumn, "");
                } else {
                    value = sstRecord.getString(lsr.getSSTIndex()).toString().trim();
                    value = value.equals("") ? "" : value;
                    cellList.add(currentColumn, value);
                    checkRowIsNull(value);  //如果里面某个单元格含有值，则标识该行不为空行
                }
                break;
            case NumberRecord.sid: //单元格为数字类型
                NumberRecord nr = (NumberRecord) record;
                currentRow = nr.getRow();
                currentColumn = nr.getColumn();

                //第一种方式
                //value = formatListener.formatNumberDateCell(numrec).trim();//这个被写死，采用的m/d/yy h:mm格式，不符合要求

                //第二种方式，参照formatNumberDateCell里面的实现方法编写
                Double valueDouble= nr.getValue();
                String formatString=formatListener.getFormatString(nr);
                if (formatString.contains("m/d/yy")){
                    formatString="yyyy-MM-dd hh:mm:ss";
                }
                int formatIndex=formatListener.getFormatIndex(nr);
                value=formatter.formatRawCellContents(valueDouble, formatIndex, formatString).trim();

                value = value.equals("") ? "" : value;
                //向容器加入列值
                cellList.add(currentColumn, value);
                checkRowIsNull(value);  //如果里面某个单元格含有值，则标识该行不为空行
                break;
            default:
                break;
        }

        //遇到新行的操作
        if (currentRow != -1 && currentRow != lastRowNumber) {
            lastColumnNumber = -1;
        }

        //空值的操作
        if (record instanceof MissingCellDummyRecord) {
            MissingCellDummyRecord mc = (MissingCellDummyRecord) record;
            curRow = currentRow = mc.getRow();
            currentColumn = mc.getColumn();
            cellList.add(currentColumn, "");
        }

        //更新行和列的值
        if (currentRow > -1) {
            lastRowNumber = currentRow;
            curRow = currentRow;
        }
        if (currentColumn > -1)
            lastColumnNumber = currentColumn;

        //行结束时的操作
        if (record instanceof LastCellOfRowDummyRecord) {
            if (isNotNullRow && curRow >= headRow) { //该行不为空行且该行不是第一行，发送（第一行为列名，不需要）
                Object rowData = rowReader.getRowData(sheetNo, curRow, cellList);
                if (rowData == null) {
                } else if (!StringUtils.isEmpty(rowReader.getErrorMessage())) {
                } else {
                    dataList.add((T) rowData);
                }
                //ExcelReaderUtil.sendRows(filePath, sheetName, sheetIndex, curRow + 1, cellList); //每行结束时，调用sendRows()方法
                totalRows++;
            }
            //清空容器
            cellList.clear();
            isNotNullRow = false;
            lastColumnNumber = -1;
        }
    }

    /**
     * 如果里面某个单元格含有值，则标识该行不为空行
     * @param value
     */
    private void checkRowIsNull(String value){
        if (StringUtils.isNotBlank(value)) {
            isNotNullRow = true;
        }
    }

    public boolean isOutputFormulaValues() {
        return outputFormulaValues;
    }

    public void setOutputFormulaValues(boolean outputFormulaValues) {
        this.outputFormulaValues = outputFormulaValues;
    }

    public EventWorkbookBuilder.SheetRecordCollectingListener getWorkbookBuildingListener() {
        return workbookBuildingListener;
    }

    public void setWorkbookBuildingListener(EventWorkbookBuilder.SheetRecordCollectingListener workbookBuildingListener) {
        this.workbookBuildingListener = workbookBuildingListener;
    }

    public FormatTrackingHSSFListener getFormatListener() {
        return formatListener;
    }

    public void setFormatListener(FormatTrackingHSSFListener formatListener) {
        this.formatListener = formatListener;
    }

    public List<T> getDataList() {
        return dataList;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public String getSheetName() {
        return sheetName;
    }
}

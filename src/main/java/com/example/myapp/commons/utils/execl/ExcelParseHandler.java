package com.example.myapp.commons.utils.execl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author ：zhangzhe
 * @description：TODO
 * @date ：Created in 2019/3/21 14:46
 * @version: $version$
 */
public class ExcelParseHandler<T> extends DefaultHandler {
    private int headCount = 0;
    //共享字符串表
    private SharedStringsTable sharedStringsTable;
    //上一次的内容
    private String lastContents;
    //单元格是否为Null
    private boolean isNullCell;
    //读取行的索引
    private int rowIndex = 0;
    //是否重新开始了一行
    private boolean isCurrentRow = false;
    //行数据
    private List<String> cellData = new ArrayList<>();
    //数据读取器
    private IRowReader rowReader;
    //sheet索引
    private int sheetNo;

    private boolean nextIsString;
    //上个有内容的单元格，判断空单元格
    private String lastColumn;

    //单元格数据类型，默认为字符串类型
    private CellDataType nextDataType = CellDataType.SSTINDEX;

    //单元格日期格式的索引
    private short formatIndex;

    //日期格式字符串
    private String formatString;

    private final DataFormatter formatter = new DataFormatter();

    private StylesTable stylesTable;

    //数据集
    private List<T> dataList = new ArrayList<>();
    //异常数据集
    private List<T> exceptionDataList = new ArrayList<>();

    private ObjectMapper objectMapper = new ObjectMapper();


    public ExcelParseHandler(IRowReader rowReader, int headCount, SharedStringsTable sharedStringsTable, StylesTable stylesTable) {
        this.rowReader = rowReader;
        this.headCount = headCount;
        this.sharedStringsTable = sharedStringsTable;
        this.stylesTable = stylesTable;
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        //节点的类型
        if ("row".equals(name)) {
            rowIndex = Integer.valueOf(attributes.getValue(0));
        }
        //表头的行直接跳过
        if (rowIndex <= headCount) {
            return;
        }
        isCurrentRow = true;
        // c => cell
        if ("c".equals(name)) {
            String currentColumn = attributes.getValue("r");
            int n = countNullCell(currentColumn, lastColumn);
            for (int i = 0; i < n; i++) {
                cellData.add("");
            }
            setNextDataType(attributes);
        }
        lastContents = "";
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if (rowIndex <= headCount) {
            return;
        }
        getCellData(name);
        //如果标签名称为 row ，这说明已到行尾，调用 optRows() 方法
        if ("row".equals(name)) {
            if (cellData.isEmpty()) {
                return;
            }

            Object rowData = rowReader.getRowData(sheetNo, rowIndex, cellData);
            if (rowData == null) {
            } else if (!StringUtils.isEmpty(rowReader.getErrorMessage())) {
            } else {
                dataList.add((T) rowData);
                isCurrentRow = false;
                cellData.clear();
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        //得到单元格内容的值
        lastContents += new String(ch, start, length);
    }

    /**
     * 获取单元格的值
     *
     * @param name 标签名称
     */
    public void getCellData(String name) {
        if (nextIsString) {
            /*int idx = Integer.parseInt(lastContents);
            lastContents = new XSSFRichTextString(sharedStringsTable.getEntryAt(idx)).toString();*/
            nextIsString = false;
        }
        //如果是单元格的值
        if (isCurrentRow) {
            if ("c".equals(name) && !isNullCell) {
                cellData.add(getDataValue(lastContents));
            } else if ("c".equals(name) && isNullCell) {//如果是空值
                //是新行则new一行的对象来保存一行的值
                cellData.add("");
            }
        }
        isNullCell = false;
    }

    /**
     * 计算两个单元格之间的单元格数目
     * excel2007最大行数是1048576，最大列数是16384，最后一列列名是XFD
     * @param currentColumn
     * @param lastColumn
     * @return
     */
    private int countNullCell(String currentColumn, String lastColumn) {
        if (StringUtils.isEmpty(lastColumn) || StringUtils.isEmpty(currentColumn)) {
            return 0;
        }

        //A1
        String lastCellRowIndex = lastColumn.replaceAll("[A-Z]+", "");
        //AB7
        String currentColumnTemp = currentColumn.replaceAll("\\d+", "");
        currentColumnTemp = fillChar(currentColumnTemp);
        char[] currentColumnLetters = currentColumnTemp.toCharArray();

        //  计算同一行空单元格数量，保持原逻辑
        if (rowIndex == Integer.valueOf(lastCellRowIndex)) {
            String lastColumnTemp = lastColumn.replaceAll("\\d+", "");
            lastColumnTemp = fillChar(lastColumnTemp);
            char[] lastColumnLetters = lastColumnTemp.toCharArray();
            int res = (currentColumnLetters[0] - lastColumnLetters[0]) * 26 * 26
                    + (currentColumnLetters[1] - lastColumnLetters[1]) * 26
                    + (currentColumnLetters[2] - lastColumnLetters[2]);
            return res - 1;
        } else {
            // 计算非同一行空单元格数量
            int res = (currentColumnLetters[0] - 64) * 26 * 26
                    + (currentColumnLetters[1] - 64) * 26
                    + (currentColumnLetters[2] - 64);
            return res - 1;
        }
    }

    private String fillChar(String str) {
        StringBuilder result = new StringBuilder();
        int strLength = str.length();
        int maxLen = 3;
        if (strLength < 3) {
            for (int i = 0; i < (maxLen - strLength); i++) {
                result.append('@');
            }
        }
        result.append(str);
        return result.toString();
    }

    private void setNextDataType(Attributes attributes) {
        String cellType = attributes.getValue("t");//单元格类型
        Optional<String> cellStyleOptional = Optional.ofNullable(attributes.getValue("s")); //
        String column = attributes.getValue("r"); //获取单元格的位置，如A1,B1

        isNullCell = null == cellType;
        nextIsString = StringUtils.equals("s", cellType);
        lastColumn = column;

        nextDataType = CellDataType.NUMBER; //cellType为空，则表示该单元格类型为数字
        formatIndex = -1;
        formatString = null;


        if (StringUtils.equals("b", cellType)) { //处理布尔值
            nextDataType = CellDataType.BOOL;
        } else if (StringUtils.equals("e", cellType)) {  //处理错误
            nextDataType = CellDataType.ERROR;
        } else if (StringUtils.equals("inlineStr", cellType)) {
            nextDataType = CellDataType.INLINESTR;
        } else if (StringUtils.equals("s", cellType)) { //处理字符串
            nextDataType = CellDataType.SSTINDEX;
        } else if (StringUtils.equals("str", cellType)) {
            nextDataType = CellDataType.FORMULA;
        }

        cellStyleOptional.ifPresent(cellStyleStr -> {//处理日期
            int styleIndex = Integer.parseInt(cellStyleStr);
            XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
            formatIndex = style.getDataFormat();
            formatString = style.getDataFormatString();

            if (formatString.contains("m/d/yy")) {
                nextDataType = CellDataType.DATE;
                formatString = "yyyy-MM-dd hh:mm:ss";
            }

            if (formatString == null) {
                nextDataType = CellDataType.NULL;
                formatString = BuiltinFormats.getBuiltinFormat(formatIndex);
            }
        });
    }

    private String getDataValue(String value) {
        String backValue;
        switch (nextDataType) {
            // 这几个的顺序不能随便交换，交换了很可能会导致数据错误
            case BOOL: //布尔值
                char first = value.charAt(0);
                backValue = first == '0' ? "FALSE" : "TRUE";
                break;
            case ERROR: //错误
                backValue = "\"ERROR:" + value + '"';
                break;
            case FORMULA: //公式
                backValue = '"' + value + '"';
                break;
            case INLINESTR:
                XSSFRichTextString rtsi = new XSSFRichTextString(value);
                backValue = rtsi.toString();
                break;
            case SSTINDEX: //字符串
                String sstIndex = value;
                try {
                    int idx = Integer.parseInt(sstIndex);
                    XSSFRichTextString rtss = new XSSFRichTextString(sharedStringsTable.getEntryAt(idx));//根据idx索引值获取内容值
                    backValue = rtss.toString();
                } catch (NumberFormatException ex) {
                    backValue = value;
                }
                break;
            case NUMBER: //数字
                if (formatString != null) {
                    backValue = formatter.formatRawCellContents(Double.parseDouble(value), formatIndex, formatString).trim();
                } else {
                    backValue = value;
                }
                backValue = backValue.replace("_", "").trim();
                break;
            case DATE: //日期
                backValue = formatter.formatRawCellContents(Double.parseDouble(value), formatIndex, formatString);
                // 对日期字符串作特殊处理，去掉T
                backValue = backValue.replace("T", " ");
                break;
            default:
                backValue = " ";
                break;
        }
        return backValue;
    }


    public List<T> getDataList() {
        return dataList;
    }

    public List<T> getExceptionDataList() {
        return exceptionDataList;
    }

    public int getSheetNo() {
        return sheetNo;
    }

    public void setSheetNo(int sheetNo) {
        this.sheetNo = sheetNo;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public IRowReader getRowReader() {
        return rowReader;
    }

    public void setRowReader(IRowReader rowReader) {
        this.rowReader = rowReader;
    }

    public List<String> getCellData() {
        return cellData;
    }

    public void setCellData(List<String> cellData) {
        this.cellData = cellData;
    }

    public enum CellDataType{
        BOOL, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER, NULL,DATE
    }
}

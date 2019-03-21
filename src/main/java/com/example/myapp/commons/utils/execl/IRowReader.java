package com.example.myapp.commons.utils.execl;

import java.util.List;

/**
 * @author ：zhangzhe
 * @description：TODO
 * @date ：Created in 2019/3/21 14:45
 * @version: $version$
 */
public interface IRowReader {
    Object getRowData(int sheetNo, int curRow, List<String> rowList);

    String getErrorMessage();

    void checkCellInfo(int sheetNo, int curRow, List<String> rowData);

    void setDataTime(String dataTime);

    String getDataTime();
}

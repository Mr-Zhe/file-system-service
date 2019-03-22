package com.example.myapp.commons.utils.execl;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * @author ：zhangzhe
 * @description：TODO
 * @date ：Created in 2019/3/21 15:49
 * @version: $version$
 */
public class TestRowReader implements IRowReader {
    @Override
    public Object getRowData(int sheetNo, int curRow, List<String> rowList) {
        //String data = rowList.stream().limit(4).collect(joining(","));
        //System.out.println(String.format("第%s行的数据是:%s", curRow, data));
        TestVO testVO = new TestVO();
        testVO.setName(rowList.get(0));
        testVO.setEnglishName(rowList.get(1));
        testVO.setAge(rowList.get(2));
        testVO.setDate(rowList.get(3));
        return testVO;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public void checkCellInfo(int sheetNo, int curRow, List<String> rowData) {

    }

    @Override
    public void setDataTime(String dataTime) {

    }

    @Override
    public String getDataTime() {
        return null;
    }
}

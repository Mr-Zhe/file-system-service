package com.example.myapp;

import com.example.myapp.commons.utils.execl.IRowReader;

import java.util.List;

/**
 * @author ：zhangzhe
 * @description：TODO
 * @date ：Created in 2019/4/9 13:40
 * @version: $version$
 */
public class ProductInfoReader implements IRowReader {
    @Override
    public Object getRowData(int sheetNo, int curRow, List<String> rowList) {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setBrand(rowList.get(1));
        productInfo.setName(rowList.get(2));
        productInfo.setCode(rowList.get(3));
        productInfo.setUnit(rowList.get(4));
        productInfo.setAmount(rowList.get(5));
        productInfo.setPrice(rowList.get(6));
        return productInfo;
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

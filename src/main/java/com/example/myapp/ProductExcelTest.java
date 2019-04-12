package com.example.myapp;

import com.example.myapp.commons.utils.execl.Excel2007Reader;
import com.example.myapp.commons.utils.execl.ExcelParseResponse;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;
import java.util.List;

/**
 * @author ：zhangzhe
 * @description：TODO
 * @date ：Created in 2019/4/9 13:46
 * @version: $version$
 */
public class ProductExcelTest {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
//        String filePath = "D:\\WorkDocument\\20190318\\北斗星客户表.xlsx";
        String filePath = "D:\\WorkDocument\\20190318\\北斗星客户表.xlsx";
        Excel2007Reader<ProductInfo> reader = new Excel2007Reader<>();
        try {
            ExcelParseResponse response = reader.process(filePath, new ProductInfoReader(), 1, true);
            List<ProductInfo> data = (List<ProductInfo>) response.getDatas();
            StringBuilder sb = new StringBuilder();
            sb.append("select * from supplychain.dealer where id_card in(");
            data.forEach(info -> {
                sb.append("'").append(info.getIdCard()).append("',");
                /*sb.append("UPDATE supplychain.dealer ")
                        .append("SET supplier_name = '")
                        .append(info.getSupplierName()).append("',")
                        .append("supplier_code = '")
                        .append(info.getSupplierCode()).append("',")
                        .append("supplier_address = '")
                        .append(info.getSupplierAddress()).append("',")
                        .append("supplier_mobile = '")
                        .append(info.getSupplierMobile()).append("' ")
                        .append("WHERE name = '")
                        .append(info.getName()).append("'")
                        .append("AND id_card = '")
                        .append(info.getIdCard()).append("' ")
                        .append("AND mobile = '")
                        .append(info.getMobile()).append("';");*/

            });
            System.out.println(sb.toString());
            for (ProductInfo info : data) {

            }

            long needTime = System.currentTimeMillis() - startTime;
            System.out.println(String.format("线程号：%s，解析数据条数：%s，耗时：%s秒",
                    Thread.currentThread().getName(), data.size(), (double) needTime / 1000));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
    }
}

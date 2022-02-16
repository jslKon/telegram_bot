package com.example.telegrambothungmb.utils;

import org.apache.poi.ss.usermodel.Row;

import java.util.List;

public class TestReadExcel {

    public static void main(String[] args) {
        ReadXlsxFileUtils readXlsxFileUtils = new ReadXlsxFileUtils();

        List<Row> rows = readXlsxFileUtils.readByKeyValue("src/main/resources/files/DrinkLst.xlsx");

        rows.forEach(row -> {
            System.out.println(row.getCell(0) + " " + row.getCell(1));
        });
    }
}

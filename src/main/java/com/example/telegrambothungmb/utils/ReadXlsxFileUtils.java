package com.example.telegrambothungmb.utils;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReadXlsxFileUtils implements ReadExcelFileUtils{

    @Override
    public List<Row> readByKeyValue(String filePath) {
        List<Row> rows = new ArrayList<>();
        try {
            FileInputStream file = new FileInputStream(filePath);

            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);

            sheet.forEach(rows::add);
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rows;
    }
}

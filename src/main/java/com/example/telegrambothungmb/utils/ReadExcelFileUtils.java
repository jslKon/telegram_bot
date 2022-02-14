package com.example.telegrambothungmb.utils;

import org.apache.poi.ss.usermodel.Row;

import java.util.List;

public interface ReadExcelFileUtils {

    List<Row> readByKeyValue(String filePath);
}

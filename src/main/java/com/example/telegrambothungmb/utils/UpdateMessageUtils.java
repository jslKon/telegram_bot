package com.example.telegrambothungmb.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.List;

@Component
public class UpdateMessageUtils {

    public static String CURRCONV_API_KEY = "72605790ef0e9cce9fe8";

    public static String CURRCONV_COMPACT = "ultra";

    public static List<String> getParamsFromMsg(Message msg){
        return Arrays.asList(msg.getText().split("\\s+"));
    }
}

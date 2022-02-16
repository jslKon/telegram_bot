package com.example.telegrambothungmb.bots;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RemindMeetingBotV2 {

    private static final DateTimeFormatter ddMMYYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String BOT_TOKEN = "5164507812:AAESgXS8vsF57MNEyjZUX7fDDyOK2gVznWA";
    private static final String REMIND_BOT = "RemindBot";

    public static final int PAGE_SIZE = 4;
    public static final int MAX_PAGE_SIZE = 3;
    public static final String INPUT_TIME_MESSAGE = "Nhập giờ theo format : hh:mm";
    public static final String INPUT_NOTE_MESSAGE = "Nhập note lại cho mọi người sau này nhé:";

    enum STEPS {
        CHOOSE_ROOM_STEP,
        CHOOSE_STAFF_STEP,
        CHOOSE_DATE_STEP,
        INPUT_TIME_STEP,
        INPUT_NOTE_STEP
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class RemindBotUserInfo {

        Long userId;

        RemindMeetingBot.STEPS step;

        boolean isChaining = false;

        List<User> chosenUserLst;

        String chosenTime = "";

        String chosenDate = "";

        int currentDayPage = 0;

        String userNote = "";
    }
}

package com.example.telegrambothungmb.bots;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
public class RemindMeetingBotV2 extends TelegramLongPollingBot {

    private static final String BOT_TOKEN = "5164507812:AAESgXS8vsF57MNEyjZUX7fDDyOK2gVznWA";
    private static final String REMIND_BOT = "RemindBot";


    @Override
    public void onUpdateReceived(Update update) {

    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public String getBotUsername() {
        return REMIND_BOT;
    }

    enum BOT_COMMANDS {
        ADD_CONTACT("/ADD"),
        UPDATE_CONTACT("/UPDATE"),
        SHOW_CONTACTS("/CONTACTS"),
        SHOW_CONTACT("/CONTACT"),
        REMIND("/REMIND"),
        GROUPS("/GROUPS"),
        GROUP("/GROUP");

        private final String command;

        BOT_COMMANDS(String command) {
            this.command = command;
        }
    }
}

package com.example.telegrambothungmb.bots;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Consumer;

import static com.example.telegrambothungmb.bots.MyDumpBot.BOT_COMMANDS.*;

//@Component
@Slf4j
public class MyDumpBot extends TelegramLongPollingBot {

    public static final String BOT_TOKEN = "5164507812:AAESgXS8vsF57MNEyjZUX7fDDyOK2gVznWA";
    public static final boolean IS_CHAINING = true;
    public static final String DUMP_BOT = "DumpBot";

    static Map<String, Consumer<Update>> botCmdGateWay = new HashMap<>();

    Map<Long, DumpBotUserInfo> userInfos = new HashMap<>();

    @PostConstruct
    void setUp() {
        botCmdGateWay.put(REMIND.command, this::doRemind);
        botCmdGateWay.put(SEND_QR.command, this::doSendQR);
        botCmdGateWay.put(SLEEP.command, this::doSleep);
    }

    @Override
    public String getBotUsername() {
        return DUMP_BOT;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) handleCallBackQuery(update);

        else if (update.hasMessage()) handleMessage(update);
    }

    private void handleMessage(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        if (userInfos.containsKey(userId)) botCmdGateWay.get(update.getMessage().getEntities().get(0).getText()).accept(update);
    }


    private void handleCallBackQuery(Update update) {

    }


    private void doSleep(Update update) {

        Message message = update.getMessage();

        List<MessageEntity> entities = message.getEntities();
    }

    private void doSendQR(Update update) {

        Message message = update.getMessage();

        List<MessageEntity> entities = message.getEntities();
    }

    private void doRemind(Update update) {

        Message message = update.getMessage();

        List<MessageEntity> entities = message.getEntities();

        userInfos.put(message.getFrom().getId(), new DumpBotUserInfo(1, IS_CHAINING, SEND_QR.command));

        //force reply

//        ForceReplyKeyboard replyKeyboard = new ForceReplyKeyboard();
//
//        replyKeyboard.setForceReply(true);
//
//        replyKeyboard.setSelective(true);
//
//        replyKeyboard.setInputFieldPlaceholder("Huy");


        //get list username want to
        List<List<String>> replyOptions = Arrays.asList(
                Arrays.asList("Ha", "Hung", "Huy"),
                Arrays.asList("Toan", "Huong", "Van")
        );
        //ReplyKeyboardMarkUp
        ReplyKeyboardMarkup replyKeyboardMarkup = createReplyKeyBoardMarkUp(replyOptions, "typing to cancel request");

        //InlineKeyBoardMarkup
        InlineKeyboardMarkup inlineKeyboardMarkup = createInlineKeyBoardMarkUp(replyOptions, "typing to cancel request");

        //send msg
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(String.valueOf(message.getChatId()));

        sendMessage.setParseMode(ParseMode.MARKDOWN);

        sendMessage.setText("Ban muon remind nhung ai?");

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup createInlineKeyBoardMarkUp(List<List<String>> replyOptions, String placeHolder) {

        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();

        //for each List<String> create a KeyboardRow and add it to keyBoardRows
        replyOptions.forEach(stringRow -> {

            List<InlineKeyboardButton> buttons = new ArrayList<>();

            stringRow.forEach(stringButton -> {
                buttons.add(InlineKeyboardButton.builder()
                        .text(stringButton)
                        .callbackData(stringButton)
                        .build());
            });

            inlineButtons.add(buttons);
        });

        return InlineKeyboardMarkup.builder()
                .clearKeyboard()
                .keyboard(inlineButtons)
                .build();
    }

    private ReplyKeyboardMarkup createReplyKeyBoardMarkUp(List<List<String>> replyOptions, String placeHolder) {

        //create keyboard row and keyboard buttons
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        //for each List<String> create a KeyboardRow and add it to keyBoardRows
        replyOptions.forEach(stringRow -> {

            //add all string in a list to a keyboard row
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.addAll(stringRow);

            keyboardRows.add(keyboardRow);
        });

        //create and set up setting for reply keyboard mark up;
        ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardMarkup.builder()
                .clearKeyboard()
                .inputFieldPlaceholder(placeHolder)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .keyboard(keyboardRows)
                .build();

        log.info("ReplyKeyboardMarkup : {}", replyKeyboardMarkup);

        return replyKeyboardMarkup;
    }

    //inner class
    @AllArgsConstructor
    @NoArgsConstructor
    private static class DumpBotUserInfo {

        int step;

        boolean isChaining = false;

        String commandChaining;
    }

    enum BOT_COMMANDS {
        REMIND("/REMIND"),
        SEND_QR("/SEND_QR"),
        SLEEP("/SLEEP");

        final String command;

        BOT_COMMANDS(String command) {
            this.command = command;
        }
    }
}

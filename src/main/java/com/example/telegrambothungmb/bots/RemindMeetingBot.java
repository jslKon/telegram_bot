package com.example.telegrambothungmb.bots;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static com.example.telegrambothungmb.bots.RemindMeetingBot.STEPS.*;

//@Component
@Slf4j
public class RemindMeetingBot extends TelegramLongPollingBot {

    private static final String BOT_TOKEN = "5164507812:AAESgXS8vsF57MNEyjZUX7fDDyOK2gVznWA";
    private static final boolean IS_CHAINING = true;
    private static final boolean IS_NOT_CHAINING = false;
    private static final String REMIND_BOT = "RemindBot";

    private static final DateTimeFormatter ddMMYYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final int PAGE_SIZE = 4;
    public static final int MAX_PAGE_SIZE = 3;
    public static final String INPUT_TIME_MESSAGE = "Nh???p gi??? theo format : hh:mm";
    public static final String INPUT_NOTE_MESSAGE = "Nh???p note l???i cho m???i ng?????i sau n??y nh??:";

    List<String> list7DaysLater = new ArrayList<>();

    public static final RemindBotUserInfo DUMMY_REMIND_BOT_USERINFO = RemindBotUserInfo.builder()
            .userId(-1L)
            .step(NO_STEP)
            .commandChaining("none")
            .isChaining(false)
            .build();

    Map<Long, RemindBotUserInfo> userInfos = new HashMap<>();

    Map<STEPS, BiConsumer<Update, RemindBotUserInfo>> stepMessageRouter = new HashMap<>();

    Map<STEPS, BiConsumer<Update, RemindBotUserInfo>> stepCallbackRouter = new HashMap<>();

    @Override
    public void onRegister() {
//        stepsRouter.put(FIRST_STEP, this::handleFirstStep);
        stepMessageRouter.put(SECOND_STEP, (this::handleSecondStep));
        stepMessageRouter.put(THIRD_STEP, this::handleThirdStep);
        stepMessageRouter.put(NO_STEP, this::handleNoStep);

        stepCallbackRouter.put(FIRST_STEP, this::handleCallBackFirstStep);
        stepCallbackRouter.put(SECOND_STEP, this::handleCallBackSecondStep);
        stepCallbackRouter.put(THIRD_STEP, this::handleCallBackThirdStep);
        stepCallbackRouter.put(NO_STEP, this::handleCallBackNoStep);
    }

    @Override
    public String getBotUsername() {
        return REMIND_BOT;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {

        //n???u ???? c?? callBackQuery th?? x??? l?? callBackQuery
        if (update.hasCallbackQuery()) handleCallbackQuery(update);

        else if (update.hasMessage()) {

            Long userId = update.getMessage().getFrom().getId();

            RemindBotUserInfo userInfoOrDefault = userInfos.getOrDefault(userId, DUMMY_REMIND_BOT_USERINFO);

            //n???u c?? user v?? user ??ang chaining
            if (userInfos.containsKey(userId) && userInfoOrDefault.isChaining) {

                //n???u l?? repley message
                if (update.getMessage().isReply()) {
                    if (INPUT_TIME_MESSAGE.equals(update.getMessage().getReplyToMessage().getText())) {
                        handleTimeInputMessage(update, userInfoOrDefault);
                    } else if (INPUT_NOTE_MESSAGE.equals(update.getMessage().getReplyToMessage().getText())) {
                        handleNoteInputMessage(update, userInfoOrDefault);
                    }
                } else stepMessageRouter.get(userInfoOrDefault.step).accept(update, userInfoOrDefault);
            }

            //n???u msg l?? msg c?? entities -> c?? th??? c?? command
            if (update.getMessage().hasEntities()) {
                handleMessageWithEntities(update);
            }
        }
    }

    private void handleNoteInputMessage(Update update, RemindBotUserInfo userInfo) {
        String noteInput = update.getMessage().getText();

        userInfos.replace(userInfo.userId,
                RemindBotUserInfo.builder()
                        .userId(userInfo.userId)
                        .step(userInfo.step)
                        .isChaining(userInfo.isChaining)
                        .commandChaining(userInfo.commandChaining)
                        .chosenUserLst(userInfo.chosenUserLst)
                        .chosenTime(userInfo.chosenTime)
                        .chosenDate(userInfo.chosenDate)
                        .currentDayPage(userInfo.currentDayPage)
                        .userNote(noteInput)
                        .build());

        log.info("done");

        new Thread(() -> {

        });

        userInfos.remove(userInfo.userId);
    }

    private void handleTimeInputMessage(Update update, RemindBotUserInfo userInfo) {
        String timeInput = update.getMessage().getText();

        userInfos.replace(userInfo.userId,
                RemindBotUserInfo.builder()
                        .userId(userInfo.userId)
                        .step(userInfo.step)
                        .isChaining(userInfo.isChaining)
                        .commandChaining(userInfo.commandChaining)
                        .chosenUserLst(userInfo.chosenUserLst)
                        .chosenTime(timeInput)
                        .chosenDate(userInfo.chosenDate)
                        .currentDayPage(userInfo.currentDayPage)
                        .userNote(userInfo.userNote)
                        .build());

        handleFourthStep(update, userInfo);
    }

    private void handleFourthStep(Update update, RemindBotUserInfo userInfo) {
        ForceReplyKeyboard forceReply = new ForceReplyKeyboard();
        forceReply.setForceReply(true);
        forceReply.setInputFieldPlaceholder("Nh???p note cho tin nh???n");

        SendMessage sendMessage = new SendMessage();

        sendMessage.setText(INPUT_NOTE_MESSAGE);

        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));

        sendMessage.setReplyMarkup(forceReply);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

//    private void handleUserIsChaining(Update update, RemindBotUserInfo userInfo) {
//        stepsRouter.get(userInfo.step).accept(update);
//    }

    private void handleFirstStep(Update update, RemindBotUserInfo userInfo) {

        Message message = update.getMessage();

        //get list username want to
        List<List<String>> replyOptions = Arrays.asList(
                Arrays.asList("Ha", "Hung", "Huy"),
                Arrays.asList("Toan", "Huong", "Van"),
                Arrays.asList("DONE")
        );

        //ReplyKeyboardMarkUp
        ReplyKeyboardMarkup replyKeyboardMarkup = createReplyKeyBoardMarkUp(replyOptions, "typing to cancel request");

        //InlineKeyBoardMarkup
        InlineKeyboardMarkup inlineKeyboardMarkup = createInlineKeyBoardMarkUp(replyOptions, "typing to cancel request");

        //send msg
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(String.valueOf(message.getChatId()));

        sendMessage.setParseMode(ParseMode.MARKDOWN);

        sendMessage.setText("B???n mu???n remind nh???ng ai th????");

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        try {
            executeAsync(sendMessage, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> method, Message response) {
                    log.info("log heere");
                }

                @Override
                public void onError(BotApiMethod<Message> method, TelegramApiRequestException apiException) {

                }

                @Override
                public void onException(BotApiMethod<Message> method, Exception exception) {

                }
            });
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleNoStep(Update update, RemindBotUserInfo remindBotUserInfo) {
    }

    private void handleThirdStep(Update update, RemindBotUserInfo remindBotUserInfo) {

        ForceReplyKeyboard forceReply = new ForceReplyKeyboard();
        forceReply.setForceReply(true);
        forceReply.setInputFieldPlaceholder("hh:mm");

        SendMessage sendMessage = new SendMessage();

        sendMessage.setText(INPUT_TIME_MESSAGE);

        sendMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));

        sendMessage.setReplyMarkup(forceReply);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    private void handleSecondStep(Update update, RemindBotUserInfo remindBotUserInfo) {


        log.info("stop here");

        CallbackQuery callbackQuery = update.getCallbackQuery();


        //send days
        //get today and 3 days after today
        List<String> result = new ArrayList<>();
        result.add("<<");
        IntStream.range(0, 4).forEach(i -> result.add(LocalDateTime.now().plus(i, ChronoUnit.DAYS).format(ddMMYYYY).substring(0, 5)));
        result.add(">>");


        //get list 4 days
        List<List<String>> replyOptions = new ArrayList<>();
        replyOptions.add(result);

        //InlineKeyBoardMarkup
        InlineKeyboardMarkup inlineKeyboardMarkup = createInlineKeyBoardMarkUp(replyOptions, "typing to cancel request");

        //send msg
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(String.valueOf(callbackQuery.getMessage().getChatId()));

        sendMessage.setParseMode(ParseMode.MARKDOWN);

        sendMessage.setText("Xin m???i b???n ch???n ng??y");

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        try {
            executeAsync(sendMessage, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> method, Message response) {

                }

                @Override
                public void onError(BotApiMethod<Message> method, TelegramApiRequestException apiException) {

                }

                @Override
                public void onException(BotApiMethod<Message> method, Exception exception) {

                }
            });
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleMessageWithEntities(Update update) {
        if ("/REMIND".equals(update.getMessage().getEntities().get(0).getText())) {
            Long userId = update.getMessage().getFrom().getId();
            RemindBotUserInfo newUser = RemindBotUserInfo.builder()
                    .userId(userId)
                    .isChaining(IS_CHAINING)
                    .step(FIRST_STEP)
                    .commandChaining("/REMIND")
                    .chosenUserLst(new ArrayList<>())
                    .build();
            userInfos.put(userId, newUser);
            handleFirstStep(update, newUser);
        } else handleNoStep(update, RemindMeetingBot.DUMMY_REMIND_BOT_USERINFO);
    }

    //handle call back
    private void handleCallbackQuery(Update update) {

        User sender = update.getCallbackQuery().getFrom();

        RemindBotUserInfo userInfo = userInfos.getOrDefault(sender.getId(), DUMMY_REMIND_BOT_USERINFO);

        stepCallbackRouter.get(userInfo.step).accept(update, userInfo);
    }

    private void handleCallBackFirstStep(Update update, RemindBotUserInfo userInfo) {

        CallbackQuery callbackQuery = update.getCallbackQuery();
        String chatId = String.valueOf(callbackQuery.getMessage().getChatId());

        String data = callbackQuery.getData();
        if ("DONE".equals(data)) {
            userInfos.replace(userInfo.userId, RemindBotUserInfo.builder()
                    .userId(userInfo.userId)
                    .step(SECOND_STEP)
                    .isChaining(userInfo.isChaining)
                    .commandChaining(userInfo.commandChaining)
                    .chosenUserLst(userInfo.chosenUserLst)
                    .currentDayPage(userInfo.currentDayPage)
                    .userNote(userInfo.userNote)
                    .build());

            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(callbackQuery.getId());
            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();

            editMessageReplyMarkup.setReplyMarkup(new InlineKeyboardMarkup());

            editMessageReplyMarkup.setChatId(chatId);
            editMessageReplyMarkup.setMessageId(callbackQuery.getMessage().getMessageId());

            editMessageReplyMarkup.setReplyMarkup(createInlineKeyBoardMarkUp(new ArrayList<>(), "type to cancle"));

            SendMessage sendMessage = new SendMessage();

            StringBuilder textResult = new StringBuilder("B???n ???? ch???n " + userInfo.chosenUserLst.size() + " ng?????i l?? : \n");

            for (int i = 0; i < userInfo.chosenUserLst.size(); i++) {

                textResult.append(userInfo.chosenUserLst.get(i).getFirstName()).append(", ");
            }

            String substring = textResult.substring(0, textResult.length() - 2);

            sendMessage.setChatId(chatId);
            sendMessage.setText(substring);
            try {
                execute(answerCallbackQuery);
                execute(editMessageReplyMarkup);
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            handleSecondStep(update, userInfo);
        } else {

            List<User> newUserLst = userInfo.chosenUserLst;
            newUserLst.add(new User(1L, data, false));
            userInfos.replace(userInfo.userId, RemindBotUserInfo.builder()
                    .userId(userInfo.userId)
                    .step(FIRST_STEP)
                    .isChaining(userInfo.isChaining)
                    .commandChaining(userInfo.commandChaining)
                    .chosenUserLst(newUserLst)
                    .build());

            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(callbackQuery.getId());
            try {
                execute(answerCallbackQuery);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleCallBackSecondStep(Update update, RemindBotUserInfo userInfo) {

        CallbackQuery callbackQuery = update.getCallbackQuery();

        String chatId = String.valueOf(callbackQuery.getMessage().getChatId());

        String callBackData = callbackQuery.getData();

        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();

        editMessageReplyMarkup.setChatId(chatId);
        editMessageReplyMarkup.setMessageId(callbackQuery.getMessage().getMessageId());

        List<List<String>> replyOptions = new ArrayList<>();
        List<String> result = new ArrayList<>();
        result.add("<<");

        if ("<<".equals(callBackData)) {

            int page = Math.max(0, userInfo.currentDayPage - 1);

            IntStream.range(page * PAGE_SIZE, page * PAGE_SIZE + 4).forEach(i -> result.add(LocalDateTime.now().plus(i, ChronoUnit.DAYS).format(ddMMYYYY).substring(0, 5)));
            result.add(">>");

            replyOptions.add(result);

            editMessageReplyMarkup.setReplyMarkup(createInlineKeyBoardMarkUp(replyOptions, "type to cancle"));

            if (userInfo.currentDayPage != 0) {
                try {
                    execute(editMessageReplyMarkup);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

            userInfos.replace(userInfo.userId, RemindBotUserInfo.builder()
                    .userId(userInfo.userId)
                    .isChaining(userInfo.isChaining)
                    .commandChaining(userInfo.commandChaining)
                    .step(userInfo.step)
                    .chosenDate(userInfo.chosenDate)
                    .chosenTime(userInfo.chosenTime)
                    .currentDayPage(page)
                    .chosenUserLst(userInfo.chosenUserLst)
                    .build());

            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(callbackQuery.getId());
            try {
                execute(answerCallbackQuery);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (">>".equals(callBackData)) {

            int page = Math.min(MAX_PAGE_SIZE, userInfo.currentDayPage + 1);

            IntStream.range(page * PAGE_SIZE, page * PAGE_SIZE + 4).forEach(i -> result.add(LocalDateTime.now().plus(i, ChronoUnit.DAYS).format(ddMMYYYY).substring(0, 5)));
            result.add(">>");

            replyOptions.add(result);

            editMessageReplyMarkup.setReplyMarkup(createInlineKeyBoardMarkUp(replyOptions, "type to cancle"));

            if (userInfo.currentDayPage != MAX_PAGE_SIZE) {
                try {
                    execute(editMessageReplyMarkup);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

            userInfos.replace(userInfo.userId, RemindBotUserInfo.builder()
                    .userId(userInfo.userId)
                    .isChaining(userInfo.isChaining)
                    .commandChaining(userInfo.commandChaining)
                    .step(userInfo.step)
                    .chosenDate(userInfo.chosenDate)
                    .chosenTime(userInfo.chosenTime)
                    .currentDayPage(page)
                    .chosenUserLst(userInfo.chosenUserLst)
                    .build());

            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(callbackQuery.getId());
            try {
                execute(answerCallbackQuery);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {

            userInfos.replace(userInfo.userId, RemindBotUserInfo.builder()
                    .userId(userInfo.userId)
                    .isChaining(userInfo.isChaining)
                    .commandChaining(userInfo.commandChaining)
                    .step(userInfo.step)
                    .chosenDate(callBackData)
                    .chosenTime(userInfo.chosenTime)
                    .currentDayPage(userInfo.currentDayPage)
                    .chosenUserLst(userInfo.chosenUserLst)
                    .build());

            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(callbackQuery.getId());
            try {
                execute(answerCallbackQuery);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            handleThirdStep(update, userInfo);
        }
    }

    private void handleCallBackThirdStep(Update update, RemindBotUserInfo userInfo) {

    }


    private void handleCallBackNoStep(Update update, RemindBotUserInfo userInfo) {

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
//                .clearKeyboard()
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

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class RemindBotUserInfo {

        Long userId;

        STEPS step;

        boolean isChaining = false;

        String commandChaining;

        List<User> chosenUserLst;

        String chosenTime = "";

        String chosenDate = "";

        int currentDayPage = 0;

        String userNote = "";
    }

    enum STEPS {
        FIRST_STEP,
        SECOND_STEP,
        THIRD_STEP,
        FOURTH_STEP,
        NO_STEP
    }
}

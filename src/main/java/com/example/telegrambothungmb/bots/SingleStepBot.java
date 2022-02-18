package com.example.telegrambothungmb.bots;

import com.example.telegrambothungmb.utils.ReadXlsxFileUtils;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.polls.StopPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.example.telegrambothungmb.bots.SingleStepBot.BOT_COMMANDS.*;
import static com.example.telegrambothungmb.bots.SingleStepBot.BOT_COMMANDS.SLEEP;

@Component
@Slf4j
@RequiredArgsConstructor
public class SingleStepBot extends TelegramLongPollingBot {

    private static final String BOT_TOKEN = "5164507812:AAESgXS8vsF57MNEyjZUX7fDDyOK2gVznWA";

    private DateTimeFormatter dft = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    public static final String SINGLE_STEP_BOT = "SingleStepBot";

    static Map<String, Consumer<Update>> botCmdGateWay = new HashMap<>();

    static Map<Long, UserDrinkInfo> userDrinkInfoMap = new HashMap<>();

    private static Map<String, Long> userIdPollIdMap = new HashMap<>();

    private final ReadXlsxFileUtils readXlsxFileUtils;


    @PostConstruct
    void setUp() {
        botCmdGateWay.put(DRINK.command, this::handleDrinkCommand);
        botCmdGateWay.put(CLOSE_DRINK_POLL.command, this::handleCloseDrinkPollCommand);
        botCmdGateWay.put(GET_TELEGRAM_ID.command, this::handleGetTelegramIdCommand);
        botCmdGateWay.put(SLEEP.command, this::handleSleepCommand);
    }


    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public String getBotUsername() {
        return SINGLE_STEP_BOT;
    }

    @Override
    public void onUpdateReceived(Update update) {

        //if call back query

        //if message
        if (update.hasMessage()) {

            //handle normal message
            handleNormalMessage(update);

            //handle message with reply
        }

        //handle poll answer
        if (update.hasPollAnswer()) {
            handlePollAnswer(update);
        }

    }

    private void handleNormalMessage(Update update) {
        if (update.getMessage().isCommand()) {
            //handle normal message with command
            botCmdGateWay.get(update.getMessage().getEntities().get(0).getText()).accept(update);
        } else {
            //TODO handle normal message with no command
        }
    }

    /**
     * /DRINK groupId to send a drink menu to a group as a poll
     *
     * @param update
     */
    private void handleDrinkCommand(Update update) {
        //get message
        Message msg = update.getMessage();

        //get params from msg
        Long fromUserId = msg.getFrom().getId();

        //get command
        String drinkCommand = msg.getEntities().get(0).getText();

        //get chat id from command
        String chatId = update.getMessage().getText().substring(
                drinkCommand.length()).trim();

        String question = "What do u want for a drink?";

        //read excel file
        List<String> lstOptions = getListDrinkFromExcel();

        //create poll
        createPollFromAList(question, lstOptions, fromUserId, chatId);
    }

    private List<String> getListDrinkFromExcel() {
        List<String> lstOptions = new ArrayList<>();

        readXlsxFileUtils.readByKeyValue("src/main/resources/files/DrinkLst.xlsx").forEach(row -> {
            lstOptions.add(String.format("%s - %s", row.getCell(0).getStringCellValue(), row.getCell(1).getStringCellValue()));
        });

        return lstOptions;
    }


    private void createPollFromAList(String question, List<String> lstOptions, Long fromUserId, String chatId) {
        SendPoll sendPoll = new SendPoll();

        sendPoll.setChatId(chatId);
        sendPoll.setAllowMultipleAnswers(true);
        sendPoll.setQuestion(question);
        sendPoll.setOptions(lstOptions);
        sendPoll.setIsAnonymous(false);
//        sendPoll.setCloseDate(10);

        try {
            executeAsync(sendPoll, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> botApiMethod, Message message) {
                    log.info("poll created");

                    //add new user to the map
                    String pollId = message.getPoll().getId();

                    userDrinkInfoMap.put(fromUserId, UserDrinkInfo.builder()
                            .id(fromUserId)
                            .pollId(pollId)
                            .msgId(message.getMessageId())
                            .chatId(Long.valueOf(chatId))
                            .options(lstOptions)
                            .lstOrder(new HashMap<>())
                            .build()
                    );

                    userIdPollIdMap.put(pollId, fromUserId);
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                    log.error("cant create poll: {}", e.getMessage());
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                    log.error("cant create poll: {}", e.getMessage());
                }
            });
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handlePollAnswer(Update update) {
        String pollId = update.getPollAnswer().getPollId();

        Long pollCreatorId = userIdPollIdMap.get(pollId);

        if (userDrinkInfoMap.containsKey(pollCreatorId)) {

            UserDrinkInfo userDrinkInfo = userDrinkInfoMap.get(pollCreatorId);

            Map<User, List<Integer>> answers = userDrinkInfo.lstOrder;

            answers.put(update.getPollAnswer().getUser(), update.getPollAnswer().getOptionIds());

            userDrinkInfoMap.replace(pollCreatorId, UserDrinkInfo.builder()
                    .id(userDrinkInfo.id)
                    .chatId(userDrinkInfo.chatId)
                    .pollId(userDrinkInfo.pollId)
                    .msgId(userDrinkInfo.msgId)
                    .options(userDrinkInfo.options)
                    .lstOrder(answers)
                    .build());
        }

        log.info("add answer");
    }

    private void handleCloseDrinkPollCommand(Update update) {

        Long userId = update.getMessage().getFrom().getId();

        UserDrinkInfo userDrinkInfo = userDrinkInfoMap.get(userId);

        StopPoll stopPoll = new StopPoll();

        stopPoll.setChatId("-1001739712412");
        stopPoll.setMessageId(userDrinkInfo.msgId);

        try {
            executeAsync(stopPoll, new SentCallback<Poll>() {
                @Override
                public void onResult(BotApiMethod<Poll> botApiMethod, Poll poll) {
                    log.info("a");

                    sendOrderInfo(userId, userDrinkInfo);
                    userDrinkInfoMap.remove(userIdPollIdMap.get(poll.getId()));

                    userIdPollIdMap.remove(poll.getId());
                }

                @Override
                public void onError(BotApiMethod<Poll> botApiMethod, TelegramApiRequestException e) {

                }

                @Override
                public void onException(BotApiMethod<Poll> botApiMethod, Exception e) {

                }
            });
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendOrderInfo(Long userId, UserDrinkInfo userDrinkInfo) {
        SendMessage msg = new SendMessage();

        msg.setChatId(String.valueOf(userId));

        List<String> orders = new ArrayList<>();

        userDrinkInfo.lstOrder.forEach((user, info) -> {

            StringBuilder order = new StringBuilder(user.getFirstName() + " ");

            for (int i = 0; i < info.size(); i++) {
                order.append(userDrinkInfo.options.get(i)).append(" ");
            }

            orders.add(order.toString());
        });

        StringBuilder orderText = new StringBuilder();

        for (String order : orders) {
            orderText.append(order).append(" \n");
        }
        msg.setText(orderText.toString());

        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * /SLEEP hh:mm dd/MM/YYYY duration(minute)
     *
     * @param update
     */
    private void handleSleepCommand(Update update) {

        List<String> params = Arrays.asList(update.getMessage().getText().split("\\s+"));

        String validateCommandParams = validateSleepCommand(params.get(1).trim(), params.get(2).trim(), params.get(3).trim());

        if(!"OK".equals(validateCommandParams)){
            sendMsgToChatId(update, validateCommandParams);
            return;
        }

        log.info("params from the command : {}", params);

        LocalDateTime chosenTime = LocalDateTime.parse(String.format("%s %s", params.get(1).trim(), params.get(2).trim()), dft);

        if(chosenTime.isBefore(LocalDateTime.now())){
            sendMsgToChatId(update, "Đã qua thời gian được chọn");
            return;
        }

        List<String> result = new ArrayList<>();

        LocalDateTime temp = chosenTime;

        while (temp.isAfter(LocalDateTime.now())) {
            result.add(temp.format(dft));

            temp = temp.plus(-Long.parseLong(params.get(3)), ChronoUnit.MINUTES);
        }

        log.info("time from result: {}", result);

        StringBuilder resultText = new StringBuilder("Bạn nên ngủ vào các khoảng giờ sau :  \n");

        for (int i = result.size()-1; i > 0; i--) {
            String time = result.get(i);
            resultText.append(time).append("\n");
        }

        log.info("Times to show to user : {}", resultText);

        sendMsgToChatId(update, resultText.toString());
    }

    private void sendMsgToChatId(Update update, String resultText) {
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(String.valueOf(update.getMessage().getFrom().getId()));
        sendMessage.setText(resultText);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String validateSleepCommand(String chosenTime, String chosenDate, String period) {
        String chosenTimeRegex = "^[0-9]{2}:[0-9]{2}$";
        String chosenDateRegex = "^[0-9]{2}/[0-9]{2}/[0-9]{4}$";
        String periodRegex = "^[0-9]{1,3}";

        if(!Pattern.matches(chosenTimeRegex, chosenTime)){
            return "Nhập sai format thời gian (HH:mm)";
        }
        if(!Pattern.matches(chosenDateRegex, chosenDate)){
            return "Nhập sai format ngày (dd/MM/yyyy)";
        };
        if(!Pattern.matches(periodRegex, period)){
            return "Nhập sai format chu kỳ (tối đa 3 chữ sỗ tối thiểu 1 chứ số";
        }
        return "OK";
    }

    /**
     * /ID
     *
     * @param update
     */

    private void handleGetTelegramIdCommand(Update update) {

    }


    enum BOT_COMMANDS {
        DRINK("/DRINK"),
        CLOSE_DRINK_POLL("/CLOSE_DRINK"),
        GET_TELEGRAM_ID("/ID"),
        SLEEP("/SLEEP");

        private final String command;

        BOT_COMMANDS(String command) {
            this.command = command;
        }
    }

    @Builder
    private static class UserDrinkInfo {

        Long id;

        Long chatId;

        String pollId;

        Integer msgId;

        List<String> options;

        Map<User, List<Integer>> lstOrder;
    }
}


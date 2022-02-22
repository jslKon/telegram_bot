package com.example.telegrambothungmb.bots;

import com.example.telegrambothungmb.service.BotAPIsService;
import com.example.telegrambothungmb.utils.ReadXlsxFileUtils;
import com.example.telegrambothungmb.utils.UpdateMessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.example.telegrambothungmb.bots.UtilsBot.BOT_COMMANDS.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class UtilsBot extends TelegramLongPollingBot {

    //dependencies
    private final ReadXlsxFileUtils readXlsxFileUtils;

    private final BotAPIsService botAPIsService;

    //params
    private static final String BOT_TOKEN = "5164507812:AAESgXS8vsF57MNEyjZUX7fDDyOK2gVznWA";

    private final DateTimeFormatter dft = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    public static final String UTILS_BOT = "UtilsBot";

    static Map<String, Consumer<Update>> botCmdGateWay = new HashMap<>();

    enum BOT_COMMANDS {
        HELP("/help", "danh sách các command"),
        CONVERT("/convert", "Chuyển đổi tiền tệ theo dạng /CONVERT from to amount"),
        CALCULATOR("/cal", "Tính toán. Format /CAL function (trong funcion không có cách)"),
        SLEEP("/sleep", "Tính h ngủ cho sáng mai khỏe, theo chu kỳ. Format /SLEEP time duration");

        private final String command;

        private final String desc;

        BOT_COMMANDS(String command, String desc) {
            this.command = command;
            this.desc = desc;
        }
    }

    @Override
    public String getBotUsername() {
        return UTILS_BOT;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    /**
     * handle update from the bot
     *
     * @param update
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            //handle normal message
            handleNormalMessage(update);
        }
    }

    private void handleNormalMessage(Update update) {
        if (update.getMessage().isCommand()) {
            //handle normal message with command
            botCmdGateWay.get(update.getMessage().getEntities().get(0).getText()).accept(update);
        } else if (update.getMessage().getForwardFrom() != null) {
            //handle forward message
            User forwardFromUser = update.getMessage().getForwardFrom();
            StringBuilder sb = new StringBuilder("Thông tin của người được forward tin nhắn : \n");
            sb.append("ID : ").append(forwardFromUser.getId()).append("\n");
            sb.append("First Name: ").append(forwardFromUser.getFirstName()).append("\n");

            if (forwardFromUser.getLastName() != null)
                sb.append("Last Name: ").append(forwardFromUser.getLastName()).append("\n");
            if (forwardFromUser.getUserName() != null)
                sb.append("UserName: ").append(forwardFromUser.getUserName()).append("\n");
            sendMsgToChatId(update, sb.toString());
        }
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

    @PostConstruct
    void setUp() {
        //TODO /SLEEP
        botCmdGateWay.put(SLEEP.command, this::handleSleepCommand);
        botCmdGateWay.put(CONVERT.command, this::handleConvertCommand);
        botCmdGateWay.put(CALCULATOR.command, this::handleCalculatorCommand);
        botCmdGateWay.put(HELP.command, this::handleHelpCommand);
    }


    /**
     * /HELP
     *
     * @param update
     */

    private void handleHelpCommand(Update update) {

        StringBuilder sb = new StringBuilder("");

        BOT_COMMANDS[] bot_commands = values();

        for (BOT_COMMANDS cmd : bot_commands) {
            sb.append(cmd.command).append(" ").append(cmd.desc).append("\n");
        }

        sb.append("Contact: hungmb@viettel.com.vn");

        sendMsgToChatId(update, sb.toString());
    }

    /**
     * /CONVERT from to amount
     */

    private void handleConvertCommand(Update update) {
        List<String> paramsFromMsg = UpdateMessageUtils.getParamsFromMsg(update.getMessage());

        //TODO validate params

        //TODO get ratio
        String currenciesPair = paramsFromMsg.get(1) + "_" + paramsFromMsg.get(2);

        Double amount = Double.valueOf(paramsFromMsg.get(3).replace(",", ""));

        Double ratio = Double.valueOf(
                botAPIsService.getRatio(
                                currenciesPair,
                                UpdateMessageUtils.CURRCONV_COMPACT,
                                UpdateMessageUtils.CURRCONV_API_KEY)
                        .getRatios().get(currenciesPair));

        log.info("ratio {}", ratio);

        //TODO calculate result
        sendMsgToChatId(update, String.valueOf(ratio * amount) + " " + paramsFromMsg.get(2));
    }

    /**
     * /CAL func
     */
    private void handleCalculatorCommand(Update update) {
        List<String> paramsFromMsg = UpdateMessageUtils.getParamsFromMsg(update.getMessage());

        String func = paramsFromMsg.get(1);

        Expression expression = new ExpressionBuilder(func).build();

        double result = expression.evaluate();

        sendMsgToChatId(update, String.valueOf(result));
    }

    /**
     * /SLEEP hh:mm dd/MM/YYYY duration(minute)
     *
     * @param update
     */
    private void handleSleepCommand(Update update) {

        List<String> params = UpdateMessageUtils.getParamsFromMsg(update.getMessage());

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

        if(result.size() > 30){
            sendMsgToChatId(update, "Kết quả quá nhiều, chọn mốc thời gian gần hơn hoặc chu kỳ lớn hơn");
            return;
        }

        StringBuilder resultText = new StringBuilder("Bạn nên ngủ vào các khoảng giờ sau :  \n");

        for (int i = result.size()-1; i > 0; i--) {
            String time = result.get(i);
            resultText.append(time).append("\n");
        }

        log.info("Times to show to user : {}", resultText);

        sendMsgToChatId(update, resultText.toString());
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
}

// Бот обработчик

import org.apache.log4j.PropertyConfigurator;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.vdurmont.emoji.EmojiParser;

public class Bot extends TelegramLongPollingBot {
    public static void main(String[] args) throws TelegramApiException {
        // Логгирование
        PropertyConfigurator.configure(System.getProperty("user.dir") + "/src/resources/log4j.properties");
        // Инициализируем бота и переопределяем методы отмеченные @Override
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(new Bot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Константы emoji
    final String howSellout = EmojiParser.parseToUnicode("Какой размер скидки интересует:grey_question:");
    final String chooseCategory = EmojiParser.parseToUnicode("Выберите категорию:point_down|type_1_2:");
    final String howPrice = EmojiParser.parseToUnicode("Какая цена интересует:grey_question:");
    final String sellout = EmojiParser.parseToUnicode("Скидки:grey_exclamation:");
    final String checkMark = EmojiParser.parseToUnicode(" :heavy_check_mark:");
    final String end = EmojiParser.parseToUnicode(":heavy_multiplication_x:");
    final String prices = EmojiParser.parseToUnicode("Цены:exclamation:");
    final String backerMark = EmojiParser.parseToUnicode(":back:");

    // Берем распарсенные данные из класса, экранируем лишние символы, добавляем в массив, затем выводим результат.
    public String resultGames(int checkSum) throws IOException {
        List<String> resValue = new ArrayList<>();
        for (String eachVal : CategoriesData.resultFiveGames(checkSum)) {
            if (eachVal.contains("-") || eachVal.contains("₽")) {
                String withoutSymbol = eachVal.replace("-", "\\-")
                        .replace("₽", "р")
                        .replace("*", "\\*")
                        .replace(".", "\\.")
                        .replace("+", "\\+");
                resValue.add(withoutSymbol + "\n\n");
            }
        }
        return String.join("", resValue);
    }

    // Конфигурация клавиатуры.
    public void setButtons(SendMessage msg) {
        ReplyKeyboardMarkup mainKeyboard = new ReplyKeyboardMarkup();
        msg.setReplyMarkup(mainKeyboard);
        mainKeyboard.setSelective(true);
        mainKeyboard.setResizeKeyboard(true);
        mainKeyboard.setOneTimeKeyboard(false);

        // Общий массив клавиатуры и 4 ряда.
        List<KeyboardRow> arrKeyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        KeyboardRow keyboardThirdRow = new KeyboardRow();
        KeyboardRow keyboardFourthRow = new KeyboardRow();

        // Условия отображения кнопок клавиатуры.
        if (msg.getText().equals(chooseCategory)) {
            keyboardFirstRow.add(new KeyboardButton(prices));
            keyboardFirstRow.add(new KeyboardButton(sellout));

            arrKeyboard.add(keyboardFirstRow);
            mainKeyboard.setKeyboard(arrKeyboard);

        } else if (msg.getText().equals(howPrice)) {
            keyboardFirstRow.add(new KeyboardButton("До 300р" + checkMark));
            keyboardFirstRow.add(new KeyboardButton("До 500р" + checkMark));
            keyboardSecondRow.add(new KeyboardButton("До 1000р" + checkMark));
            keyboardSecondRow.add(new KeyboardButton("До 1500р" + checkMark));
            keyboardThirdRow.add(new KeyboardButton("До 2000р"+ checkMark));
            keyboardThirdRow.add(new KeyboardButton("До 2500р" + checkMark));
            keyboardFourthRow.add(new KeyboardButton("Основное меню" + backerMark));

            arrKeyboard.add(keyboardFirstRow);
            arrKeyboard.add(keyboardSecondRow);
            arrKeyboard.add(keyboardThirdRow);
            arrKeyboard.add(keyboardFourthRow);
            mainKeyboard.setKeyboard(arrKeyboard);

        } else if (msg.getText().equals(howSellout)) {
            keyboardFirstRow.add(new KeyboardButton("От 30%" + checkMark));
            keyboardFirstRow.add(new KeyboardButton("От 50%" + checkMark));
            keyboardSecondRow.add(new KeyboardButton("От 60%" + checkMark));
            keyboardSecondRow.add(new KeyboardButton("От 70%" + checkMark));
            keyboardThirdRow.add(new KeyboardButton("От 80%" + checkMark));
            keyboardThirdRow.add(new KeyboardButton("От 90%" + checkMark));
            keyboardFourthRow.add(new KeyboardButton("Основное меню" + backerMark));

            arrKeyboard.add(keyboardFirstRow);
            arrKeyboard.add(keyboardSecondRow);
            arrKeyboard.add(keyboardThirdRow);
            arrKeyboard.add(keyboardFourthRow);
            mainKeyboard.setKeyboard(arrKeyboard);

        // Если кждое условие выше false, вывести определнную кнопку.
        // Костыльное решение, но пока так.
        // Через регулярку находим число с процентом и минусом перед ним, если true, значит текущий раздел == цены.
        } else {
            Pattern regex = Pattern.compile("-\\d{2}%");
            Matcher getPercentSymbol = regex.matcher(msg.getText());
            if (getPercentSymbol.find()) {
                String resPercent = getPercentSymbol.group();
                if (!resPercent.equals("")) {
                    keyboardFirstRow.add(new KeyboardButton("Вернуть цены" + backerMark));
                }
            // Иначе текущий раздел == скидки.
            } else {
                keyboardFirstRow.add(new KeyboardButton("Вернуть скидки" + backerMark));
            }
            // Добавляем значения в общий массив.
            arrKeyboard.add(keyboardFirstRow);
            mainKeyboard.setKeyboard(arrKeyboard);
        }
    }

    // Модуль отправки сообщения и клавиатуры в текущую сессию чата.
    public void sendMsg(Message msg, String text) {
        SendMessage sender = new SendMessage();
        sender.enableMarkdownV2(true);
        sender.setChatId(msg.getChatId().toString());
        sender.setText(text);
        try {
            setButtons(sender);
            execute(sender);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Юзернейм бота.
    @Override
    public String getBotUsername() {
        return ""; // <- Вставить наименование бота
    }

    // Уникальный Токен бота.
    @Override
    public String getBotToken() {
        return ""; // <- Вставить токен бота
    }

    // Модуль Использование модуля отправки сообщения по условию.
    // Не использован опертор switch/case по причине того что, данная конструкция не работает с модулем Emoji.
    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            if (message.getText().equals("/start") || message.getText().equals("Основное меню" + backerMark)) {
                sendMsg(message, chooseCategory);
            } else if (message.getText().equals(prices) || message.getText().equals("Вернуть цены" + backerMark)) {
                sendMsg(message, howPrice);
            } else if (message.getText().equals(sellout) || message.getText().equals("Вернуть скидки" + backerMark)) {
                sendMsg(message, howSellout);
            } else if (message.getText().equals("До 300р" + checkMark)) {
                try {
                    sendMsg(message, resultGames(300));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (message.getText().equals("До 500р" + checkMark)) {
                try {
                    sendMsg(message, resultGames(500));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (message.getText().equals("До 1000р" + checkMark)) {
                try {
                    sendMsg(message, resultGames(1000));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (message.getText().equals("До 1500р" + checkMark)) {
                try {
                    sendMsg(message, resultGames(1500));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (message.getText().equals("До 2000р" + checkMark)) {
                try {
                    sendMsg(message, resultGames(2000));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (message.getText().equals("До 2500р" + checkMark)) {
                try {
                    sendMsg(message, resultGames(2500));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (message.getText().equals("От 30%" + checkMark)) {
                try {
                    sendMsg(message, resultGames(30));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (message.getText().equals("От 50%" + checkMark)) {
                try {
                    sendMsg(message, resultGames(50));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (message.getText().equals("От 60%" + checkMark)) {
                try {
                    sendMsg(message, resultGames(60));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (message.getText().equals("От 70%" + checkMark)) {
                try {
                    sendMsg(message, resultGames(70));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (message.getText().equals("От 80%" + checkMark)) {
                try {
                    sendMsg(message, resultGames(80));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (message.getText().equals("От 90%" + checkMark)) {
                try {
                    sendMsg(message, resultGames(90));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                sendMsg(message, "Используй клавиатуру" + end);
            }
        }
    }
}
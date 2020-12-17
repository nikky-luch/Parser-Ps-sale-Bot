// Парсер.

import com.google.common.collect.Lists;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CategoriesData {

    // Указываем путь к сайту, с которого будем парсить данные.
    public static Document getPage() throws IOException {
        String url = "https://ps-sale.ru/";
        return Jsoup.parse(new URL(url), 3000);
    }

    // Возвращаем массив с данными всех игр, которые попали в определенную категорию.
    public static List<String> resultInfoGame(int priceVal) throws IOException {
        Document page = getPage();
        Element mainTable = page.select("div[class=container]").first();
        Elements divLinks = mainTable.select("a[class=divlink]");

        List<String> arrGameInfo = new ArrayList<>();
        for (Element eachValues : divLinks) {
            // Наименования игр.
            String resultNames = eachValues.select("div[class=name-holder]").text();
            // Основная цена со скидкой.
            String resultPrices = eachValues.select("div[class=price-holder]").text().replace(" ", "");
            String gamePsnPrice = eachValues.select("div[class=price-holder price-holder-with-psnprice]").text().replace(" ", "");
            // Скидка только для ps plus как основная.
            String gameNonPsnPrice = eachValues.select("div[class=psn-price-holder psn-price-holder-without-price]").text().replace(" ", "");
            // Специальная цена для ps plus, в дополнение к основной.
            String gamePricePSplus = eachValues.select("div[class=psn-price-holder]").text().replace(" ", "");
            // Скидки процент.
            String redDiscount = eachValues.select("div[class=percent-holder red-place-holder]").text();
            String yellowDiscount = eachValues.select("div[class=percent-holder yellow-place-holder]").text();
            String greenDiscount = eachValues.select("div[class=percent-holder green-place-holder]").text();
            // Старая цена.
            String oldPrice = eachValues.select("div[class=old-price-holder]").text();
            String numberPrice = oldPrice.substring(13).replace(" ", "");
            String textPrice = oldPrice.substring(0, 12);
            String resultOldPrice = textPrice + " " + numberPrice;

            // Обрезаем символы у цены и процента.
            Pattern delAllSymbol = Pattern.compile("\\d+");

            // Проверяем количество чисел на вход, заходим в нужный блок и выводим всю инфу с необходимой категорией цен или процентов.
            // Проверяем количество цифр на вход.
            int getLengthArgument = (int) Math.ceil(Math.log10(priceVal));
            // Если цифр больше чем 2, значит вернуть данные по ценам.
            if (getLengthArgument > 2) {
                Matcher allPrices = delAllSymbol.matcher(resultPrices + gamePsnPrice + gameNonPsnPrice);
                if (allPrices.find()) {
                    String resPrices = allPrices.group();
                    int getIntPrices = Integer.parseInt(resPrices);
                    if (getIntPrices <= priceVal) {
                        String setStringPrice = Integer.toString(getIntPrices);
                        // Если есть специальная цена PS Plus, добавить в массив.
                        if (!gamePricePSplus.equals("")) {
                            arrGameInfo.add(resultNames + " " + "\\>"
                                            + setStringPrice + "р" + "\\<"
                                            + " PS Plus: " + gamePricePSplus + " "
                                            + redDiscount
                                            + yellowDiscount
                                            + greenDiscount + " "
                                            + resultOldPrice);
                        } else {
                            arrGameInfo.add(resultNames + " " + "\\>"
                                            + setStringPrice + "р" + "\\<" + " "
                                            + redDiscount
                                            + yellowDiscount
                                            + greenDiscount + " "
                                            + resultOldPrice);
                        }
                    }
                }

            // Если цифр 2 или меньше, вернуть данные по процентам.
            } else {
                Matcher allPercent = delAllSymbol.matcher(redDiscount + yellowDiscount + greenDiscount);
                if (allPercent.find()) {
                    String resPercent = allPercent.group();
                    int getIntPercent = Integer.parseInt(resPercent);
                    if (getIntPercent >= priceVal) {
                        String setStringPercent = Integer.toString(getIntPercent);
                        if (!gamePricePSplus.equals("")) {
                            arrGameInfo.add(resultNames + " "
                                            + resultPrices
                                            + gamePsnPrice
                                            + gameNonPsnPrice
                                            + " PS Plus: " + gamePricePSplus + " " + "\\>"
                                            + setStringPercent + "%" + "\\<" + " "
                                            + resultOldPrice);
                        } else {
                            arrGameInfo.add(resultNames + " "
                                            + resultPrices
                                            + gamePsnPrice
                                            + gameNonPsnPrice + " " + "\\>"
                                            + setStringPercent + "%" + "\\<" + " "
                                            + resultOldPrice);
                        }
                    }
                }
            }
        }
        return arrGameInfo;
    }

    // Метод генерации 7 рандомных элементов из массива (вариантов игр), который принимает на вход результат числа переданного от Бота.
    public static List<String> resultFiveGames(int botNumber) throws IOException {
        Random rand = new Random();
        List<String> finalList = new ArrayList<>();
        List<String> givenList = Lists.newArrayList(resultInfoGame(botNumber));

        if (givenList.size() > 7) {
            int numberOfElements = 7;
            for (int i = 0; i < numberOfElements; i++) {
                int randomIndex = rand.nextInt(givenList.size());
                String randomElement = givenList.get(randomIndex);
                finalList.add(randomElement);
                givenList.remove(randomIndex);
            }
            return finalList;
        } else {
            return givenList;
        }
    }
}

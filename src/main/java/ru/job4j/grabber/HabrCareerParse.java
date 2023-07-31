package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.format.DateTimeFormatter;


public class HabrCareerParse {

    private static final int NUMBER_OF_PAGES = 5;
    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        for (int i = 0; i < NUMBER_OF_PAGES; i++) {
            Connection connection = Jsoup.connect(PAGE_LINK + i);
            getVacancies(connection);
        }
    }

    public static void getVacancies(Connection connection) throws IOException {
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            String vacancyName = titleElement.text();

            String vacancyDatetime = row.select(".vacancy-card__date time").first().attr("datetime");
            System.out.println(vacancyDatetime);
            HabrCareerDateTimeParser habrCareerDateTimeParser = new HabrCareerDateTimeParser();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            vacancyDatetime = habrCareerDateTimeParser.parse(vacancyDatetime).format(formatter);
            System.out.println("parsed:" + habrCareerDateTimeParser.parse(vacancyDatetime));

            String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            System.out.printf("%s: %s %s%n", vacancyDatetime, vacancyName, link);
        });
    }
}
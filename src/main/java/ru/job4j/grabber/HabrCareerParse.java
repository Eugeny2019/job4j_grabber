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

    private static final int NUMBER_OF_PAGES = 1;
    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        HabrCareerParse habrCareerParse = new HabrCareerParse();
        for (int i = 0; i < NUMBER_OF_PAGES; i++) {
            Connection connection = Jsoup.connect(PAGE_LINK + i);
            habrCareerParse.getVacancies(connection);
        }
    }

    public void getVacancies(Connection connection) throws IOException {
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            String vacancyName = titleElement.text();

            String vacancyDatetime = row.select(".vacancy-card__date time").first().attr("datetime");
            HabrCareerDateTimeParser habrCareerDateTimeParser = new HabrCareerDateTimeParser();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            vacancyDatetime = habrCareerDateTimeParser.parse(vacancyDatetime).format(formatter);

            String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));

            String vacancyDescription;
            vacancyDescription = retrieveDescription(link);

            System.out.printf("%s: %s %s%nDescription:%n%s%n", vacancyDatetime, vacancyName, link, vacancyDescription);
        });
    }

    private String retrieveDescription(String link) {
        StringBuilder description = new StringBuilder();
        Connection connection = Jsoup.connect(link);
        Document document = null;
        try {
            document = connection.get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements sectionRows = document.select(".vacancy-description__text>h3");
        Elements sectionTextRows = document.select(".vacancy-description__text>div.style-ugc");

        for (int i = 0; i < sectionRows.size(); i++) {
            description.append(sectionRows.get(i).text());
            description.append(":");
            description.append(System.lineSeparator());
            description.append(sectionTextRows.get(i).wholeText());
            description.append(":");
            description.append(System.lineSeparator());
        }
        return description.toString();
    }
}
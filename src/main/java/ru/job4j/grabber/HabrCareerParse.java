package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.quartz.SchedulerException;
import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class HabrCareerParse implements Parse {
    private static final int NUMBER_OF_PAGES = 5;
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public List<Post> getVacancies(Connection connection) throws IOException {
        List<Post> posts = new ArrayList<>();
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            String vacancyName = titleElement.text();

            String vacancyDatetime = row.select(".vacancy-card__date time").first().attr("datetime");
            LocalDateTime vacancyLocalDateTime = dateTimeParser.parse(vacancyDatetime);

            String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            int vacancyId = Integer.parseInt(link.substring(link.lastIndexOf("/") + 1));

            String vacancyDescription;
            vacancyDescription = retrieveDescription(link);

            posts.add(new Post(vacancyId, vacancyName, link, vacancyDescription, vacancyLocalDateTime));
        });
        return posts;
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
            description.append(System.lineSeparator());
            description.append(sectionTextRows.get(i).wholeText());
            description.append(System.lineSeparator());
        }
        return description.toString();
    }

    @Override
    public List<Post> list(String link) {
        if (link == null || link.isBlank()) {
            link = PAGE_LINK;
        }
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_PAGES; i++) {
            Connection connection = Jsoup.connect(link + i);
            try {
                posts.addAll(getVacancies(connection));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return posts;
    }

    @Override
    public void init() throws SchedulerException {

    }
}
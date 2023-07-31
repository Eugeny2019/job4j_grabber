package utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import ru.job4j.grabber.HabrCareerParse;

import java.io.IOException;

public class HabrCareerDateTimeParserTests {

    @Test
    public void dateConversionShouldNotResultInAnError() throws IOException {
        Connection connection = Jsoup.connect("https://career.habr.com/vacancies/java_developer?page=");
        HabrCareerParse.getVacancies(connection);
    }
}

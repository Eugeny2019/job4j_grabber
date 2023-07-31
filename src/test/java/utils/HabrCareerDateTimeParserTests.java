package utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.time.format.DateTimeFormatter;

public class HabrCareerDateTimeParserTests {

    @Test
    public void dateConversionShouldNotResultInAnError() {
        HabrCareerDateTimeParser habrCareerDateTimeParser = new HabrCareerDateTimeParser();
        String expected = "31-07-2023 14:55:29";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String fact = habrCareerDateTimeParser.parse("2023-07-31T14:55:29+03:00").format(formatter);
        Assertions.assertEquals(expected, fact);
    }
}

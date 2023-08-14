package ru.job4j.grabber;

import java.util.List;

public interface Parse extends Grab {
    List<Post> list(String link);
}
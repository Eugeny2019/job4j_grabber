package ru.job4j.grabber;

import org.quartz.SchedulerException;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {
    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            cnn = DriverManager.getConnection(cfg.getProperty("jdbc.url"), cfg.getProperty("jdbc.username"), cfg.getProperty("jdbc.password"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Properties config = new Properties();
        try (InputStream in = new FileInputStream("src/main/resources/app.properties")) {
            config.load(in);
        }
        PsqlStore psqlStore = new PsqlStore(config);
        Post post = new Post(1, "vacation", "https://afdgdds.fds", "cool vacation", new HabrCareerDateTimeParser().parse("2023-07-31T14:55:29+03:00"));
        psqlStore.save(post);
        System.out.println(psqlStore.findById(1));
        psqlStore.getAll().forEach(System.out::println);
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement = cnn.prepareStatement("insert into post(name, text, link, created) values (?, ?, ?, ?) ON CONFLICT (link) DO NOTHING")) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement = cnn.prepareStatement("select * from post order by id asc")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(new Post(
                                    resultSet.getInt("id"),
                                    resultSet.getString("name"),
                                    resultSet.getString("link"),
                                    resultSet.getString("text"),
                                    resultSet.getTimestamp("created").toLocalDateTime()
                            )
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (posts.isEmpty()) {
            posts.add(new Post(0, null, null, null, null));
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post;
        try (PreparedStatement statement = cnn.prepareStatement("select * from post where id=?")) {
            statement.setInt(1, id);
            statement.execute();
            try (ResultSet resultSet = statement.executeQuery()) {
                post = (resultSet.next())
                        ?
                        new Post(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getString("link"),
                                resultSet.getString("text"),
                                resultSet.getTimestamp("created").toLocalDateTime())
                        :
                        new Post(0, null, null, null, null);
                return post;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    @Override
    public void init() throws SchedulerException {

    }
}
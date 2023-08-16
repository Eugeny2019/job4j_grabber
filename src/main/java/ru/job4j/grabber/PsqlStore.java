package ru.job4j.grabber;

import org.quartz.SchedulerException;

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
        try (PreparedStatement statement = cnn.prepareStatement("select * from post order by id asc")) {
            List<Post> posts = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(makePost(resultSet));
                }
                if (posts.isEmpty()) {
                    posts.add(makePost(resultSet));
                }
                return posts;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Post findById(int id) {
        try (PreparedStatement statement = cnn.prepareStatement("select * from post where id=?")) {
            statement.setInt(1, id);
            statement.execute();
            try (ResultSet resultSet = statement.executeQuery()) {
                return makePost(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException();
    }

    private Post makePost(ResultSet resultSet) throws SQLException {
        return (resultSet.next())
                ?
                new Post(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("link"),
                        resultSet.getString("text"),
                        resultSet.getTimestamp("created").toLocalDateTime())
                :
                new Post(0, null, null, null, null);
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
package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public class UserJdbcStorage extends BaseRepository<User> implements UserStorage {
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_BY_EMAIL_QUERY = "SELECT * FROM users WHERE email = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String INSERT_QUERY = "INSERT INTO users(login, email, name, birthday) " + "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET login = ?, email = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String ADD_FRIENDS_QUERY = "INSERT INTO friends (user_id, friend_id) " + "VALUES (?, ?)";
    private static final String DELETE_FRIENDS_QUERY = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_FRIENDS_QUERY = "SELECT u.* FROM users u JOIN friends f ON u.id = f.friend_id WHERE f.user_id = ?";
    private static final String FIND_COMMON_FRIENDS_QUERY = "SELECT u.* FROM users u WHERE u.id IN (" +
            "SELECT f1.friend_id FROM friends f1 WHERE f1.user_id = ?" +
            ") AND u.id IN (" +
            "SELECT f2.friend_id FROM friends f2 WHERE f2.user_id = ?" +
            ")";

    public UserJdbcStorage(JdbcTemplate jdbcTemplate, RowMapper<User> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public Optional<User> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return findOne(FIND_BY_EMAIL_QUERY, email);
    }

    @Override
    public List<User> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public User save(User user) {
        Long id = insert(
                INSERT_QUERY,
                user.getLogin(),
                user.getEmail(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        update(
                UPDATE_QUERY,
                user.getLogin(),
                user.getEmail(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        insert(
                ADD_FRIENDS_QUERY,
                userId,
                friendId
        );
    }

    @Override
    public boolean removeFriend(Long userId, Long friendId) {
        return update(
                DELETE_FRIENDS_QUERY,
                userId,
                friendId
        );
    }

    @Override
    public List<User> getFriends(Long userId) {
        return findMany(FIND_FRIENDS_QUERY, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        return findMany(FIND_COMMON_FRIENDS_QUERY, userId1, userId2);
    }
}

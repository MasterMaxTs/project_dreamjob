package ru.job4j.dreamjob.store;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.job4j.dreamjob.Main;
import ru.job4j.dreamjob.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class UserDBStoreTest {

    private BasicDataSource pool;
    private UserDBStore store;
    private List<User> users;

    @Before
    public void whenSetUp() {
        pool = new Main().loadPool();
        store = new UserDBStore(pool);
        User first = new User(1, "name1", "email1", "pass1");
        User second= new User(2, "name2", "email2", "pass2");
        users = List.of(first, second);
    }

    @After
    public void wipeTable() {
        try (Connection cn = pool.getConnection()) {
            PreparedStatement ps = cn.prepareStatement("DELETE FROM users");
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void whenFindAllUsers() {
        users.forEach(store::add);
        assertEquals(users, store.findAll());
    }

    @Test
    public void whenAddUser() {
        User user= users.get(0);
        store.add(user);
        User userInDb = store.findById(user.getId()).get();
        assertThat(userInDb.getName(), is(user.getName()));
        assertThat(userInDb.getEmail(), is(user.getEmail()));
    }

    @Test
    public void whenUpdateUser() {
        User user = users.get(0);
        store.add(user);
        user.setEmail("new email");
        store.update(user);
        assertThat(
                store.findById(user.getId()).get().getEmail(),
                is("new email")
        );
    }

    @Test
    public void whenFindUserByIdThanSuccess() {
        users.forEach(store::add);
        User expected = users.get(1);
        User userInDb = store.findById(expected.getId()).get();
        assertThat(userInDb.getName(), is(expected.getName()));
        assertThat(userInDb.getEmail(), is(expected.getEmail()));
    }

    @Test
    public void whenFindUserByIdThanEmpty() {
        users.forEach(store::add);
        Optional<User> userInDb = store.findById(3);
        assertThat(userInDb, is(Optional.empty()));
    }

    @Test
    public void whenFindUserByEmailAndPwdThanSuccess() {
        User expected = users.get(0);
        store.add(expected);
        User userInDb = store.findUserByEmailAndPwd(
                expected.getEmail(),
                expected.getPassword()
        ).get();
        assertThat(userInDb.getEmail(), is(expected.getEmail()));
        assertThat(userInDb.getPassword(), is(expected.getPassword()));
    }

    @Test
    public void whenInvalidEmailThanFindUserByEmailAndPwdThanFail() {
        User user = users.get(0);
        store.add(user);
        Optional<User> userInDb = store.findUserByEmailAndPwd(
                "test@test.com",
                user.getPassword()
        );
        assertThat(userInDb, is(Optional.empty()));
    }

    @Test
    public void whenInvalidPasswordThanFindUserByEmailAndPwdThanFail() {
        User user = users.get(0);
        store.add(user);
        Optional<User> userInDb = store.findUserByEmailAndPwd(
                user.getEmail(),
                "testing"
        );
        assertThat(userInDb, is(Optional.empty()));
    }
}
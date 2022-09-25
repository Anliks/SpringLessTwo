package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.UserDto;
import com.edu.ulab.app.entity.Person;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.exception.NotValidDataException;
import com.edu.ulab.app.mapper.UserMapper;
import com.edu.ulab.app.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImplTemplate implements UserService {
    private final JdbcTemplate jdbcTemplate;
    UserMapper userMapper;

    public UserServiceImplTemplate(JdbcTemplate jdbcTemplate, UserMapper userMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto createUser(UserDto userDto) {

        final String INSERT_SQL = "INSERT INTO PERSON(FULL_NAME, TITLE, AGE) VALUES (?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
                    ps.setString(1, userDto.getFullName());
                    ps.setString(2, userDto.getTitle());
                    ps.setLong(3, userDto.getAge());
                    return ps;
                }, keyHolder);

        userDto.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return userDto;
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        final String UPDATE_SQL = "UPDATE PERSON SET FULL_NAME = ?, TITLE = ?, AGE = ? WHERE ID = ?";
        if (userDto.getId() == null) {
            throw new NotValidDataException("Id is null");
        }
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(UPDATE_SQL);
            ps.setString(1, userDto.getFullName());
            ps.setString(2, userDto.getTitle());
            ps.setInt(3, userDto.getAge());
            ps.setLong(4, userDto.getId());
            log.info("User id={} updated", userDto.getId());
            return ps;
        });
        return userDto;
    }

    @Override
    public UserDto getUserById(Long id) {
        final String SELECT_SQL = "SELECT * FROM PERSON WHERE ID = ?";
        Person person;

        if (id == null) {
            throw new NotValidDataException("Id is null");
        }

        try {
            person = jdbcTemplate.queryForObject(SELECT_SQL, new BeanPropertyRowMapper<>(Person.class), id);
        } catch (EmptyResultDataAccessException ex) {
            throw new NotFoundException("User Not found");
        }
        log.info("User found! {}", person);
        return userMapper.personToUserDto(person);
    }

    @Override
    public void deleteUserById(Long id) {
        if (id == null) {
            throw new NotValidDataException("id is null");
        }

        final String DELETE_SQL = "DELETE FROM PERSON WHERE ID = ?";
        if (jdbcTemplate.update(DELETE_SQL, id) == 0) {
            throw new NotFoundException("Id is not found");
        }
    }
}

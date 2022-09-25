package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.UserDto;
import com.edu.ulab.app.entity.Person;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.exception.NotValidDataException;
import com.edu.ulab.app.mapper.UserMapper;
import com.edu.ulab.app.repository.UserRepository;
import com.edu.ulab.app.service.UserService;
import com.edu.ulab.app.validation.UserValidation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        Person user = userMapper.userDtoToPerson(userDto);
        log.info("Mapped user: {}", user);
        Person savedUser = userRepository.save(user);
        log.info("Saved user: {}", savedUser);
        return userMapper.personToUserDto(savedUser);
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        if (UserValidation.isValid(userDto)) {
            UserDto findUser = getUserById(userDto.getId());
            log.info("find user for update {}", findUser);
            findUser.setAge(userDto.getAge());
            findUser.setTitle(userDto.getTitle());
            findUser.setFullName(userDto.getFullName());
            log.info("user sets column {}", findUser);
            Person updatedUser = userRepository.save(userMapper.userDtoToPerson(findUser));
            log.info("user update {}", updatedUser);
            return userMapper.personToUserDto(updatedUser);
        } else {
            throw new NotValidDataException("Not Valid Data");
        }
    }

    @Override
    public UserDto getUserById(Long id) {
        log.info("Got ID for get User {}", id);
        UserDto userDto = userMapper.personToUserDto(userRepository.findById(id).orElseThrow(() -> new NotFoundException("user ID Not found " + id)));
        log.info("Got User By ID {}", userDto);
        return userDto;
    }

    @Override
    public void deleteUserById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            log.info("User deleted");
        } else {
            throw new NotFoundException("Id not found is base " + id);
        }

    }

}

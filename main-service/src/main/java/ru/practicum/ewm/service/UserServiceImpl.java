package ru.practicum.ewm.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest request) {
        log.info("Создание пользователя: {}", request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Пользователь с email '" + request.getEmail() + "' уже существует");
        }
        User user = UserMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        log.info("Пользователь создан с id: {}", savedUser.getId());
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Получение пользователей: ids={}, from={}, size={}", ids, from, size);

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        // ИСПРАВЛЕНО: используем единый метод с поддержкой null ids
        return userRepository.findAllByIds(ids, pageable)
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с id: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        userRepository.deleteById(userId);
        log.info("Пользователь с id: {} удален", userId);
    }
}
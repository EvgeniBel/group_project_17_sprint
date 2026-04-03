package ru.practicum.ewm.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.service.UserService;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAdminController.class)
class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private NewUserRequest newUserRequest;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        newUserRequest = new NewUserRequest();
        newUserRequest.setEmail("test@example.com");
        newUserRequest.setName("Test User");

        userDto = new UserDto(1L, "test@example.com", "Test User");
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        when(userService.createUser(any(NewUserRequest.class))).thenReturn(userDto);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));

        verify(userService, times(1)).createUser(any(NewUserRequest.class));
    }

    @Test
    void createUser_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        NewUserRequest invalidRequest = new NewUserRequest();
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setName("Test User");

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithEmptyName_ShouldReturnBadRequest() throws Exception {
        NewUserRequest invalidRequest = new NewUserRequest();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setName("");

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUsers_WithoutIds_ShouldReturnList() throws Exception {
        List<UserDto> users = Arrays.asList(userDto);
        when(userService.getUsers(null, 0, 10)).thenReturn(users);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));

        verify(userService, times(1)).getUsers(null, 0, 10);
    }

    @Test
    void getUsers_WithIds_ShouldReturnFilteredList() throws Exception {
        List<Long> ids = Arrays.asList(1L, 2L);
        List<UserDto> users = Arrays.asList(userDto);
        when(userService.getUsers(ids, 0, 10)).thenReturn(users);

        mockMvc.perform(get("/admin/users")
                        .param("ids", "1", "2"))
                .andExpect(status().isOk());

        verify(userService, times(1)).getUsers(ids, 0, 10);
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        doThrow(new NotFoundException("Пользователь с id=999 не найден"))
                .when(userService).deleteUser(999L);

        mockMvc.perform(delete("/admin/users/999"))
                .andExpect(status().isNotFound());
    }

}
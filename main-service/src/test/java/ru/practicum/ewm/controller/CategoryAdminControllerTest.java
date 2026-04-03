package ru.practicum.ewm.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryRequest;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.service.CategoryService;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryAdminController.class)
class CategoryAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    private NewCategoryRequest newCategoryRequest;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        newCategoryRequest = new NewCategoryRequest();
        newCategoryRequest.setName("Концерты");

        categoryDto = new CategoryDto(1L, "Концерты");
    }

    @Test
    void createCategory() throws Exception {
        when(categoryService.createCategory(any(NewCategoryRequest.class)))
                .thenReturn(categoryDto);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Концерты"));

        verify(categoryService, times(1)).createCategory(any(NewCategoryRequest.class));
    }

    @Test
    void createCategoryWithEmptyName() throws Exception {
        NewCategoryRequest invalidRequest = new NewCategoryRequest();
        invalidRequest.setName("");

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategoryWithDuplicateName() throws Exception {
        when(categoryService.createCategory(any(NewCategoryRequest.class)))
                .thenThrow(new ConflictException("Категория с именем 'Концерты' уже существует"));

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("409 CONFLICT"));
    }

    @Test
    void updateCategory() throws Exception {
        CategoryDto updatedDto = new CategoryDto(1L, "Спектакли");

        when(categoryService.updateCategory(eq(1L), any(CategoryDto.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(patch("/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Спектакли"));

        verify(categoryService, times(1)).updateCategory(eq(1L), any(CategoryDto.class));
    }

    @Test
    void updateCategoryWithNonExistentId() throws Exception {
        CategoryDto updateDto = new CategoryDto(999L, "Спектакли");

        when(categoryService.updateCategory(eq(999L), any(CategoryDto.class)))
                .thenThrow(new NotFoundException("Категория с id=999 не найдена"));

        mockMvc.perform(patch("/admin/categories/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404 NOT_FOUND"));
    }

    @Test
    void deleteCategory() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/admin/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteCategory(1L);
    }

    @Test
    void deleteCategoryWithNonExistentId() throws Exception {
        doThrow(new NotFoundException("Категория с id=999 не найдена"))
                .when(categoryService).deleteCategory(999L);

        mockMvc.perform(delete("/admin/categories/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404 NOT_FOUND"));
    }

}
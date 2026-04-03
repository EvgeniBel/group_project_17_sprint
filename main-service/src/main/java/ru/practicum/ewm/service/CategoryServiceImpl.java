package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryRequest;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;  // ДОБАВЛЕНО для проверки связанных событий

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryRequest request) {
        log.info("Создание категории: {}", request);

        if (categoryRepository.existsByName(request.getName())) {
            throw new ConflictException("Категория с именем '" + request.getName() + "' уже существует");
        }

        Category category = CategoryMapper.toEntity(request);
        Category savedCategory = categoryRepository.save(category);
        log.info("Категория создана с id: {}", savedCategory.getId());
        return CategoryMapper.toCategoryDto(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        log.info("Обновление категории с id: {}", catId);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + catId + " не найдена"));

        if (!category.getName().equals(categoryDto.getName()) &&
                categoryRepository.existsByName(categoryDto.getName())) {
            throw new ConflictException("Категория с именем '" + categoryDto.getName() + "' уже существует");
        }

        category.setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(category);

        return CategoryMapper.toCategoryDto(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        log.info("Удаление категории с id: {}", catId);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + catId + " не найдена"));

        // ИСПРАВЛЕНО: используем отдельный запрос для проверки связанных событий
        boolean hasEvents = eventRepository.existsByCategoryId(catId);
        if (hasEvents) {
            throw new ConflictException("Невозможно удалить категорию, так как с ней связаны события");
        }

        categoryRepository.deleteById(catId);
        log.info("Категория с id: {} удалена", catId);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        log.info("Получение категорий: from = {}, size = {}", from, size);

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        return categoryRepository.findAll(pageable)
                .stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategory(Long id) {
        log.info("Получение категории с id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Category с id=%d не найден", id)));

        return CategoryMapper.toCategoryDto(category);
    }
}
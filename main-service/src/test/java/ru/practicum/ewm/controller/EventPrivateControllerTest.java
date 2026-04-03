package ru.practicum.ewm.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.LocationDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.service.EventService;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventPrivateController.class)
class EventPrivateControllerTest {

    @MockitoBean
    EventService eventService;
    @Autowired
    ObjectMapper mapper;
    LocationDto location = new LocationDto(33.56, 76.87);
    CategoryDto category = new CategoryDto(3L, "Цирк");
    UserShortDto initiator = new UserShortDto(1L, "Коля");
    NewEventDto newEventDto = NewEventDto.builder()
            .annotation("новое очень интересное событие")
            .category(3L)
            .description("супер новое очень интересное событие")
            .eventDate("2026-10-23 15:30:00")
            .location(location)
            .paid(true)
            .participantLimit(200)
            .requestModeration(true)
            .title("летающие слоны")
            .build();
    EventFullDto fullDtoForResponse = EventFullDto.builder()
            .annotation("новое очень интересное событие")
            .category(category)
            .confirmedRequests(153L)
            .createdOn("2026-05-15 10:25:46")
            .description("супер новое очень интересное событие")
            .eventDate("2026-10-23 15:30:00")
            .id(7L)
            .initiator(initiator)
            .location(location)
            .paid(true)
            .participantLimit(200)
            .publishedOn("2026-05-16 18:46:13")
            .requestModeration(true)
            .state("PUBLISHED")
            .title("летающие слоны")
            .views(567L)
            .build();
    @Autowired
    private MockMvc mvc;

    @Test
    void testAddEvent() throws Exception {
        when(eventService.addEvent(1L, newEventDto))
                .thenReturn(fullDtoForResponse);

        mvc.perform(post("/users/1/events")
                        .content(mapper.writeValueAsString(newEventDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.annotation", is(fullDtoForResponse.getAnnotation())))
                .andExpect(jsonPath("$.category.id",
                        is(fullDtoForResponse.getCategory().getId()), Long.class))
                .andExpect(jsonPath("$.category.name", is(fullDtoForResponse.getCategory().getName())))
                .andExpect(jsonPath("$.confirmedRequests",
                        is(fullDtoForResponse.getConfirmedRequests()), Long.class))
                .andExpect(jsonPath("$.createdOn", is(fullDtoForResponse.getCreatedOn())))
                .andExpect(jsonPath("$.description", is(fullDtoForResponse.getDescription())))
                .andExpect(jsonPath("$.eventDate", is(fullDtoForResponse.getEventDate())))
                .andExpect(jsonPath("$.id", is(fullDtoForResponse.getId()), Long.class))
                .andExpect(jsonPath("$.initiator.id",
                        is(fullDtoForResponse.getInitiator().getId()), Long.class))
                .andExpect(jsonPath("$.initiator.name", is(fullDtoForResponse.getInitiator().getName())))
                .andExpect(jsonPath("$.location.lat",
                        is(fullDtoForResponse.getLocation().getLat()), Double.class))
                .andExpect(jsonPath("$.location.lon",
                        is(fullDtoForResponse.getLocation().getLon()), Double.class))
                .andExpect(jsonPath("$.paid", is(fullDtoForResponse.getPaid())))
                .andExpect(jsonPath("$.participantLimit",
                        is(fullDtoForResponse.getParticipantLimit()), Integer.class))
                .andExpect(jsonPath("$.publishedOn", is(fullDtoForResponse.getPublishedOn())))
                .andExpect(jsonPath("$.requestModeration", is(fullDtoForResponse.getRequestModeration())))
                .andExpect(jsonPath("$.state", is(fullDtoForResponse.getState())))
                .andExpect(jsonPath("$.title", is(fullDtoForResponse.getTitle())))
                .andExpect(jsonPath("$.views", is(fullDtoForResponse.getViews()), Long.class));

        verify(eventService, times(1)).addEvent(1L, newEventDto);
    }

    @Test
    void testGetEventsOfUser() throws Exception {
        EventShortDto dto1 = EventShortDto.builder()
                .annotation("новое очень интересное событие")
                .category(category)
                .confirmedRequests(153L)
                .eventDate("2026-10-23 15:30:00")
                .id(7L)
                .initiator(initiator)
                .paid(true)
                .title("летающие слоны")
                .views(567L)
                .build();

        EventShortDto dto2 = EventShortDto.builder()
                .annotation("рок-концерт вселенского масштаба")
                .category(category)
                .confirmedRequests(10654L)
                .eventDate("2026-11-12 10:00:00")
                .id(7L)
                .initiator(initiator)
                .paid(true)
                .title("поющие электронные гитары")
                .views(64516L)
                .build();

        List<EventShortDto> dtos = List.of(dto1, dto2);

        when(eventService.getEventsOfUser(1L, 0, 10))
                .thenReturn(dtos);

        mvc.perform(get("/users/1/events")
                        .queryParam("from", "0")
                        .queryParam("size", "10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].annotation", is(dto1.getAnnotation())))
                .andExpect(jsonPath("$.[0].category.id", is(dto1.getCategory().getId()), Long.class))
                .andExpect(jsonPath("$.[0].category.name", is(dto1.getCategory().getName())))
                .andExpect(jsonPath("$.[0].confirmedRequests", is(dto1.getConfirmedRequests()), Long.class))
                .andExpect(jsonPath("$.[0].eventDate", is(dto1.getEventDate())))
                .andExpect(jsonPath("$.[0].id", is(dto1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].initiator.id", is(dto1.getInitiator().getId()), Long.class))
                .andExpect(jsonPath("$.[0].initiator.name", is(dto1.getInitiator().getName())))
                .andExpect(jsonPath("$.[0].paid", is(dto1.getPaid())))
                .andExpect(jsonPath("$.[0].title", is(dto1.getTitle())))
                .andExpect(jsonPath("$.[0].views", is(dto1.getViews()), Long.class))

                .andExpect(jsonPath("$.[1].annotation", is(dto2.getAnnotation())))
                .andExpect(jsonPath("$.[1].category.id", is(dto2.getCategory().getId()), Long.class))
                .andExpect(jsonPath("$.[1].category.name", is(dto2.getCategory().getName())))
                .andExpect(jsonPath("$.[1].confirmedRequests", is(dto2.getConfirmedRequests()), Long.class))
                .andExpect(jsonPath("$.[1].eventDate", is(dto2.getEventDate())))
                .andExpect(jsonPath("$.[1].id", is(dto2.getId()), Long.class))
                .andExpect(jsonPath("$.[1].initiator.id", is(dto2.getInitiator().getId()), Long.class))
                .andExpect(jsonPath("$.[1].initiator.name", is(dto2.getInitiator().getName())))
                .andExpect(jsonPath("$.[1].paid", is(dto2.getPaid())))
                .andExpect(jsonPath("$.[1].title", is(dto2.getTitle())))
                .andExpect(jsonPath("$.[1].views", is(dto2.getViews()), Long.class));

        verify(eventService, times(1)).getEventsOfUser(1L, 0, 10);
    }

    @Test
    void testGetEventById() throws Exception {
        when(eventService.getEventById(1L, 7L))
                .thenReturn(fullDtoForResponse);

        mvc.perform(get("/users/1/events/7")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.annotation", is(fullDtoForResponse.getAnnotation())))
                .andExpect(jsonPath("$.category.id",
                        is(fullDtoForResponse.getCategory().getId()), Long.class))
                .andExpect(jsonPath("$.category.name", is(fullDtoForResponse.getCategory().getName())))
                .andExpect(jsonPath("$.confirmedRequests",
                        is(fullDtoForResponse.getConfirmedRequests()), Long.class))
                .andExpect(jsonPath("$.createdOn", is(fullDtoForResponse.getCreatedOn())))
                .andExpect(jsonPath("$.description", is(fullDtoForResponse.getDescription())))
                .andExpect(jsonPath("$.eventDate", is(fullDtoForResponse.getEventDate())))
                .andExpect(jsonPath("$.id", is(fullDtoForResponse.getId()), Long.class))
                .andExpect(jsonPath("$.initiator.id",
                        is(fullDtoForResponse.getInitiator().getId()), Long.class))
                .andExpect(jsonPath("$.initiator.name", is(fullDtoForResponse.getInitiator().getName())))
                .andExpect(jsonPath("$.location.lat",
                        is(fullDtoForResponse.getLocation().getLat()), Double.class))
                .andExpect(jsonPath("$.location.lon",
                        is(fullDtoForResponse.getLocation().getLon()), Double.class))
                .andExpect(jsonPath("$.paid", is(fullDtoForResponse.getPaid())))
                .andExpect(jsonPath("$.participantLimit",
                        is(fullDtoForResponse.getParticipantLimit()), Integer.class))
                .andExpect(jsonPath("$.publishedOn", is(fullDtoForResponse.getPublishedOn())))
                .andExpect(jsonPath("$.requestModeration", is(fullDtoForResponse.getRequestModeration())))
                .andExpect(jsonPath("$.state", is(fullDtoForResponse.getState())))
                .andExpect(jsonPath("$.title", is(fullDtoForResponse.getTitle())))
                .andExpect(jsonPath("$.views", is(fullDtoForResponse.getViews()), Long.class));

        verify(eventService, times(1)).getEventById(1L, 7L);
    }

}
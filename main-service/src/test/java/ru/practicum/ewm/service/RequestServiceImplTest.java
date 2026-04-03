package ru.practicum.ewm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.dto.request.CreateUpdateRequestDto;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.model.request.ParticipationRequest;
import ru.practicum.ewm.model.request.RequestStatus;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequestServiceImpl Unit Tests")
class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private RequestServiceImpl requestService;

    private User testUser;
    private User eventInitiator;
    private Event testEvent;
    private ParticipationRequest testRequest;
    private CreateUpdateRequestDto createDto;

    @BeforeEach
    void setUp() {
        eventInitiator = new User(
                10L,
                "Event Initiator",
                "initiator@example.com",
                null
        );

        testUser = new User(
                1L,
                "Test User",
                "test@example.com",
                null
        );

        testEvent = Event.builder()
                .id(2L)
                .title("Test Event")
                .eventDate(LocalDateTime.now().plusDays(5))
                .state(EventState.PUBLISHED)
                .initiator(eventInitiator)
                .participantLimit(10)
                .requestModeration(true)
                .build();

        testRequest = ParticipationRequest.builder()
                .id(3L)
                .created(LocalDateTime.now())
                .event(testEvent)
                .requester(testUser)
                .status(RequestStatus.PENDING)
                .build();

        createDto = CreateUpdateRequestDto.builder()
                .userId(1L)
                .eventId(2L)
                .build();
    }

    @Test
    @DisplayName("Создание заявки - успешный сценарий")
    void createRequest_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(testEvent));
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(testRequest);

        ParticipationRequestDto result = requestService.createRequest(createDto);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals(2L, result.getEventId());
        assertEquals(1L, result.getRequesterId());
        assertEquals("PENDING", result.getStatus());

        verify(userRepository).findById(1L);
        verify(eventRepository).findById(2L);
        verify(requestRepository).save(any(ParticipationRequest.class));
    }

    @Test
    @DisplayName("Получение списка заявок пользователя - успешный сценарий")
    void getRequestByUserId_Success() {
        when(requestRepository.findAllByUserId(1L)).thenReturn(List.of(testRequest));

        List<ParticipationRequestDto> result = requestService.getRequestByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getId());
        assertEquals(2L, result.get(0).getEventId());

        verify(requestRepository).findAllByUserId(1L);
    }

    @Test
    @DisplayName("Получение списка заявок - пустой список")
    void getRequestByUserId_EmptyList() {
        when(requestRepository.findAllByUserId(1L)).thenReturn(List.of());

        List<ParticipationRequestDto> result = requestService.getRequestByUserId(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(requestRepository).findAllByUserId(1L);
    }

    @Test
    @DisplayName("Отмена заявки - пользователь не найден (404)")
    void canceledRequest_UserNotFound() {
        CreateUpdateRequestDto cancelDto = CreateUpdateRequestDto.builder()
                .eventId(2L)
                .userId(1L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                requestService.canceledRequest(1L, 3L)
        );

        verify(userRepository).findById(1L);
        verify(eventRepository, never()).findById(any());
    }

}
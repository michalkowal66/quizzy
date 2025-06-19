package com.example.quizzy.services;

import com.example.quizzy.dto.QuizDto;
import com.example.quizzy.dto.QuizResponseDto;
import com.example.quizzy.entity.Quiz;
import com.example.quizzy.entity.User;
import com.example.quizzy.repository.QuizRepository;
import com.example.quizzy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Quiz Service Implementation Tests")
class QuizServiceImplTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private QuizServiceImpl quizService;

    private User testUser;
    private Quiz testQuiz;
    private QuizDto quizDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testQuiz = new Quiz();
        testQuiz.setId(10L);
        testQuiz.setTitle("Initial Title");
        testQuiz.setDescription("Initial Description");
        testQuiz.setAuthor(testUser);

        quizDto = new QuizDto();
        quizDto.setTitle("New Quiz Title");
        quizDto.setDescription("New Quiz Description");
    }

    private void mockSecurityContext() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        lenient().when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn("testuser");
    }

    @Test
    @DisplayName("createQuiz should save and map quiz correctly")
    void createQuiz_shouldSaveAndReturnQuizResponseDto() {
        // Arrange
        mockSecurityContext();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> {
            Quiz quiz = invocation.getArgument(0);
            quiz.setId(10L); // Simulate ID generation on save
            return quiz;
        });

        // Act
        QuizResponseDto responseDto = quizService.createQuiz(quizDto);

        // Assert
        assertNotNull(responseDto);
        assertEquals(quizDto.getTitle(), responseDto.getTitle());
        assertEquals(testUser.getUsername(), responseDto.getAuthorUsername());

        // Capture the argument passed to quizRepository.save to verify its properties
        ArgumentCaptor<Quiz> quizCaptor = ArgumentCaptor.forClass(Quiz.class);
        verify(quizRepository).save(quizCaptor.capture());
        Quiz savedQuiz = quizCaptor.getValue();

        assertEquals(quizDto.getTitle(), savedQuiz.getTitle());
        assertEquals(testUser, savedQuiz.getAuthor());
    }

    @Test
    @DisplayName("getQuizById should return DTO when quiz exists")
    void getQuizById_whenQuizExists_shouldReturnQuizResponseDto() {
        // Arrange
        when(quizRepository.findById(10L)).thenReturn(Optional.of(testQuiz));

        // Act
        QuizResponseDto responseDto = quizService.getQuizById(10L);

        // Assert
        assertNotNull(responseDto);
        assertEquals(testQuiz.getTitle(), responseDto.getTitle());
        assertEquals(testUser.getUsername(), responseDto.getAuthorUsername());
    }

    @Test
    @DisplayName("getQuizById should throw exception when quiz does not exist")
    void getQuizById_whenQuizNotFound_shouldThrowException() {
        // Arrange
        when(quizRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> quizService.getQuizById(99L));
    }

    @Test
    @DisplayName("getCurrentUserQuizzes should return quizzes for the current user")
    void getCurrentUserQuizzes_shouldReturnListOfUserQuizzes() {
        // Arrange
        mockSecurityContext();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(quizRepository.findByAuthorId(1L)).thenReturn(Collections.singletonList(testQuiz));

        // Act
        List<QuizResponseDto> quizzes = quizService.getCurrentUserQuizzes();

        // Assert
        assertNotNull(quizzes);
        assertEquals(1, quizzes.size());
        assertEquals(testQuiz.getTitle(), quizzes.get(0).getTitle());
        verify(quizRepository).findByAuthorId(1L);
    }

    @Test
    @DisplayName("getCurrentUserQuizzes should throw exception when user not found")
    void getCurrentUserQuizzes_whenUserNotFound_shouldThrowException() {
        // Arrange
        mockSecurityContext();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> quizService.getCurrentUserQuizzes());
    }


    @Test
    @DisplayName("updateQuiz should correctly update fields and save")
    void updateQuiz_whenQuizExists_shouldUpdateAndReturnDto() {
        // Arrange
        when(quizRepository.findById(10L)).thenReturn(Optional.of(testQuiz));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuizDto updateDto = new QuizDto();
        updateDto.setTitle("Updated Title");
        updateDto.setDescription("Updated Description");

        // Act
        QuizResponseDto responseDto = quizService.updateQuiz(10L, updateDto);

        // Assert
        assertNotNull(responseDto);
        assertEquals("Updated Title", responseDto.getTitle());
        assertEquals("Updated Description", responseDto.getDescription());

        // Capture the argument to verify the state of the entity before saving
        ArgumentCaptor<Quiz> quizCaptor = ArgumentCaptor.forClass(Quiz.class);
        verify(quizRepository).save(quizCaptor.capture());
        assertEquals("Updated Title", quizCaptor.getValue().getTitle());
    }

    @Test
    @DisplayName("deleteQuiz should call repository delete method when quiz exists")
    void deleteQuiz_whenQuizExists_shouldCallDeleteById() {
        // Arrange
        when(quizRepository.existsById(10L)).thenReturn(true);
        doNothing().when(quizRepository).deleteById(10L);

        // Act
        quizService.deleteQuiz(10L);

        // Assert / Verify
        verify(quizRepository, times(1)).existsById(10L);
        verify(quizRepository, times(1)).deleteById(10L);
    }

    @Test
    @DisplayName("deleteQuiz should throw exception when quiz does not exist")
    void deleteQuiz_whenQuizNotFound_shouldThrowException() {
        // Arrange
        when(quizRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> quizService.deleteQuiz(99L));
        verify(quizRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("getAllQuizzes should return a list of all quiz DTOs")
    void getAllQuizzes_shouldReturnAllQuizDtos() {
        // Arrange
        Quiz quiz2 = new Quiz();
        quiz2.setId(20L);
        quiz2.setTitle("Another Quiz");
        quiz2.setAuthor(testUser);

        List<Quiz> quizList = List.of(testQuiz, quiz2);
        when(quizRepository.findAll()).thenReturn(quizList);

        // Act
        List<QuizResponseDto> result = quizService.getAllQuizzes();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Initial Title", result.get(0).getTitle());
        assertEquals("Another Quiz", result.get(1).getTitle());

        // Verify
        verify(quizRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("updateQuiz should throw exception when quiz does not exist")
    void updateQuiz_whenQuizNotFound_shouldThrowException() {
        // Arrange
        long nonExistentQuizId = 99L;
        QuizDto updateDto = new QuizDto();
        updateDto.setTitle("Non Existent");

        when(quizRepository.findById(nonExistentQuizId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            quizService.updateQuiz(nonExistentQuizId, updateDto);
        });

        // Verify
        verify(quizRepository, times(1)).findById(nonExistentQuizId);
        verify(quizRepository, never()).save(any(Quiz.class));
    }
}
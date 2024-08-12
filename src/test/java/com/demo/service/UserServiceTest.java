package com.demo.service;

import com.demo.dto.AccountDTO;
import com.demo.entities.User;
import com.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setFirstName("Hector");
        user.setLastName("Dof");
        user.setUsername("HectorDof");
        user.setEmail("johndoe@example.com");
    }

    @Test
    void createNewUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        User createdUser = userService.createNewUser(user);

        assertEquals(user.getId(), createdUser.getId());
        assertEquals(user.getFirstName(), createdUser.getFirstName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testCreateNewUserFailure() {
        // Simulate a failure scenario where save returns null
        when(userRepository.save(any(User.class))).thenReturn(null);

        User createdUser = userService.createNewUser(user);

        assertNull(createdUser, "The created user should be null if save fails.");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void fetchAll() {
        List<User> users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);

        List<User> fetchedUsers = userService.fetchAll();

        assertEquals(1, fetchedUsers.size());
        assertEquals(user.getId(), fetchedUsers.get(0).getId());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testFetchAllFailure() {
        // Simulate a failure scenario where findAll returns an empty list
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> fetchedUsers = userService.fetchAll();

        assertTrue(fetchedUsers.isEmpty(), "The fetched user list should be empty if findAll fails.");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testFetchByUsername() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        Optional<User> foundUser = userRepository.findByUsername("HectorDof");

        assertTrue(foundUser.isPresent());
        assertEquals(user.getUsername(), foundUser.get().getUsername());
        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    public void testFetchByUsernameFailure() {
        // Simulate a failure scenario where findByUsername returns an empty Optional
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        Optional<User> foundUser = userRepository.findByUsername("unknownuser");

        assertFalse(foundUser.isPresent(), "The found user should be empty if findByUsername fails.");
        verify(userRepository, times(1)).findByUsername(anyString());
    }
}
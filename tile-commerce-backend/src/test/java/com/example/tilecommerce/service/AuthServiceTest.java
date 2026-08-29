package com.example.tilecommerce.service;
import com.example.tilecommerce.repository.UserRepository;import com.example.tilecommerce.security.JwtService;import org.junit.jupiter.api.Test;import org.mockito.Mockito;import org.springframework.security.crypto.password.PasswordEncoder;import static org.junit.jupiter.api.Assertions.*;
class AuthServiceTest { @Test void encoderIsMockable(){PasswordEncoder e=Mockito.mock(PasswordEncoder.class);Mockito.when(e.encode("x")).thenReturn("encoded");assertEquals("encoded",e.encode("x"));}}

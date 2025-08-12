package kirill.zhdanov.fastauthapi;

import kirill.zhdanov.fastauthapi.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Сначала настраиваем моки
        when(cacheManager.getCache("auth")).thenReturn(cache);
        // А затем вручную создаем сервис с этими моками
        authService = new AuthService(passwordEncoder, cacheManager);
    }

    @Test
    void storeAndVerify_success() {
        // Arrange (Настройка)
        String username = "user1";
        String password = "pass123";
        String hashedPassword = "hashed_pass123";

        // Настраиваем, что вернет хешер при кодировании пароля
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);

        // Act (Действие): Регистрируем пользователя
        // Метод storeUser должен положить хеш в кэш
        authService.storeUser(username, password);

        // Проверяем, что метод put был вызван на кэше с правильными данными
        verify(cache).put(username, hashedPassword);

        // Arrange для проверки: настраиваем, что вернет кэш и хешер при проверке
        when(cache.get(username, String.class)).thenReturn(hashedPassword);
        when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);

        // Assert (Проверка): Убеждаемся, что пользователь теперь присутствует
        assertTrue(authService.isUserPresent(username, password));
    }

    @Test
    void verify_wrongPassword() {
        // Arrange
        String username = "user1";
        String wrongPassword = "wrong";
        String hashedPassword = "hashed_pass123";

        // Предполагаем, что пользователь уже есть в кэше
        when(cache.get(username, String.class)).thenReturn(hashedPassword);
        // Настраиваем, что проверка неверного пароля вернет false
        when(passwordEncoder.matches(wrongPassword, hashedPassword)).thenReturn(false);

        // Act & Assert
        assertFalse(authService.isUserPresent(username, wrongPassword));
    }

    @Test
    void verify_unknownUser() {
        // Arrange
        // Настраиваем, что кэш вернет null для несуществующего пользователя
        when(cache.get("noone", String.class)).thenReturn(null);

        // Act & Assert
        assertFalse(authService.isUserPresent("noone", "any"));
        // Убедимся, что метод matches даже не вызывался
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}

package kirill.zhdanov.fastauthapi.service;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final PasswordEncoder encoder;
    private final Cache cache;

    public AuthService(PasswordEncoder encoder, CacheManager cacheManager) {
        this.encoder = encoder;
        this.cache = cacheManager.getCache("auth");
    }

    /**
     * Хеширует и сохраняет пароль пользователя в кэш.
     * Аннотация @CachePut всегда выполняет метод и помещает результат в кэш.
     *
     * @param username    имя пользователя (ключ кэша)
     * @param rawPassword сырой пароль для хеширования
     * @return хешированный пароль
     */
    @CachePut(value = "auth", key = "#username")
    public String storeUser(String username, String rawPassword) {
        String hashedPassword = encoder.encode(rawPassword);
        cache.put(username, hashedPassword);
        return hashedPassword;
    }

    /**
     * Проверяет, присутствует ли пользователь в кэше с верным паролем.
     *
     * @param username    имя пользователя
     * @param rawPassword сырой пароль
     * @return true, если пользователь существует и пароль совпадает
     */
    public boolean isUserPresent(String username, String rawPassword) {
        String cachedHash = cache.get(username, String.class);
        return cachedHash != null && encoder.matches(rawPassword, cachedHash);
    }

    public String getHashPassword(String username) {
        return cache.get(username, String.class);
    }
}

package kirill.zhdanov.fastauthapi.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AuthService authService;

    public UserDetailsServiceImpl(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String hashedPassword = authService.getHashPassword(username);
        if (hashedPassword == null) {
            throw new UsernameNotFoundException("Пользователь не найден: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                username,
                hashedPassword,
                true,
                true,
                true, true, new java.util.ArrayList<>());
    }
}

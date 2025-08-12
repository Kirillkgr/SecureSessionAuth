package kirill.zhdanov.fastauthapi.controller;

import kirill.zhdanov.fastauthapi.model.UserDto;
import kirill.zhdanov.fastauthapi.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody UserDto dto) {
        authService.storeUser(dto.username(), dto.password());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check")
    public ResponseEntity<Boolean> check(@RequestBody UserDto dto) {
        boolean exists = authService.isUserPresent(dto.username(), dto.password());
        return ResponseEntity.ok(exists);
    }
}

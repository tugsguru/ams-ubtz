package ams.mn.ubtz.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class MixedPasswordEncoder implements PasswordEncoder {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword != null && encodedPassword.startsWith("$2a$")) {
            return bcrypt.matches(rawPassword, encodedPassword); // BCrypt формат
        } else {
            return rawPassword.toString().equals(encodedPassword); // Plain text
        }
    }
}

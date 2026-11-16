package cat.copernic.nothinganpri.config;

import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.entity.UsuariJpaEntity;
import cat.copernic.nothinganpri.repository.SolicitudValidacionRepository;
import cat.copernic.nothinganpri.repository.UsuariJpaRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * Si el compte està desactivat, indica el motiu a la pàgina de login:
 *  - pendent: el registre espera la validació d'un agent
 *  - rebutjat: l'agent ha rebutjat el registre
 *  - disabled: compte desactivat per un altre motiu
 * Només es revela si la contrasenya és correcta.
 */
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final UsuariJpaRepository usuariRepository;
    private final SolicitudValidacionRepository solicitudRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginFailureHandler(
            UsuariJpaRepository usuariRepository,
            SolicitudValidacionRepository solicitudRepository,
            PasswordEncoder passwordEncoder) {

        this.usuariRepository = usuariRepository;
        this.solicitudRepository = solicitudRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        String destino = "/login?error";

        if (exception instanceof DisabledException) {
            destino = "/login?error=" + motivo(
                    request.getParameter("email"),
                    request.getParameter("password"));
        }

        response.sendRedirect(request.getContextPath() + destino);
    }

    private String motivo(String email, String password) {

        if (email == null || password == null) {
            return "disabled";
        }

        Optional<UsuariJpaEntity> trobat =
                usuariRepository.findByEmail(email.trim());

        if (trobat.isEmpty()
                || !passwordEncoder.matches(password,
                        trobat.get().getPassword())) {
            return "disabled";
        }

        if (trobat.get() instanceof ClientJpaEntity) {

            ClientJpaEntity client = (ClientJpaEntity) trobat.get();

            if (solicitudRepository.existsByClienteAndTipoAndEstado(
                    client, "REGISTRO", "PENDIENTE")) {
                return "pendent";
            }

            if (solicitudRepository.existsByClienteAndTipoAndEstado(
                    client, "REGISTRO", "RECHAZADA")) {
                return "rebutjat";
            }
        }

        return "disabled";
    }
}
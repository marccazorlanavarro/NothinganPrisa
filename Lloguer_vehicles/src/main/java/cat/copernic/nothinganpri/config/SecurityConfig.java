package cat.copernic.nothinganpri.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            LoginFailureHandler loginFailureHandler)
            throws Exception {

        http
                // CSRF
                .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/chatbot/**")
                )
                // AUTORITZACIONS
                .authorizeHttpRequests(auth -> auth
                // RUTES PUBLIQUES
                .requestMatchers(
                        "/",
                        "/index",
                        "/login",
                        "/registre",
                        "/privacitat",
                        "/documentacio",
                        "/revisio",
                        "/confirmacio",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/uploads/**",
                        "/vehicles/*/foto",
                        "/api/chatbot/**",
                        "/corsa2005.png",
                        "/favicon.ico",
                        "/error",
                        "/recuperar-contrasenya",
                        "/reset-password",
                        "/verificar-codi",
                        "/nova-contrasenya"
                ).permitAll()
                // ADMINISTRACIO
                .requestMatchers("/admin/**")
                    .hasAnyRole("ADMIN", "ADMINISTRADOR")

                // Informe històric de reserves (RF91) — només admin
                .requestMatchers("/reserva/informe", "/reserva/informe/**",
                                 "/reserva/informe-client/**")
                    .hasAnyRole("ADMIN", "ADMINISTRADOR")

                // AGENTS I GESTIO DE SOL·LICITUDS
                .requestMatchers(
                        "/agent/**"
                ).hasAnyRole(
                        "ADMIN",
                        "ADMINISTRADOR",
                        "AGENT"
                )
                // GESTIO DE CLIENTS I AGENTS
                .requestMatchers(
                        "/clients",
                        "/clients/**",
                        "/agents",
                        "/agents/**",
                        "/reserva/lliurar/**",
                        "/reserva/retornar/**",
                        "/vehicles/nou",
                        "/vehicles/*/editar",
                        "/vehicles/desa",
                        "/vehicles/*/baixa",
                        "/vehicles/*/activar"
                ).hasAnyRole(
                        "AGENT",
                        "ADMIN",
                        "ADMINISTRADOR"
                )
                // CATÀLEG DE VEHICLES
                .requestMatchers("/vehicles")
                .authenticated()
                // CLIENTS
                .requestMatchers("/client/**")
                .hasRole("CLIENT")
                // PERFIL
                .requestMatchers(
                        "/perfil/**",
                        "/perfil"
                ).authenticated()
                // RESTA DE RUTES
                .anyRequest().authenticated()
                )
                // LOGIN
                .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/", false)
                .failureHandler(loginFailureHandler)
                .permitAll()
                )
                // LOGOUT
                .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                );

        return http.build();
    }

    // PASSWORD ENCODER
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}
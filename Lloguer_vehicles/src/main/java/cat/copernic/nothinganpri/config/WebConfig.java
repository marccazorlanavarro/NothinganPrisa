package cat.copernic.nothinganpri.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir}")
private String uploadDir;

@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
    try {
        Files.createDirectories(dir);
    } catch (IOException e) {
        throw new IllegalStateException("No es pot crear " + dir, e);
    }
    System.out.println(">>> Servint /uploads/** des de: " + dir);

    registry.addResourceHandler("/uploads/**")
            .addResourceLocations(dir.toUri().toString());
}
}
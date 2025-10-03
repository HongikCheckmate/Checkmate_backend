package project.project1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Slf4j
@SpringBootApplication
@EnableJpaAuditing
public class Project1Application {

	public static void main(String[] args) {
		SpringApplication.run(Project1Application.class, args);

		log.info("Test #5 for CI/CD!");
	}

}

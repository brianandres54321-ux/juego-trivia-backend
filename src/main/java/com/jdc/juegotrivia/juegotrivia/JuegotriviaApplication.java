package com.jdc.juegotrivia.juegotrivia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JuegotriviaApplication {

	public static void main(String[] args) {
		System.setProperty("file.encoding", "UTF-8");
		SpringApplication.run(JuegotriviaApplication.class, args);
		System.out.println("✅ Aplicación Trivia iniciada con éxito en http://localhost:8080");
	}


}

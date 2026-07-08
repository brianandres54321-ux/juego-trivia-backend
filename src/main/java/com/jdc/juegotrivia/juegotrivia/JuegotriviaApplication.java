package com.jdc.juegotrivia.juegotrivia;

import com.jdc.juegotrivia.juegotrivia.servidor.TriviaServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@SpringBootApplication
public class JuegotriviaApplication {

	@Autowired
	private TriviaServer triviaServer;

	public static void main(String[] args) {
		System.setProperty("file.encoding", "UTF-8");
		SpringApplication.run(JuegotriviaApplication.class, args);
		System.out.println("✅ Aplicación Trivia iniciada con éxito en http://localhost:8080");
	}


}

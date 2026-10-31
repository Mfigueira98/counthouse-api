package io.github.mfigueira98.counthouse;

import org.springframework.boot.SpringApplication;

public class TestCounthouseApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(CounthouseApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

package org.sharemolangapp.smlapp;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import javafx.application.Application;


@SpringBootApplication
public class SmlappApplication {

	public static void main(String[] args) {
//		SpringApplication.run(SmlappApplication.class, args);
		Application.launch(SmlFxApplication.class, args);
	}

}

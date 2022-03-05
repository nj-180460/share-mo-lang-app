package org.sharemolangapp.smlapp;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;



public class SmlFxApplication extends Application{

	private ConfigurableApplicationContext applicationContext;
	
	@Override
	public void init() {
		applicationContext = new SpringApplicationBuilder(SmlappApplication.class).run();
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		applicationContext.publishEvent(new StageReadyEvent(stage));
	}
	
	@Override
	public void stop() {
		applicationContext.close();
		Platform.exit();
	}
	
	static class StageReadyEvent extends ApplicationEvent{
		private static final long serialVersionUID = 1L;

		public StageReadyEvent(Stage stage) {
			super(stage);
		}
		
		public Stage getStage() {
			return ((Stage)getSource());
		}
		
	}

}

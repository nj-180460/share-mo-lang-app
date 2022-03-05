package org.sharemolangapp.smlapp;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.sharemolangapp.smlapp.SmlFxApplication.StageReadyEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent>{
	
	private static Scene scene;
	
	private final String appTitle;
	@Value("classpath:/fxml/home.fxml")
	private Resource homeResource;
	@Value("classpath:/fxml/sender.fxml")
	private Resource senderResource;
	@Value("classpath:/fxml/receiver.fxml")
	private Resource receiverResource;
	
	public StageInitializer(@Value("${spring.application.ui.title}") String appTitle) {
		this.appTitle = appTitle;
	}
	
	@Override
	public void onApplicationEvent(StageReadyEvent event) {
		Stage stage = event.getStage();
		stage.centerOnScreen();
		
		try {
			setupStage(stage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void setupStage(Stage stage) throws IOException {
//		stage.setMinHeight(580);
//    	stage.setMinWidth(700);
        stage.setTitle(appTitle);
        
        RootManager rootManager = RootManager.getRootManager();
        rootManager.registerResource(RootManager.FXML_HOME, homeResource);
        rootManager.registerResource(RootManager.FXML_SENDER, senderResource);
        rootManager.registerResource(RootManager.FXML_RECEIVER, receiverResource);
        
        scene = new Scene(rootManager.loadFXML(homeResource));
        stage.setScene(scene);
//        stage.setFullScreen(true);
//        stage.setMaximized(true);
        stage.show();
	}
	
	
	public static class RootManager {
		
		public static final String FXML_HOME = "home";
		public static final String FXML_SENDER = "sender";
		public static final String FXML_RECEIVER = "receiver";
		
		private static RootManager rootManager;
		private static final LinkedHashMap<String, Resource> registeredResources = new LinkedHashMap<>(); 
		
		private FXMLLoader fxmlLoader;
		private Parent parent;
		
		private RootManager() {
			
		}
		
		public static RootManager getRootManager() {
			return rootManager != null ? rootManager : new RootManager(); 
		}
		
		public void registerResource(String keyName, Resource resource) {
			registeredResources.put(keyName, resource);
		}
		
		public Parent loadFXML(Resource resource) throws IOException {
//			FXMLLoader fxmlLoader = new FXMLLoader(StageInitializer.class.getResource("fxml/"+fxml + ".fxml"));
			fxmlLoader = new FXMLLoader(resource.getURL());
			return (parent = fxmlLoader.load());
		}
		
		public LinkedHashMap<String, Resource> getRegisteredResources(){
			return registeredResources;
		}
		
		public Parent getParent() {
			return parent;
		}
		
		public FXMLLoader getFXMLLoader() {
			return fxmlLoader;
		}
		
//		private void setRoot(Resource resource) throws IOException {
//	        scene.setRoot(loadFXML(resource));
//	    }
//		
//		private void setRoot(String keyName) throws IOException {
//	        scene.setRoot(loadFXML(registeredResources.get(keyName)));
//	    }
		
		
	}
}

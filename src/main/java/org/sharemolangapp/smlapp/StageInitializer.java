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
	
	private static final RootManager ROOT_MANAGER = new RootManager();
	private static Scene scene;
	
	
	private final String appTitle;
	@Value("classpath:/fxml/home.fxml")
	private Resource homeResource;
	@Value("classpath:/fxml/sender.fxml")
	private Resource senderResource;
	@Value("classpath:/fxml/receiver.fxml")
	private Resource receiverResource;
	@Value("classpath:/fxml/connectionPropertiesDialog.fxml")
	private Resource connectionPropertiesDialog;
	@Value("classpath:/fxml/manageConnection.fxml")
	private Resource manageConnection;
	@Value("classpath:/fxml/generalUseBorderPane.fxml")
	private Resource generalUseBorderPane;
	
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
		stage.setMinHeight(550);
    	stage.setMinWidth(285);
        stage.setTitle(appTitle);
        
        RootManager rootManager = RootManager.getRootManager();
        rootManager.registerResource(RootManager.FXML_HOME, homeResource);
        rootManager.registerResource(RootManager.FXML_SENDER, senderResource);
        rootManager.registerResource(RootManager.FXML_RECEIVER, receiverResource);
        rootManager.registerResource(RootManager.FXML_CONNECTION_PROPERTIES_DIALOG, connectionPropertiesDialog);
        rootManager.registerResource(RootManager.FXML_MANAGE_CONNECTION, manageConnection);
        rootManager.registerResource(RootManager.FXML_GENERAL_USER_BORDERPANE, generalUseBorderPane);
        
        scene = new Scene(rootManager.loadFXML(homeResource));
        rootManager.loadFXML(homeResource);
        stage.setScene(scene);
//        stage.setFullScreen(true);
//        stage.setMaximized(true);
        stage.show();
	}
	
	
	
	
	
	public static class RootManager {
		
		public static final String FXML_HOME = "home";
		public static final String FXML_SENDER = "sender";
		public static final String FXML_RECEIVER = "receiver";
		public static final String FXML_CONNECTION_PROPERTIES_DIALOG = "connectionPropertiesDialog";
		public static final String FXML_MANAGE_CONNECTION = "manageConnection";
		public static final String FXML_GENERAL_USER_BORDERPANE = "generalUseBorderPane";
		
		private static final LinkedHashMap<String, Resource> registeredResources = new LinkedHashMap<>();
		
		private static FXMLLoader fxmlLoader;
		private static Parent parent;
		
		private RootManager() {
			
		}
		
		public static RootManager getRootManager() {
			return ROOT_MANAGER; 
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

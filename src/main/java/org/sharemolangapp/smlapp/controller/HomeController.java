package org.sharemolangapp.smlapp.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.sharemolangapp.smlapp.StageInitializer.RootManager;
import org.sharemolangapp.smlapp.receiver.ReceiverController;
import org.sharemolangapp.smlapp.sender.SenderController;
import org.springframework.stereotype.Component;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


@Component
public class HomeController implements Initializable {

	private final RootManager rootManager = RootManager.getRootManager();
	@FXML private BorderPane homeBorderPane; 
//	private ArrayList<String> queueList = new ArrayList<>();
	
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initMenu();
	}
	
	
	
	@FXML
	private void handleHomeSendButton(ActionEvent actionEvent) throws IOException {
//		@SuppressWarnings("unchecked")
//		RootManager<SenderController> rootManager = (RootManager<SenderController>) RootManager.getRootManager();
//		rootManager.setRoot(RootManager.FXML_SENDER);
//		SenderController sender = rootManager.getController();
//		sender.setQueueList(queueList);
		
		Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
		Scene scene = stage.getScene();
		
		scene.setRoot(rootManager.loadFXML(rootManager.getRegisteredResources().get(RootManager.FXML_SENDER)));
		
		SenderController senderController = rootManager.getFXMLLoader().getController();
		senderController.connectToServerDialog();
		stage.setOnCloseRequest( (stageHandle) -> {
			senderController.closeAll();
			stage.close();
		});
	}
	
	
	@FXML
	private void handleHomeReceiveButton(ActionEvent actionEvent) throws IOException {
//		rootManager.setRoot(RootManager.FXML_RECEIVER);
		
		Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
		Scene scene = stage.getScene();
		
		scene.setRoot(rootManager.loadFXML(rootManager.getRegisteredResources().get(RootManager.FXML_RECEIVER)));
		
		ReceiverController receiverController = rootManager.getFXMLLoader().getController();
		stage.setOnCloseRequest( (stageHandle) -> {
			receiverController.closeAll();
			stage.close();
		});
	}
	
	
	
	
	
	private void initMenu() {
		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("Option");
		MenuItem itemPreferences = new MenuItem("Preferences");
		MenuItem itemExit = new MenuItem("Exit");
		SeparatorMenuItem separator = new SeparatorMenuItem();
		
		menu.getItems().add(itemPreferences);
		menu.getItems().add(separator);
		menu.getItems().add(itemExit);
		
		menuBar.getMenus().add(menu);
		
		homeBorderPane.setTop(menuBar);
	}
	
	
//	public void setQueueList(ArrayList<String> queueList) {
//		this.queueList.clear();
//		this.queueList.addAll(queueList);
//	}
	
	
}

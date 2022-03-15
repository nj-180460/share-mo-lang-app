package org.sharemolangapp.smlapp.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.sharemolangapp.smlapp.StageInitializer.RootManager;
import org.sharemolangapp.smlapp.receiver.ReceiverController;
import org.sharemolangapp.smlapp.sender.SenderController;
import org.sharemolangapp.smlapp.util.ConfigConstant;
import org.springframework.stereotype.Component;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
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
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initMenu();
	}
	
	
	
	@FXML
	private void handleHomeSendButton(ActionEvent actionEvent) throws IOException {
		
		Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
		Scene scene = stage.getScene();
		Parent fxmlParent = rootManager.loadFXML(rootManager.getRegisteredResources().get(RootManager.FXML_SENDER));
		scene.setRoot(fxmlParent);
		
		SenderController senderController = rootManager.getFXMLLoader().getController();
		stage.setOnCloseRequest( (stageHandle) -> {
			senderController.closeAll();
			stage.close();
			Platform.exit();
		});
		
		boolean isConnected = senderController.connectToServerDialog();
		
		if(!isConnected) {
			senderController.returnHome();
		}
	}
	
	
	
	@FXML
	private void handleHomeReceiveButton(ActionEvent actionEvent) throws IOException {
		
		Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
		Scene scene = stage.getScene();
		
		scene.setRoot(rootManager.loadFXML(rootManager.getRegisteredResources().get(RootManager.FXML_RECEIVER)));
		
		ReceiverController receiverController = rootManager.getFXMLLoader().getController();
		stage.setOnCloseRequest( (stageHandle) -> {
			receiverController.closeAll();
			stage.close();
			Platform.exit();
		});
	}
	
	
	
	
	
	private void initMenu() {
		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("Option");
		MenuItem itemPreferences = new MenuItem(ConfigConstant.SettingsNode.PREFERENCES.getNodeText());
		MenuItem itemExit = new MenuItem(ConfigConstant.SettingsNode.EXIT.getNodeText());
		SeparatorMenuItem separator = new SeparatorMenuItem();
		
		menuPreferences(itemPreferences);
		menuExit(itemExit);
		
		menu.getItems().add(itemPreferences);
		menu.getItems().add(separator);
		menu.getItems().add(itemExit);
		
		menuBar.getMenus().add(menu);
		
		homeBorderPane.setTop(menuBar);
	}
	
	
	
	
	private void menuPreferences(MenuItem menuPref) {
		menuPref.setOnAction( (event) -> {
			
		});
	}
	
	
	private void menuExit(MenuItem menuExit) {
		menuExit.setOnAction( (event) -> {
			Platform.exit();
		});
	}
	
}

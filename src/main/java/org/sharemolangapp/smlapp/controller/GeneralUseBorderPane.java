package org.sharemolangapp.smlapp.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;



public class GeneralUseBorderPane implements Initializable{
	
	@FXML BorderPane generalUseBorderPane;
	private Stage stage;
	private boolean isSucceed;

	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
	}
	
	
	public BorderPane getGeneralUseBorderPane() {
		return generalUseBorderPane;
	}
	
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	public void closeStage() {
		stage.close();
	}
	
	
	
	public boolean isSucceed() {
		return isSucceed;
	}
	
	public void setSucceed(boolean isSucceed) {
		this.isSucceed = isSucceed;
	}
	
}

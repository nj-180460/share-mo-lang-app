package org.sharemolangapp.smlapp.receiver;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;



public class ManageConnection implements Initializable{

	@FXML private Button disconnectClientButton;
	@FXML private Label labelServerName;
	@FXML private Label labelHostAddress;
	private ReceiverController receiverController;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
	}
	
	
	@FXML
	private void handleClientDisconnect(ActionEvent actionEvent) {
		labelServerName.setText("Disconnected");
		labelHostAddress.setText("Disconnected");
	}
	
	
	void setParentController(ReceiverController receiverController) {
		this.receiverController = receiverController;
	}
	
	
	void closeStage(ActionEvent event) {
        Node  source = (Node)  event.getSource(); 
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
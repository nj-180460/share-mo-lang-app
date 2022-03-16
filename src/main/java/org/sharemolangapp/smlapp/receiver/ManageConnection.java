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
	@FXML private Label labelSenderName;
	@FXML private Label labelHostAddress;
	
	private ReceiverService receiverService;
	private ReceiverController receiverController;
	
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
	}
	
	
	@FXML
	private void handleClientDisconnect(ActionEvent actionEvent) {
		receiverService.closeClientConnection();
		labelSenderName.setText("");
		labelHostAddress.setText("");
		receiverController.setConnectedToText("");
	}
	
	
	void setReceiverService(ReceiverService receiverService) {
		this.receiverService = receiverService;
		
		String host = String.valueOf(receiverService.getClientProperties().get("host"));
		String clientName = String.valueOf(receiverService.getClientProperties().get("clientName"));
		
		if(host != null && !host.equals("null")) {
			
			labelSenderName.setText(host);
			labelHostAddress.setText(host);
			if(clientName != null && !clientName.equals("null")) {
				labelSenderName.setText(clientName);
			}
			
		} else {
			labelSenderName.setText("");
			labelHostAddress.setText("");
		}
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

package org.sharemolangapp.smlapp.sender;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;



public class ConnectionProperties {
	
	private SenderController senderController;
	private SenderService senderService;
	
	@FXML Label labelServerName;
	@FXML Label labelHostAddress;
	@FXML Label labelServerStatus;
	
	
	void setSenderController(SenderController senderController) {
		this.senderController = senderController;
	}
	
	
	void setSenderService(SenderService senderService) {
		this.senderService = senderService;
		
		String host = String.valueOf(this.senderService.getServerProperties().get("host"));
		String port = String.valueOf(this.senderService.getServerProperties().get("port"));
		
		labelServerName.setText("Some server");
		
		if((host == null || host.isBlank()) && (port == null || port.isBlank())) {
			
			labelHostAddress.setText("No host address. Complete host address");
			
		} else {
			
			labelHostAddress.setText(host + ":" + port);
			labelServerStatus.setText("Connected and active");
			
		}
	}
	
	
	
	
	
	@FXML
	private void handleDisconnect(ActionEvent actionEvent) {
		
		senderService.closeAllConnection();
		
		labelServerName.setText("Disconnected");
		labelHostAddress.setText("Disconnected");
		labelServerStatus.setText("Not connected");
	}
	
	
	@FXML
	private void handleReconnect(ActionEvent actionEvent) {
		String host = String.valueOf(senderService.getServerProperties().get("host"));
		String port = String.valueOf(senderService.getServerProperties().get("port"));
		
		if((host == null || host.isBlank()) && (port == null || port.isBlank())) {
			
			labelHostAddress.setText("No host address. Complete host address");
			
		} else {
			
			if(senderService.isConnected()) {
			
				Alert alertOn = new Alert(AlertType.NONE);
				alertOn.setHeaderText("You are still connected to a server.");
	        	alertOn.setContentText("You are not connected to a server");
	        	alertOn.setAlertType(AlertType.ERROR);
	    		alertOn.show();
			
			} else {
				
				if(senderController.connect()) {
					labelServerName.setText("Some server");
					labelHostAddress.setText(host + ":" + port);
					labelServerStatus.setText("Connected and active");
				} else {
					labelServerStatus.setText("Not connected");
				}
				
			}
			
		}
		
	}
	
	
	@FXML
	private void handleNewConnection(ActionEvent actionEvent) {
		senderController.connectToServerDialog();
	}
	
	
	
	void closeStage(ActionEvent event) {
        Node  source = (Node)  event.getSource(); 
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }
	
}

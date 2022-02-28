package org.sharemolangapp.smlapp.controller;

import java.io.IOException;
import java.util.ArrayList;

import org.sharemolangapp.smlapp.StageInitializer.RootManager;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;


@Component
public class HomeController {

	private final RootManager rootManager = RootManager.getRootManager();
	private ArrayList<String> queueList = new ArrayList<>();
	
	@FXML
	private void handleHomeSendButton() throws IOException {
		@SuppressWarnings("unchecked")
		RootManager<SenderController> rootManager = (RootManager<SenderController>) RootManager.getRootManager();
		rootManager.setRoot(RootManager.FXML_SENDER);
		SenderController sender = rootManager.getController();
		sender.setQueueList(queueList);
	}
	
	
	@FXML
	private void handleHomeReceiveButton() throws IOException {
		rootManager.setRoot(RootManager.FXML_RECEIVER);
	}
	
	
	public void setQueueList(ArrayList<String> queueList) {
		this.queueList.clear();
		this.queueList.addAll(queueList);
	}
}

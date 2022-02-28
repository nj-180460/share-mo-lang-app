package org.sharemolangapp.smlapp.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sharemolangapp.smlapp.StageInitializer.RootManager;
import org.sharemolangapp.smlapp.layer.SendableProcess;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;



public class SenderController implements Initializable {
	
	@FXML private Button uploadFilesButton;
	@FXML private Button sendFilesButton;
	@FXML private Button backHomeButton;
	@FXML private ListView<String> listviewQueue;
	@FXML private ListView<String> listviewSent;
	
	private ObservableList<String> observableListQueue;
	private ObservableList<String> observableListSent;
	
	private static int counter = 3;
	
	private ArrayList<String> initialValue = new ArrayList<>();//List.of("0", "1", "2"));
	private ArrayList<String> selectedQueueList = new ArrayList<>();
	
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		// for queue list
		observableListQueue = listviewQueue.getItems();
		observableListQueue.addAll(initialValue);
		handleListviewQueue(listviewQueue);
		
		// for sent list
		observableListSent = listviewSent.getItems();
	}
	
	
	@FXML
	private void handleUploadFiles() {
		
		String someFile = String.valueOf(counter++);
		initialValue.add(0,someFile);
		observableListQueue.add(0, someFile);
	}
	
	
	@FXML
	private void handleSendFiles() {
		
		SendableProcess sendProcess = new SendableProcess() {

			@Override
			public void process() {
				ArrayList<String> toRemove = new ArrayList<>();
				selectedQueueList.forEach( el -> {
					toRemove.add(el);
					Platform.runLater( () -> {
						observableListQueue.remove(el);
						observableListSent.add(0,el);
						int ind = observableListQueue.indexOf(el);
						MultipleSelectionModel<String> multiSelectionModel = listviewQueue.getSelectionModel();
						multiSelectionModel.clearSelection(ind);
					});
				});
				initialValue.removeAll(toRemove);
				selectedQueueList.clear();
			}
			
		};
		
		ExecutorService exeService = Executors.newSingleThreadExecutor();
		exeService.execute(new ListViewTask(sendProcess));
		exeService.shutdown();
	}

	
	@FXML
	private void handleBackHome() throws IOException {
		@SuppressWarnings("unchecked")
		RootManager<HomeController> rootManager = (RootManager<HomeController>) RootManager.getRootManager();
		rootManager.setRoot(RootManager.FXML_HOME);
		HomeController homeController = rootManager.getController();
		homeController.setQueueList(initialValue);
	}
	
	
	
	private void handleListviewQueue(ListView<String> listviewQueue) {
		MultipleSelectionModel<String> multiSelectionModel = listviewQueue.getSelectionModel();
		multiSelectionModel.setSelectionMode(SelectionMode.MULTIPLE);
		ReadOnlyObjectProperty<String> readOnlyProp = multiSelectionModel.selectedItemProperty();
		readOnlyProp.addListener( new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				selectedQueueList.clear();
				selectedQueueList.addAll(multiSelectionModel.getSelectedItems());
			}
			
		});
	}
	
	
	public ArrayList<String> getQueueList(){
		return initialValue;
	}
	
	public void setQueueList(ArrayList<String> queueList) {
		initialValue.addAll(queueList);
		observableListQueue.addAll(initialValue);
	}
	
	
	private class ListViewTask extends Task<Void>{

		private final SendableProcess sendProcess;
		
		private ListViewTask(SendableProcess sendProcess) {
			this.sendProcess = sendProcess;
		}
		
		@Override
		protected Void call() throws Exception {
			sendProcess.process();
			return null;
		}
		
	}
}

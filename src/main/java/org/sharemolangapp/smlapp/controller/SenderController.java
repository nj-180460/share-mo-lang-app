package org.sharemolangapp.smlapp.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sharemolangapp.smlapp.StageInitializer.RootManager;
import org.sharemolangapp.smlapp.controller.ReceiverController.ProgressIndicatorBar;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;



public class SenderController implements Initializable {
	
	private static final int DEFAULT_BUFFER_SIZE = 8192;
	private static boolean isWindows = true;
	static {
		String os = System.getProperty("os.name").toUpperCase();
		Pattern pattern = Pattern.compile("WINDOWS");
		Matcher matcher = pattern.matcher(os);
		
		if(matcher.find()) {
			isWindows = true;
		} else {
			isWindows = false;
		}
	}
	private static int DEFAULT_N_THREAD = 5;
	private static boolean TRANSFER_MODE = false;
	
	private final RootManager rootManager = RootManager.getRootManager();
	private final FileChooser fileChooser = new FileChooser();
    private final LinkedHashMap<File, WorkMonitor> workDoneMap  = new LinkedHashMap<>();
	
	private final ObservableList<String> observableListQueue = FXCollections.observableArrayList();
	private final ObservableList<String> observableListSent = FXCollections.observableArrayList();
	
	private final LinkedHashMap<String, File> selectedFilesMap = new LinkedHashMap<>(); 
	private ObservableList<String> selectedQueueList = FXCollections.observableArrayList();
	
	private final Label label = new Label("Sending...");
	
	@FXML private Button uploadFilesButton;
	@FXML private Button sendFilesButton;
	@FXML private Button backHomeButton;
	@FXML private ListView<String> listviewQueue;
	@FXML private ListView<String> listviewSent;
	@FXML private BorderPane dragOverComponent;
	@FXML private VBox bottomPane;
	
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		// for queue list
		observableListQueue.addAll(selectedFilesMap.keySet());
		listviewQueue.setItems(observableListQueue);
		handleListviewQueue(listviewQueue);
		
		// for sent list
		listviewSent.setItems(observableListSent);
		
		// file chooser
		fileChooser.setTitle("pag pick nala files");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		
	}
	
	
	private void handleListviewQueue(ListView<String> listviewQueue) {
		MultipleSelectionModel<String> multiSelectionModel = listviewQueue.getSelectionModel();
		multiSelectionModel.setSelectionMode(SelectionMode.MULTIPLE);
//		ReadOnlyObjectProperty<String> readOnlyProp = multiSelectionModel.selectedItemProperty();
//		readOnlyProp.addListener( new ChangeListener<String>() {
//
//			@Override
//			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
////				selectedQueueList.clear();
//				selectedQueueList = FXCollections.observableArrayList(multiSelectionModel.getSelectedItems());
////				selectedQueueList.addAll(multiSelectionModel.getSelectedItems());
//			}
//			
//		});
	}
	
	
	
	@FXML
	private void handleUploadFiles(ActionEvent actionEvent) {
		
		// FILE CHOOSTER
		List<File> selectedFileList = fileChooser.showOpenMultipleDialog(((Node)actionEvent.getSource()).getScene().getWindow());
		
		if(selectedFileList != null && !selectedFileList.isEmpty()) {
			
			selectedFileList.forEach( file -> {
				String absolutePath = file.getAbsolutePath(); 
				WorkMonitor workDone = new WorkMonitor(file.length());
				if(!observableListQueue.contains(absolutePath)) {
					workDoneMap.put(file, workDone);
					selectedFilesMap.put(absolutePath, file); 
					observableListQueue.add(0, absolutePath); // at index 0 para una siya sa listahan
				}
			});
		}
	}
	
	public void openFileExplorerLocation(File file) {
		
		if(isWindows) {
			try {
				Runtime.getRuntime().exec("explorer /select, "+file.getAbsolutePath()); // for windows os
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	@FXML
	private void handleSendFiles(ActionEvent actionEvent) {
		
		if(!TRANSFER_MODE) {
			
			ListViewService listViewService = new ListViewService(SenderController.this);
			listViewService.start();

//			updateListView();
		
		}
	}

	
	private void updateListView() {
		
		ObservableList<String> toRemove = FXCollections.observableArrayList();
		MultipleSelectionModel<String> multiSelectionModel = listviewQueue.getSelectionModel();
		
		selectedQueueList = FXCollections.observableArrayList(multiSelectionModel.getSelectedItems());
		dragOverComponent.setDisable(true);
		uploadFilesButton.setDisable(true);
		backHomeButton.setDisable(true);
		Platform.runLater( () -> {
			addProgressBarComponent();
		});
		
		selectedQueueList.forEach( absolutePath -> {
			
			TRANSFER_MODE = true;
			
			toRemove.add(absolutePath);
			
			if(selectedFilesMap.containsKey(absolutePath)) {
				
				// DO SOMETHING -- SEND FILE HERE
				File file = selectedFilesMap.get(absolutePath);
				Platform.runLater( () -> label.setText("File: ("+toMB(file.length())+") :::: "+absolutePath) );
//				WorkMonitor workMonitor = workDoneMap.get(file);
//				workMonitor.monitorWorker( () -> {
//					Platform.runLater( () -> label.setText("Progress: "+toMB(workMonitor.getWorkDone())+" of "+toMB(workMonitor.getTotalWork())+" === "+absolutePath) );
//				});
				
				try {
					sendFileTo(file);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					workDoneMap.remove(file);
					selectedFilesMap.remove(absolutePath);
					Platform.runLater( () -> {
						observableListQueue.remove(absolutePath);
						observableListSent.add(0, absolutePath);
					});
				}
			}
		});
		
		toRemove.forEach(selectedFilesMap::remove);
		Platform.runLater( () -> {
			selectedQueueList.clear();
			observableListQueue.removeAll(selectedQueueList);
			multiSelectionModel.clearSelection();
			
			dragOverComponent.setDisable(false);
			uploadFilesButton.setDisable(false);
			backHomeButton.setDisable(false);
			
			removeProgressBarComponent();
		});
		TRANSFER_MODE = false;
		
	}
	
	
	
	private void sendFileTo(File file) throws IOException {
		String absolutePath = file.getAbsolutePath();
		String fileName = file.getName();
		StringBuilder receivingFolder = new StringBuilder();
		receivingFolder.append(System.getProperty("user.home"));
		receivingFolder.append(File.separator);
		receivingFolder.append("Desktop");
		receivingFolder.append(File.separator);
		receivingFolder.append("received");
		receivingFolder.append(File.separator);
		receivingFolder.append(fileName);
		
		File toOutputfile = new File(receivingFolder.toString());
		WorkMonitor workMonitor = workDoneMap.get(file);
		
		
		try(FileOutputStream output = new FileOutputStream(toOutputfile);
				FileInputStream input = new FileInputStream(file)){
			long transferred = 0;
//			long fileSize = file.length();
	        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
	        int read;
	        while ((read = input.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
	        	output.write(buffer, 0, read);
	            transferred += read;
	            workMonitor.setWorkDone(transferred);
//				Platform.runLater( () -> label.setText("Progress: "+toMB(workMonitor.getWorkDone())+" of "+toMB(workMonitor.getTotalWork())));
	        }
	        
		} catch(IOException ioex) {
			throw new IOException(ioex);
		}
	}
	
	private String toMB(double value) {
		double computedValue =  (value/1024)/1024;
		return String.format("%,.2f", computedValue)+"MB";
	}
	
	private String toKB(double value) {
		double computedValue =  (value/1024);
		return String.format("%,.2f", computedValue)+"KB";
	}
	
	
	
	@FXML
	private void handleBackHome(ActionEvent actionEvent) throws IOException {
		
		Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
		Scene scene = stage.getScene();
		Parent fxmlParent = rootManager.loadFXML(rootManager.getRegisteredResources().get(RootManager.FXML_HOME));
//		FXMLLoader fxmlLoader = rootManager.getFXMLLoader();
//		HomeController init = fxmlLoader.getController();
		scene.setRoot(fxmlParent);
	}
	
	
	
	
	@FXML
	private void handleDragOver(DragEvent dragEvent) {
		
		Dragboard dragBoard = dragEvent.getDragboard();
		
		if(dragBoard.hasFiles()) {
			dragEvent.acceptTransferModes(TransferMode.COPY);
			ClipboardContent content = new ClipboardContent();
			content.putFiles(dragBoard.getFiles());
			dragEvent.consume();
		}
	}
	
	
	@FXML
	private void handleDragDropped(DragEvent dragEvent) {
		
		ArrayList<File> files = new ArrayList<>();
		Dragboard dragBoard = dragEvent.getDragboard();
		LinkedHashMap<String, File> selectedFiles = new LinkedHashMap<String, File>();
		
		if(dragBoard.hasFiles()) {
			files.addAll(dragBoard.getFiles());
			
			files.forEach( file -> {
				String absolutePath = file.getAbsolutePath();
				WorkMonitor workDone = new WorkMonitor(file.length());
				workDoneMap.put(file, workDone);
				selectedFilesMap.put(absolutePath, file);
				selectedFiles.put(absolutePath, file);
			});
			
			observableListQueue.addAll(selectedFiles.keySet().stream().filter(fileName -> !observableListQueue.contains(fileName) ).toList());
		}
		dragEvent.setDropCompleted(true);
		dragEvent.consume();
	}
	
	
	
	private void addProgressBarComponent() {
		bottomPane.getChildren().add(0,label);
	}
	
	private void removeProgressBarComponent() {
		bottomPane.getChildren().remove(label);
	}
	
	
	
	private class ListViewService extends Service<ObservableList<String>>{

		private final SenderController sendProcess;
		
		private ListViewService(SenderController sendProcess) {
			this.sendProcess = sendProcess;
		}
		
		@Override
		protected Task<ObservableList<String>> createTask() {
			return new ListViewTask(sendProcess);
		}
		
	}
	
	
	private class ListViewTask extends Task<ObservableList<String>>{

		private final SenderController sendProcess;
		
		private ListViewTask(SenderController sendProcess) {
			this.sendProcess = sendProcess;
		}
		
		@Override
		protected ObservableList<String> call() throws Exception {
			sendProcess.updateListView();
			return null;
		}
		
	}
	
	
	
	
	
	
//	/***
//	 * 
//	 * Custom progress bar
//	 *
//	 */
//	class ProgressIndicatorBar extends StackPane {
//		
//		final private static int DEFAULT_LABEL_PADDING = 1;
//		
//		final private WorkMonitor workDone;
//		final private double totalWork;
//
//		final private ProgressBar bar  = new ProgressBar();
//		final private Text text = new Text();
//		final private String labelFormatSpecifier;
//
//		
//		ProgressIndicatorBar(final WorkMonitor workDone, final double totalWork, final String labelFormatSpecifier) {
//			this.workDone  = workDone;
//		    this.totalWork = totalWork;
//		    this.labelFormatSpecifier = labelFormatSpecifier;
//		    
//		    bar.setMaxWidth(Double.MAX_VALUE); // allows the progress bar to expand to fill available horizontal space.
//		    bar.setStyle("-fx-accent: #0F0;");
//
//		    workDone.monitorWorker( () -> {
//		    	Platform.runLater( () -> {
//		    		syncProgress();
//		    	});
//		    });
//		    
//		    getChildren().setAll(bar, text);
//		}
//
//		// synchronizes the progress indicated with the work done.
//		private void syncProgress() {
//			if (workDone == null || totalWork == 0) {
//				text.setText("");
//				bar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
//			} else {
//				text.setText(String.format(labelFormatSpecifier, Math.ceil(workDone.getWorkDone())));
//				bar.setProgress(workDone.getWorkDone() / totalWork);
//			}
//			
//			bar.setMinHeight(text.getBoundsInLocal().getHeight() + DEFAULT_LABEL_PADDING * 2);
//			bar.setMinWidth (text.getBoundsInLocal().getWidth()  + DEFAULT_LABEL_PADDING * 2);
//		}
//	}
//	
//	
//	
//	/**
//	 * 
//	 * Custome cell on listview
//	 *
//	 */
//	private class ProgressBarListCell extends ListCell<ProgressIndicatorBar> {
//
//        public ProgressBarListCell() {
//            super();
//        }
//
//        @Override
//        protected void updateItem(ProgressIndicatorBar item, boolean empty) {
//            super.updateItem(item, empty);
//            if (item != null && !empty) {
//                setGraphic(item);
//            } else {
//                setGraphic(null);
//            }
//        }
//    }
	
	
	
	
	
	
	
	public class WorkMonitor {
		
		private final long totalWork;
		
		private long workDone;
		private long oldValue = workDone;
		
		public WorkMonitor(long totalWork) {
			this.totalWork = totalWork;
		}
		
		public long getTotalWork() {
			return totalWork;
		}
		
		public void setWorkDone(long workDone) {
			this.workDone = workDone;
		}
		
		public long getWorkDone() {
			return workDone;
		}
		
		public void monitorWorker(Workable worker) {
			ExecutorService exeService = Executors.newSingleThreadExecutor();
			exeService.execute( () -> {
				while(workDone != totalWork) {
					if(workDone != oldValue) {
						oldValue = workDone;
						worker.work();
					}
				}
				TRANSFER_MODE = false;
			});
			exeService.shutdown();
		}
	}
	
	interface Workable{
		public void work();
	}
}

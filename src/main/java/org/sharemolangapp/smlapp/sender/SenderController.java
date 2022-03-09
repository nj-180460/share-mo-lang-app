package org.sharemolangapp.smlapp.sender;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sharemolangapp.smlapp.StageInitializer.RootManager;
import org.sharemolangapp.smlapp.layer.Workable;
import org.sharemolangapp.smlapp.util.GenericUtils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;



public class SenderController implements Initializable {
	
	
	private static boolean TRANSFER_MODE = false;
	private static boolean CLOSING_STAGE = false;
	
	private final RootManager rootManager = RootManager.getRootManager();
	private final FileChooser fileChooser = new FileChooser();
    private final LinkedHashMap<File, WorkMonitor> workDoneMap  = new LinkedHashMap<>();
	
	private final ObservableList<ProgressIndicatorBar> observableListQueue = FXCollections.observableArrayList();
	private final ObservableList<String> observableListSent = FXCollections.observableArrayList();
	
	private final LinkedHashMap<ProgressIndicatorBar, File> selectedFilesMap = new LinkedHashMap<>(); 
	private ObservableList<ProgressIndicatorBar> selectedQueueList = FXCollections.observableArrayList();
	
	@FXML private Button uploadFilesButton;
	@FXML private Button sendFilesButton;
	@FXML private Button connectionPropertiesButton;
	@FXML private Button backHomeButton;
	@FXML private ListView<ProgressIndicatorBar> listviewQueue;
	@FXML private ListView<String> listviewSent;
	@FXML private BorderPane dragOverComponent;
	@FXML private VBox bottomPane;
	
	private SenderService senderService;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		CLOSING_STAGE = false;
		
		senderService = new SenderService();
		
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
	
	
	
	private void handleListviewQueue(ListView<ProgressIndicatorBar> listviewQueue) {
		listviewQueue.setCellFactory(new Callback<ListView<ProgressIndicatorBar>, ListCell<ProgressIndicatorBar>>() {
            @Override
            public ListCell<ProgressIndicatorBar> call(ListView<ProgressIndicatorBar> listView) {
                return new ProgressBarListCell();
            }
        });
		
		MultipleSelectionModel<ProgressIndicatorBar> multiSelectionModel = listviewQueue.getSelectionModel();
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
		LinkedHashMap<ProgressIndicatorBar, File> selectedFiles = new LinkedHashMap<>();
		
		if(selectedFileList != null && !selectedFileList.isEmpty()) {
			
			selectedFileList.forEach( file -> {
				String absolutePath = file.getAbsolutePath(); 
				WorkMonitor workDone = new WorkMonitor(file.length());
//				observableListQueue.forEach( progressBar -> {
//					if(progressBar.fileName.equals(absolutePath)) {
//						workDoneMap.put(file, workDone);
//						ProgressIndicatorBar withProgressBar = new ProgressIndicatorBar(workDone, file.length(), absolutePath);
//						selectedFilesMap.put(withProgressBar, file); 
//						observableListQueue.add(0, withProgressBar); // at index 0 para una siya sa listahan
//					}
//				});
				
//				Optional<ProgressIndicatorBar> foundFile = observableListQueue.stream()
//						.filter( pb -> pb.fileName.equals(absolutePath))
//						.findFirst();
//				
//				if(foundFile == null || foundFile.isEmpty()) {
//					workDoneMap.put(file, workDone);
//					ProgressIndicatorBar withProgressBar = new ProgressIndicatorBar(workDone, file.length(), absolutePath);
//					selectedFilesMap.put(withProgressBar, file); 
//					observableListQueue.add(0, foundFile.get());
//				}
				
				
				Optional<ProgressIndicatorBar> foundFile = observableListQueue.stream()
						.filter( pb -> pb.fileName.equals(absolutePath))
						.findFirst();
				
				if(foundFile == null || foundFile.isEmpty()) {
					ProgressIndicatorBar withProgressBar = new ProgressIndicatorBar(workDone, file.length(), absolutePath);
					workDoneMap.put(file, workDone);
					selectedFilesMap.put(withProgressBar, file);
					selectedFiles.put(withProgressBar, file);
				}
				
//				if(!observableListQueue.contains(absolutePath)) {
//					workDoneMap.put(file, workDone);
//					ProgressIndicatorBar withProgressBar = new ProgressIndicatorBar(workDone, file.length(), absolutePath);
//					selectedFilesMap.put(withProgressBar, file); 
//					observableListQueue.add(0, withProgressBar); // at index 0 para una siya sa listahan
////					selectedFilesMap.put(absolutePath, file); 
////					observableListQueue.add(0, absolutePath); // at index 0 para una siya sa listahan
//				}
			});
			
			observableListQueue.addAll(selectedFiles.keySet().stream().toList());
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

	
	private void sendNow() throws IOException {
		
		ObservableList<ProgressIndicatorBar> toRemove = FXCollections.observableArrayList();
		MultipleSelectionModel<ProgressIndicatorBar> multiSelectionModel = listviewQueue.getSelectionModel();
		
		selectedQueueList = FXCollections.observableArrayList(multiSelectionModel.getSelectedItems());
		dragOverComponent.setDisable(true);
		listviewQueue.setDisable(true);
		uploadFilesButton.setDisable(true);
		connectionPropertiesButton.setDisable(true);
		backHomeButton.setDisable(true);
		
		
		senderService.setFilesLeft(selectedQueueList.size());
		
		selectedQueueList.forEach( absolutePath -> {
			
			if(CLOSING_STAGE) {
				return;
			}
			
			TRANSFER_MODE = true;
			
			toRemove.add(absolutePath);
			
			if(selectedFilesMap.containsKey(absolutePath)) {
				
				// DO SOMETHING -- SEND FILE HERE
				File file = selectedFilesMap.get(absolutePath);
				absolutePath.startMonitoring();
//				Platform.runLater( () -> label.setText("File: ("+toMB(file.length())+") :::: "+absolutePath) );
				WorkMonitor workMonitor = workDoneMap.get(file);
//				workMonitor.monitorWorker( () -> {
//					Platform.runLater( () -> label.setText("Progress: "+toMB(workMonitor.getWorkDone())+" of "+toMB(workMonitor.getTotalWork())+" === "+absolutePath) );
//				});
				
				try {
//					sendFileTo(file);
					senderService.sendFileTo(file, workMonitor);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					workDoneMap.remove(file);
					selectedFilesMap.remove(absolutePath);
					Platform.runLater( () -> {
						observableListQueue.remove(absolutePath);
//						observableListSent.add(0, absolutePath);
						observableListSent.add(0, file.getAbsolutePath());
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
			listviewQueue.setDisable(false);
			uploadFilesButton.setDisable(false);
			connectionPropertiesButton.setDisable(false);
			backHomeButton.setDisable(false);
			
		});
		
		TRANSFER_MODE = false;
		
	}
	
	
	
//	private void sendFileTo(File file) throws IOException {
//		String fileName = file.getName();
//		StringBuilder receivingFolder = new StringBuilder();
//		receivingFolder.append(System.getProperty("user.home"));
//		receivingFolder.append(File.separator);
//		receivingFolder.append("Desktop");
//		receivingFolder.append(File.separator);
//		receivingFolder.append("received");
//		receivingFolder.append(File.separator);
//		receivingFolder.append(fileName);
//		
//		File toOutputfile = new File(receivingFolder.toString());
//		WorkMonitor workMonitor = workDoneMap.get(file);
//		
//		
//		try(FileOutputStream output = new FileOutputStream(toOutputfile);
//				FileInputStream input = new FileInputStream(file)){
//			long transferred = 0;
//	        byte[] buffer = new byte[ConfigConstant.DEFAULT_BUFFER_SIZE];
//	        int read;
//	        while ((read = input.read(buffer, 0, ConfigConstant.DEFAULT_BUFFER_SIZE)) >= 0) {
//	        	output.write(buffer, 0, read);
//	            transferred += read;
//	            workMonitor.setWorkDone(transferred);
//	        }
//	        
//		} catch(IOException ioex) {
//			throw new IOException(ioex);
//		}
//	}
//	
//	private String toMB(double value) {
//		double computedValue =  (value/1024)/1024;
//		return String.format("%,.2f", computedValue)+"MB";
//	}
	
	
	
	
	@FXML
	private void handleConnectionProperties(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(rootManager.getRegisteredResources().get(RootManager.FXML_CONNECTION_PROPERTIES_DIALOG).getURL());
        Parent fxmlParent = fxmlLoader.load();
        
        ConnectionProperties dialogController = fxmlLoader.getController();
        dialogController.setSenderController(this);
        dialogController.setSenderService(senderService);
        
        Scene scene = new Scene(fxmlParent, 400, 200);
        Stage stage = new Stage();
        
        stage.setOnCloseRequest((eventHandle) -> stage.close());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
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
		LinkedHashMap<ProgressIndicatorBar, File> selectedFiles = new LinkedHashMap<>();
		
		if(dragBoard.hasFiles()) {
			files.addAll(dragBoard.getFiles());
			
			files.forEach( file -> {
				String absolutePath = file.getAbsolutePath();
				WorkMonitor workDone = new WorkMonitor(file.length());
//				workDoneMap.put(file, workDone);
				
				ProgressIndicatorBar withProgressBar = new ProgressIndicatorBar(workDone, file.length(), absolutePath);
//				selectedFilesMap.put(withProgressBar, file);
//				selectedFiles.put(withProgressBar, file);
				Optional<ProgressIndicatorBar> foundFile = observableListQueue.stream()
						.filter( pb -> pb.fileName.equals(absolutePath))
						.findFirst();
				
				if(foundFile == null || foundFile.isEmpty()) {
					workDoneMap.put(file, workDone);
					selectedFilesMap.put(withProgressBar, file);
					selectedFiles.put(withProgressBar, file);
				}
				
//				selectedFilesMap.put(absolutePath, file);
//				selectedFiles.put(absolutePath, file);
			});
			
//			observableListQueue.addAll(selectedFiles.keySet().stream().filter(fileName -> !observableListQueue.contains(fileName) ).toList());
			observableListQueue.addAll(selectedFiles.keySet().stream().toList());
		}
		
		dragEvent.setDropCompleted(true);
		dragEvent.consume();
	}
	
	
	
	
	
	
	
	public void connectToServerDialog() {
		
		Dialog<Properties> dialog = new Dialog<>();
		dialog.setTitle("Connect to ");
		dialog.setHeaderText("Enter host and port the server then Connect, mate.");
		dialog.setResizable(false);
		dialog.setWidth(300);
		 
		TextField tfHost = new TextField();
		TextField tfPort = new TextField();
		Button testConnectionButton = new Button();
		Alert alertOnTest = new Alert(AlertType.NONE);
		
		testConnectionButton.setText("Test Connection");
		testConnectionButton.setOnAction( (eventHandler) -> {
			
			boolean isConnected = false;
			Properties serverProp = new Properties();
			serverProp.setProperty("host", tfHost.getText());
        	serverProp.setProperty("port", tfPort.getText());
        	senderService.setServerProperties(serverProp);
			
			try {
				isConnected = senderService.testConnection();
			} catch (NumberFormatException e) {
				e.printStackTrace();
				alertOnTest.setHeaderText("Invalid inputs");
				alertOnTest.setContentText("You entered invalid inputs. Provide valid inputs.");
				alertOnTest.setAlertType(AlertType.ERROR);
				alertOnTest.show();
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
				alertOnTest.setHeaderText("Unknown Server");
				alertOnTest.setContentText("Unable to connect to server. Make sure you provide the right address of the server.");
				alertOnTest.setAlertType(AlertType.ERROR);
				alertOnTest.show();
				
			} catch (IOException e) {
				e.printStackTrace();
				alertOnTest.setHeaderText("Connection unavailable");
				alertOnTest.setContentText("Unable to start connection. Make sure you are connected to a network.");
				alertOnTest.setAlertType(AlertType.ERROR);
				alertOnTest.show();
			}
			
			if(isConnected) {
				alertOnTest.setHeaderText("Connection success");
				alertOnTest.setContentText("Successfully connected!");
				alertOnTest.setAlertType(AlertType.INFORMATION);
				alertOnTest.show();
			} else {
				alertOnTest.setHeaderText("Connection refused");
				alertOnTest.setContentText("You might connecting to an invalid server address. Try again.");
				alertOnTest.setAlertType(AlertType.ERROR);
				alertOnTest.show();
			}
			
		});
		
		tfHost.setPromptText("Host");
		tfHost.setAlignment(Pos.CENTER);
		
		tfPort.setPromptText("Port");
		tfPort.setAlignment(Pos.CENTER);
		         
		GridPane grid = new GridPane();
		grid.setPadding(new Insets(5,5,5,5));
		grid.setHgap(20);
		grid.setVgap(20);
		grid.add(tfHost, 1, 1);
		grid.add(tfPort, 1, 2);
		grid.add(testConnectionButton, 1, 3);
		dialog.getDialogPane().setContent(grid);
		         
		ButtonType buttonTypeOk = new ButtonType("Connect", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
		 
		dialog.setResultConverter(new Callback<ButtonType, Properties>() {
		    @Override
		    public Properties call(ButtonType b) {
		 
		        if (b == buttonTypeOk) {
		 
		        	Properties serverProp = new Properties();
		        	String host = tfHost.getText();
		        	String port = tfPort.getText();
		        	
		        	serverProp.setProperty("host", host);
		        	serverProp.setProperty("port", port);
		        	
		        	if((host == null || host.isBlank()) && (port == null || port.isBlank())) {
		    			return null;
		    		}
		        	
		            return serverProp;
		        }
		 
		        return null;
		    }
		});
		         
		Optional<Properties> result = dialog.showAndWait();
		         
		if (result.isPresent()) {
			
			senderService.setServerProperties(result.get());
        	
			connect();
			
		} else {
			
			Alert alertOnConnect = new Alert(AlertType.NONE);
			alertOnConnect.setHeaderText("Incomplete host address");
			alertOnConnect.setContentText("IP address and port must be present.");
			alertOnConnect.setAlertType(AlertType.ERROR);
			alertOnConnect.show();
			
		}
	}
	
	
	
	boolean connect() {
		
		Alert alertOnConnect = new Alert(AlertType.NONE);
		boolean isConnected = false;
		
    	try {
			isConnected = senderService.connect();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			alertOnConnect.setHeaderText("Invalid inputs");
			alertOnConnect.setContentText("You entered invalid inputs. Provide valid inputs.");
			alertOnConnect.setAlertType(AlertType.ERROR);
			alertOnConnect.show();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			alertOnConnect.setHeaderText("Unknown Server");
			alertOnConnect.setContentText("Unable to connect to server. Make sure you provide the right address of the server.");
			alertOnConnect.setAlertType(AlertType.ERROR);
			alertOnConnect.show();
			
		} catch (IOException e) {
			e.printStackTrace();
			alertOnConnect.setHeaderText("Connection unavailable");
			alertOnConnect.setContentText("Unable to start connection. Make sure you are connected to a network.");
			alertOnConnect.setAlertType(AlertType.ERROR);
			alertOnConnect.show();
		}
		
    	
		if(isConnected) {
			
			alertOnConnect.setHeaderText("Connection success");
			alertOnConnect.setContentText("Successfully connected!");
			alertOnConnect.setAlertType(AlertType.INFORMATION);
			alertOnConnect.show();
			
		} else {
			
			alertOnConnect.setHeaderText("Connection refused");
			alertOnConnect.setContentText("You might connecting to an invalid server address. Try again.");
			alertOnConnect.setAlertType(AlertType.ERROR);
			alertOnConnect.show();
			
		}
		
		return isConnected;
	}
	
	
	
	
	
	
	public void closeAll() {
		CLOSING_STAGE = true;
		senderService.closeAllConnection();
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
			sendProcess.sendNow();
			return null;
		}
		
	}
	
	
	
	
	
	
	/***
	 * 
	 * Custom progress bar
	 *
	 */
	class ProgressIndicatorBar extends StackPane {
		
		final private static int DEFAULT_LABEL_PADDING = 1;
		
		final private WorkMonitor workDone;
		final private double totalWork;

		final private ProgressBar bar  = new ProgressBar();
		final private Text text = new Text();
		final private String labelFormatSpecifier;
		private final String fileName;

		
		ProgressIndicatorBar(final WorkMonitor workDone, final double totalWork, final String labelFormatSpecifier) {
			this.workDone  = workDone;
		    this.totalWork = totalWork;
		    this.labelFormatSpecifier = "(%s/"+GenericUtils.toMB(totalWork)+") "+labelFormatSpecifier;
		    this.fileName = labelFormatSpecifier;
		    
		    text.setTextAlignment(TextAlignment.LEFT);
		    text.setText(String.format(this.labelFormatSpecifier, GenericUtils.toMB(Math.ceil(workDone.getWorkDone()))));
		    bar.setMaxWidth(Double.MAX_VALUE); // allows the progress bar to expand to fill available horizontal space.
		    bar.setStyle("-fx-accent: #0F0;");
		    bar.setProgress(workDone.getWorkDone() / totalWork);
		    
		    getChildren().setAll(bar, text);
		}

		// synchronizes the progress indicated with the work done.
		private void syncProgress(double progress, String computedWorkDone) {
			if (workDone == null || totalWork == 0) {
				text.setText("");
				bar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
			} else {
				text.setText(String.format(labelFormatSpecifier, computedWorkDone));
				bar.setProgress(progress);
			}
			
			bar.setMinHeight(text.getBoundsInLocal().getHeight() + DEFAULT_LABEL_PADDING * 2);
			bar.setMinWidth (text.getBoundsInLocal().getWidth()  + DEFAULT_LABEL_PADDING * 2);
		}
		
		private void startMonitoring() {
		    workDone.monitorWorker( () -> {
		    	String computedValue = GenericUtils.toMB(workDone.getWorkDone());
		    	double progress = workDone.getWorkDone() / totalWork;
		    	Platform.runLater( () -> {
		    		syncProgress(progress, computedValue);
		    	});
		    });
		}
		
	}
	
	
	
	/**
	 * 
	 * Custome cell on listview
	 *
	 */
	private class ProgressBarListCell extends ListCell<ProgressIndicatorBar> {

        public ProgressBarListCell() {
            super();
        }

        @Override
        protected void updateItem(ProgressIndicatorBar item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) {
                setGraphic(item);
            } else {
                setGraphic(null);
            }
        }
    }
	
	
	
	
	
	
	
	class WorkMonitor {
		
		private final long totalWork;
		
		private long workDone;
		private long oldValue = workDone;
		
		WorkMonitor(long totalWork) {
			this.totalWork = totalWork;
		}
		
		long getTotalWork() {
			return totalWork;
		}
		
		void setWorkDone(long workDone) {
			this.workDone = workDone;
		}
		
		long getWorkDone() {
			return workDone;
		}
		
		void monitorWorker(Workable worker) {
			ExecutorService exeService = Executors.newSingleThreadExecutor();
			exeService.execute( () -> {
				while((workDone != totalWork)) {
					
					if(CLOSING_STAGE) {
						break;
					}
					
					try {
						Thread.sleep(GenericUtils.TRANSFER_RATE_MS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
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
	
}

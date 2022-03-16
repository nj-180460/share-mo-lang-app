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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sharemolangapp.smlapp.StageInitializer.RootManager;
import org.sharemolangapp.smlapp.controller.ExceptionExpandedUI;
import org.sharemolangapp.smlapp.controller.GeneralUseBorderPane;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
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
	private SenderService senderService;
	
	private GeneralUseBorderPane generalUseBorderPane;
	@FXML private Button uploadFilesButton;
	@FXML private Button sendFilesButton;
	@FXML private Button connectionPropertiesButton;
	@FXML private Button backHomeButton;
	@FXML private ListView<ProgressIndicatorBar> listviewQueue;
	@FXML private ListView<String> listviewSent;
	@FXML private BorderPane dragOverComponent;
	@FXML private VBox bottomPane;
	
	
	
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
				
				Optional<ProgressIndicatorBar> foundFile = observableListQueue.stream()
						.filter( pb -> pb.fileName.equals(absolutePath))
						.findFirst();
				
				if(foundFile == null || foundFile.isEmpty()) {
					ProgressIndicatorBar withProgressBar = new ProgressIndicatorBar(workDone, file.length(), absolutePath);
					workDoneMap.put(file, workDone);
					selectedFilesMap.put(withProgressBar, file);
					selectedFiles.put(withProgressBar, file);
				}
				
			});
			
			observableListQueue.addAll(selectedFiles.keySet().stream().toList());
		}
		
	}
	
	
	
	@FXML
	private void handleSendFiles(ActionEvent actionEvent) {
		
		if(!TRANSFER_MODE) {
			
			if(senderService.isServerAccepted()) {
				
				ListViewService listViewService = new ListViewService(SenderController.this);
				listViewService.start();
				
			} else {
				
				Alert alertOn = new Alert(AlertType.WARNING);
				alertOn.setTitle("Confirmation");
				alertOn.setHeaderText("Server Confirmation");
				alertOn.setContentText("Not yet ready to send. Wait for server to accept your request.");
				alertOn.show();
				
			}
		
		}
	}

	
	private void sendNow() throws IOException {
		
		if(CLOSING_STAGE) {
			return;
		}
		
		ObservableList<ProgressIndicatorBar> toRemove = FXCollections.observableArrayList();
		MultipleSelectionModel<ProgressIndicatorBar> multiSelectionModel = listviewQueue.getSelectionModel();
		
		selectedQueueList = FXCollections.observableArrayList(multiSelectionModel.getSelectedItems());
		
		toggleComponents(true);
		connectionPropertiesButton.setDisable(true);
		
		senderService.setTotalQueuedFiles(selectedQueueList.size());
		
		selectedQueueList.forEach( pbarAnimation -> {
			
			if(CLOSING_STAGE) {
				TRANSFER_MODE = false;
				return;
			}
			
			TRANSFER_MODE = true;
			toRemove.add(pbarAnimation);
			
			if(selectedFilesMap.containsKey(pbarAnimation)) {
				
				// DO SOMETHING -- SEND FILE HERE
				File file = selectedFilesMap.get(pbarAnimation);
				pbarAnimation.startMonitoring();
				WorkMonitor workMonitor = workDoneMap.get(file);
				
				try {
					senderService.sendFileTo(file, workMonitor);
					
				} catch (IOException e) {
					e.printStackTrace();
					
				} finally {
					workDoneMap.remove(file);
					selectedFilesMap.remove(pbarAnimation);
					Platform.runLater( () -> {
						observableListQueue.remove(pbarAnimation);
//						observableListSent.add(0, absolutePath);
						observableListSent.add(0, file.getAbsolutePath());
					});
				}
			}
		});
		
		
		// server not down
		toRemove.forEach(selectedFilesMap::remove);
		
		Platform.runLater( () -> {
			
			selectedQueueList.clear();
			observableListQueue.removeAll(selectedQueueList);
			multiSelectionModel.clearSelection();
			
			toggleComponents(false);
			connectionPropertiesButton.setDisable(false);
			
		});
				
		TRANSFER_MODE = false;
	}
	
	
	
	
	
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
		
		senderService.closeAllConnection();
		
		Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
		Scene scene = stage.getScene();
		Parent fxmlParent = rootManager.loadFXML(rootManager.getRegisteredResources().get(RootManager.FXML_HOME));
//		FXMLLoader fxmlLoader = rootManager.getFXMLLoader();
//		HomeController init = fxmlLoader.getController();
		scene.setRoot(fxmlParent);
	}
	
	
	
	
	@FXML
	private void handleClearSelectedFiles(ActionEvent actionEvent) {
		ObservableList<ProgressIndicatorBar> toRemove = FXCollections.observableArrayList();
		MultipleSelectionModel<ProgressIndicatorBar> multiSelectionModel = listviewQueue.getSelectionModel();
		
		selectedQueueList = FXCollections.observableArrayList(multiSelectionModel.getSelectedItems());
		toggleComponents(true);
		connectionPropertiesButton.setDisable(true);
		
		selectedQueueList.forEach( absolutePath -> {
			
			toRemove.add(absolutePath);
			
			if(selectedFilesMap.containsKey(absolutePath)) {
				
				// DO SOMETHING -- SEND FILE HERE
				File file = selectedFilesMap.get(absolutePath);
				absolutePath.startMonitoring();
				
				workDoneMap.remove(file);
				selectedFilesMap.remove(absolutePath);
				Platform.runLater( () -> {
					observableListQueue.remove(absolutePath);
				});
				
			}
		});
		
		toRemove.forEach(selectedFilesMap::remove);
		
		Platform.runLater( () -> {
			
			selectedQueueList.clear();
			observableListQueue.removeAll(selectedQueueList);
			multiSelectionModel.clearSelection();
			
			toggleComponents(false);
			connectionPropertiesButton.setDisable(false);
			
		});
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
		
		Dragboard dragBoard = dragEvent.getDragboard();
		
		if(senderService.isConnected()) {
		
			ArrayList<File> files = new ArrayList<>();
			LinkedHashMap<ProgressIndicatorBar, File> selectedFiles = new LinkedHashMap<>();
			
			if(dragBoard.hasFiles()) {
				files.addAll(dragBoard.getFiles());
				
				files.forEach( file -> {
					String absolutePath = file.getAbsolutePath();
					WorkMonitor workDone = new WorkMonitor(file.length());
					
					ProgressIndicatorBar withProgressBar = new ProgressIndicatorBar(workDone, file.length(), absolutePath);
					Optional<ProgressIndicatorBar> foundFile = observableListQueue.stream()
							.filter( pb -> pb.fileName.equals(absolutePath))
							.findFirst();
					
					if(foundFile == null || foundFile.isEmpty()) {
						workDoneMap.put(file, workDone);
						selectedFilesMap.put(withProgressBar, file);
						selectedFiles.put(withProgressBar, file);
					}
					
				});
				
				observableListQueue.addAll(selectedFiles.keySet().stream().toList());
			}
			
		
		} else {
			
			Alert alertOn = new Alert(AlertType.NONE);
			alertOn.setHeaderText("To whom should you send this nonsense?");
        	alertOn.setContentText("You are not connected to a receiver yet.");
        	alertOn.setAlertType(AlertType.ERROR);
    		alertOn.show();
		}
		
		dragEvent.setDropCompleted(true);
		dragEvent.consume();
	}
	
	
	
	private void toggleComponents(boolean enable) {
		dragOverComponent.setDisable(enable);
		sendFilesButton.setDisable(enable);
		uploadFilesButton.setDisable(enable);
//		backHomeButton.setDisable(enable);
	}
	
	
	
	
	
	public boolean connectToServerDialog() {
		
		Dialog<Properties> dialog = new Dialog<>();
		dialog.setTitle("Connect to ");
		dialog.setHeaderText("Enter host and port the server then Connect, mate.");
		dialog.setResizable(false);
		dialog.setWidth(300);
		 
		TextField tfHost = new TextField();
		TextField tfPort = new TextField();
		Button testConnectionButton = new Button();
		
		Alert alertOnTest = new Alert(AlertType.NONE);
		ExceptionExpandedUI expandableUI = new ExceptionExpandedUI(alertOnTest);
		
		
		testConnectionButton.setText("Test Connection");
		testConnectionButton.setOnAction( (eventHandler) -> {
			
			String headerText = "Connection success";
			String contentText = "Successfully connected!";
			AlertType alertType = AlertType.INFORMATION;
			Properties serverProp = new Properties();
			
			serverProp.setProperty("host", tfHost.getText());
        	serverProp.setProperty("port", tfPort.getText());
        	senderService.setServerProperties(serverProp);
			
        	try {
    			
    			if(!senderService.testConnection()) {
    				
    				headerText = "Connection refused";
    				contentText = "You might connecting to an invalid server address. Try again";
    				alertType = AlertType.ERROR;
    				
    			}
    			
    			
    		} catch (NumberFormatException e) {
    			e.printStackTrace();
    			expandableUI.addExceptionStackTrace(e);
    			headerText = "Invalid inputs";
    			contentText = "You entered invalid inputs. Provide valid inputs.";
    			alertType = AlertType.ERROR;
    			
    		} catch (UnknownHostException e) {
    			e.printStackTrace();
    			expandableUI.addExceptionStackTrace(e);
    			headerText = "Unknown Server";
    			contentText = "Unable to connect to server. Make sure you provide the right address of the server.";
    			alertType = AlertType.ERROR;
    			
    		} catch (IOException e) {
    			e.printStackTrace();
    			expandableUI.addExceptionStackTrace(e);
    			headerText = "Connection unavailable";
    			contentText = "Unable to start connection. Make sure you are connected to a network.";
    			alertType = AlertType.ERROR;
    			
    		} catch (InterruptedException e) {
				e.printStackTrace(); // i think do nothing when interrupted nalang
				
			} catch (ExecutionException e) {
				e.printStackTrace();
				
			}
        	
        	alertOnTest.setHeaderText(headerText);
        	alertOnTest.setContentText(contentText);
        	alertOnTest.setAlertType(alertType);
    		alertOnTest.show();
			
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
		        	
		        	Alert alertOnConnect = new Alert(AlertType.NONE);
		        	String headerText = "Incomplete host address";
		        	String contentText = "IP address and port must be present.";
		        	
		        	serverProp.setProperty("host", host);
		        	serverProp.setProperty("port", port);
		        	
		        	if((host == null || host.isBlank()) && (port == null || port.isBlank())) {
		    			
						alertOnConnect.setHeaderText(headerText);
						alertOnConnect.setContentText(contentText);
						alertOnConnect.setAlertType(AlertType.ERROR);
						alertOnConnect.show();
		        		
		    		} else {
		    			
		    			return serverProp;
		    		}
		        	
		            return null;
		        }
		 
		        return null;
		    }
		});
		         
		Optional<Properties> result = dialog.showAndWait();
		
		senderService.closeAllConnection();
		
		boolean isConnected = false;
		
		if(result.isPresent()) {
			
			senderService.setServerProperties(result.get());
			isConnected = connect();
			
			if(!isConnected) {
				senderService.getServerProperties().clear();
			}
			
		}
		
		
		return isConnected;
	}
	
	
	
	boolean connect() {
		
		Alert alertOnConnect = new Alert(AlertType.NONE);
		ExceptionExpandedUI expandableUI = new ExceptionExpandedUI(alertOnConnect);
		boolean isConnected = false;
		
		String headerText = "Connected";
		String contentText = "Successfully connected!";
		AlertType alertType = AlertType.INFORMATION;
		
		
    	try {
    		
			// waiting - animation effect
			Service<Boolean> service = new Service<>() {

				@Override
				protected Task<Boolean> createTask() {
					
			        Task<Boolean> task = new Task<>() {
			        	
			        	@Override
						protected Boolean call() throws Exception {
							return senderService.connect();
						}
						
					};
					
					task.setOnSucceeded( eventHandle -> {
						generalUseBorderPane.setSucceed(true);
						generalUseBorderPane.closeStage();
					});
					
					task.setOnFailed( eventHandle -> {
						generalUseBorderPane.setSucceed(false);
						generalUseBorderPane.closeStage();
						try {
							returnHome();
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
					
					task.setOnCancelled( cancelledEvent -> {
						generalUseBorderPane.setSucceed(false);
						generalUseBorderPane.closeStage();
						try {
							returnHome();
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
					
					return task;
				}
				
			};
			
			service.start();
			loadingAnimation();
			
			if(service.getValue() == null) {
				
				headerText = "Connection refused";
				contentText = "You might connecting to an invalid server address or the receiver might not response to your request. Try again.";
				alertType = AlertType.ERROR;
				
			} else if(!service.getValue()){
				
				headerText = "Connection reject";
				contentText = "You have been rejected by the receiver, mate.";
				alertType = AlertType.ERROR;
				
			} else {
				
				isConnected = true;
				
			}
			
			
			
//			if(!(isConnected = senderService.connect())) {
//				
//				headerText = "Connection refused";
//				contentText = "You might connecting to an invalid server address. Try again";
//				alertType = AlertType.ERROR;
//				
//			}
			
			
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
			expandableUI.addExceptionStackTrace(e);
			headerText = "Invalid inputs";
			contentText = "You entered invalid inputs. Provide valid inputs.";
			alertType = AlertType.ERROR;
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			expandableUI.addExceptionStackTrace(e);
			headerText = "Unknown Server";
			contentText = "Unable to connect to server. Make sure you provide the right address of the server.";
			alertType = AlertType.ERROR;
			
		} catch (IOException e) {
			e.printStackTrace();
			expandableUI.addExceptionStackTrace(e);
			headerText = "Connection unavailable";
			contentText = "Unable to start connection. Make sure you are connected to a network.";
			alertType = AlertType.ERROR;
			
		}
    	
		alertOnConnect.setHeaderText(headerText);
		alertOnConnect.setContentText(contentText);
		alertOnConnect.setAlertType(alertType);
		alertOnConnect.show();
		
		return isConnected;
	}
	
	
	
	
	
	public void returnHome() throws IOException {
		closeAll();
		Parent parent = rootManager.getParent();
		Scene scene = parent.getScene();
		Parent fxmlParent = rootManager.loadFXML(rootManager.getRegisteredResources().get(RootManager.FXML_HOME));
		scene.setRoot(fxmlParent);
	}
	
	
	
	
	public void closeAll() {
		CLOSING_STAGE = true;
		senderService.closeAllConnection();
	}
	
	
	
	
	
	private void loadingAnimation() throws IOException {
		
		FXMLLoader fxmlLoader = new FXMLLoader(rootManager.getRegisteredResources().get(RootManager.FXML_GENERAL_USER_BORDERPANE).getURL());
		
        Parent fxmlParent = fxmlLoader.load();
        
        Scene scene = new Scene(fxmlParent, 300, 200);
        Stage stage = new Stage();
        stage.setResizable(false);
        
        generalUseBorderPane = fxmlLoader.getController();
        generalUseBorderPane.setStage(stage);
        
        ProgressIndicator progressIndicatorIndefinite = new ProgressIndicator();
        generalUseBorderPane.getGeneralUseBorderPane().setCenter(progressIndicatorIndefinite);
        Label text = new Label();
        text.setText("Waiting for receiver to accept the request.");
        generalUseBorderPane.getGeneralUseBorderPane().setBottom(text);
        
        stage.setMinWidth(300);
        stage.setMinHeight(200);
        stage.setMaxWidth(300);
        stage.setMaxHeight(200);
        stage.setOnCloseRequest( (eventHandle) -> {
        	generalUseBorderPane.closeStage();
        	if(!generalUseBorderPane.isSucceed()) {
        		try {
					returnHome();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        });
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
        
	}
	
	
	
	
	
	private class ListViewService extends Service<ObservableList<String>>{

		private final SenderController senderController;
		
		private ListViewService(SenderController sendProcess) {
			this.senderController = sendProcess;
		}
		
		@Override
		protected Task<ObservableList<String>> createTask() {
			return new ListViewTask(this.senderController);
		}
		
	}
	
	
	
	
	private class ListViewTask extends Task<ObservableList<String>>{

		private final SenderController senderController;
		
		private ListViewTask(SenderController senderController) {
			this.senderController = senderController;
		}
		
		@Override
		protected ObservableList<String> call() throws Exception {
			this.senderController.sendNow();
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
		private final ExecutorService exeService = Executors.newSingleThreadExecutor();
		
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
		
		// wara pa kagamiti
		private void shutdownMonitoring() {
			exeService.shutdownNow();
		}
	}
	
}

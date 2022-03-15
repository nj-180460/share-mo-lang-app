package org.sharemolangapp.smlapp.receiver;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sharemolangapp.smlapp.StageInitializer.RootManager;
import org.sharemolangapp.smlapp.controller.ExceptionUI;
import org.sharemolangapp.smlapp.controller.GeneralUseBorderPane;
import org.sharemolangapp.smlapp.layer.Workable;
import org.sharemolangapp.smlapp.util.ConfigConstant;
import org.sharemolangapp.smlapp.util.GenericUtils;
import org.sharemolangapp.smlapp.util.QRCodeUtil;

import com.google.zxing.WriterException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.control.Alert.AlertType;



public class ReceiverController implements Initializable{

	
	private static final RootManager rootManager = RootManager.getRootManager();
	private static boolean TRANSFER_MODE = false;
	private static boolean CLOSING_STAGE = false;
	private static boolean CLOSING_SCENE = false;
	
	private final ObservableList<ProgressIndicatorBar> obsReceivedListView = FXCollections.observableArrayList();
	
	private ReceiverService receiverService;
	private WorkMonitor workMonitor;
	
	private GeneralUseBorderPane generalUseBorderPane;
	@FXML private Button receiverManageConnectionButton;
	@FXML private Button backHomeButton;
	@FXML private ImageView qrcodeImageServerContent;
	@FXML private Label labelReceiverHost;
	@FXML private Label labelReceiverPort;
	@FXML private Label connectedToLabel;
	@FXML private ListView<ProgressIndicatorBar> receivedListViewReceiver;

	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		CLOSING_STAGE = false;
		CLOSING_SCENE = false;
		TRANSFER_MODE = false;
		
		receiverService = new ReceiverService(this);
		initializeData();
		
		receivedListViewReceiver.setItems(obsReceivedListView);
		handleListviewReceive(receivedListViewReceiver);
		
	}
	
	
	public void closeAll() {
		CLOSING_SCENE = true;
		if(workMonitor != null) {
			workMonitor.shutdownMonitoring();
		}
		receiverService.closeAll();
	}
	
	
	
	void setConnectedToText(final String clientConnectedTo) {
		Platform.runLater( () -> {
			connectedToLabel.setText(clientConnectedTo == null ? "" : clientConnectedTo);
		});
	}
	
	
	
	private void initializeData() {
		
		Alert alertOn = new Alert(AlertType.NONE);
		ExceptionUI exceptionUI = new ExceptionUI(alertOn);
		
		try {
			
			Service<Void> service = new Service<>() {

				@Override
				protected Task<Void> createTask() {
					
			        Task<Void> task = new Task<>() {
			        	
			        	@Override
						protected Void call() throws Exception {
							
			        		receiverService.setUpServerProperties();
							receiverService.startServer();
							receiverService.startWaitingForClient();
							
							return null;
						}
						
					};
					
					
					task.setOnSucceeded( eventHandle -> {
						
						generalUseBorderPane.closeStage();
						
						try {
							initializaDataOnView(receiverService.getServerProperties());
						} catch (URISyntaxException | WriterException e) {
							
							e.printStackTrace();
							exceptionUI.addExceptionStackTrace(e);
							alertOn.setHeaderText("Failed to generate QR Code");
							alertOn.setContentText(e.toString());
							alertOn.setAlertType(AlertType.ERROR);
							alertOn.show();
							
						}
						
					});
					
					
					task.setOnFailed( eventHandle -> {
						
						generalUseBorderPane.closeStage();
						closeAll();
						
					});
					
					
					return task;
				}
				
			};
			
			service.start();
			loadingAnimation();
			
		} catch (IOException e) {
			e.printStackTrace();
			exceptionUI.addExceptionStackTrace(e);
			alertOn.setHeaderText("Failed to load FXML resource");
			alertOn.setContentText(e.toString());
			alertOn.setAlertType(AlertType.ERROR);
			alertOn.show();
		}
	}
	
	
	
	private void initializaDataOnView(Properties serverProperties) throws URISyntaxException, WriterException {
		
		labelReceiverHost.setText(String.valueOf(serverProperties.get("host")));
		labelReceiverPort.setText(String.valueOf(serverProperties.get("port")));
		qrcodeImageServerContent.setImage(SwingFXUtils.toFXImage(QRCodeUtil.generateBufferedQRCodeImage(serverProperties), null));
		
	}
	
	
	
	
	private void loadingAnimation() throws IOException {
		
		FXMLLoader fxmlLoader = new FXMLLoader(rootManager.getRegisteredResources().get(RootManager.FXML_GENERAL_USER_BORDERPANE).getURL());
		
        Parent fxmlParent = fxmlLoader.load();
        
        Scene scene = new Scene(fxmlParent, 300, 200);
        Stage stage = new Stage();
        
        generalUseBorderPane = fxmlLoader.getController();
        generalUseBorderPane.setStage(stage);
        
        ProgressIndicator progressIndicatorIndefinite = new ProgressIndicator();
        generalUseBorderPane.getGeneralUseBorderPane().setCenter(progressIndicatorIndefinite);
        Label text = new Label();
        text.setText("Starting server...Please wait.");
        generalUseBorderPane.getGeneralUseBorderPane().setBottom(text);
        
        stage.setMinWidth(300);
        stage.setMinHeight(200);
        stage.setMaxWidth(300);
        stage.setMaxHeight(200);
        stage.setOnCloseRequest((eventHandle) -> {
        	generalUseBorderPane.closeStage(); // === stage.close();
			closeAll();
        });
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
        
	}
	
	
	
	boolean serverConfirmation() {
		
		Platform.runLater( () -> {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirmation");
			alert.setHeaderText("Someone is trying to send files. Accept to receive those files.");
			alert.setContentText("Accept?");
			alert.setOnCloseRequest( event -> {
				receiverService.setRequest(ConfigConstant.DECLINE_RESPONSE);
				alert.close();
			});
			alert.showAndWait()
				.filter( result -> result == ButtonType.OK)
				.ifPresentOrElse(
						result -> { 
							receiverService.setRequest(ConfigConstant.OK_RESPONSE);
							setConnectedToText("Connected: "+ receiverService.getClientProperties().get("host").toString());
						},
						() -> {
							receiverService.setRequest(ConfigConstant.DECLINE_RESPONSE);
							setConnectedToText("");
						});
		});
		
		while(receiverService.getResponse().equals(ConfigConstant.CLIENT_REQUESTING_RESPONSE)
				&& receiverService.isConnected()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return receiverService.getResponse().equals(ConfigConstant.OK_RESPONSE);
	}
	
	
	
	@FXML
	private void handlerReceiverManageConnection(ActionEvent actionEvent) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(rootManager.getRegisteredResources().get(RootManager.FXML_MANAGE_CONNECTION).getURL());
        Parent fxmlParent = fxmlLoader.load();
        
        ManageConnection dialogController = fxmlLoader.getController();
        dialogController.setParentController(this);
        dialogController.setReceiverService(receiverService);
        
        Scene scene = new Scene(fxmlParent, 400, 200);
        Stage stage = new Stage();
        
        stage.setOnCloseRequest((eventHandle) -> stage.close());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
	}
	
	
	
	
	@FXML
	private void handleBackHome(ActionEvent actionEvent) throws IOException {
		
		closeAll();
		generalUseBorderPane.closeStage();
		
		Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
		Scene scene = stage.getScene();
		Parent fxmlParent = rootManager.loadFXML(rootManager.getRegisteredResources().get(RootManager.FXML_HOME));
//		FXMLLoader fxmlLoader = rootManager.getFXMLLoader();
//		HomeController init = fxmlLoader.getController();
		scene.setRoot(fxmlParent);
	}
	
	
	
	
	WorkMonitor getWorkMonitor() {
		return workMonitor;
	}
	
	
	void addMonitoringFile(File file, String fileSize) {

		workMonitor = new WorkMonitor(Long.parseLong(fileSize));
		ProgressIndicatorBar withProgressBar = new ProgressIndicatorBar(workMonitor, Double.parseDouble(fileSize), file.getAbsolutePath());
		
		Service<Void> service = new Service<>() {

			@Override
			protected Task<Void> createTask() {
				
				return new Task<Void>() {

					@Override
					protected Void call() throws Exception {
						
						Platform.runLater( () -> {
							obsReceivedListView.add(withProgressBar);
						});
						
						TRANSFER_MODE = true;
						withProgressBar.startMonitoring();
						TRANSFER_MODE = false;
						
						return null;
					}
					
				};
			}
			
		};
		
		service.start();
	}
	
	
	
	
	
	private void handleListviewReceive(ListView<ProgressIndicatorBar> listviewQueue) {
		
		// custom progress bar
		listviewQueue.setCellFactory(new Callback<ListView<ProgressIndicatorBar>, ListCell<ProgressIndicatorBar>>() {
            @Override
            public ListCell<ProgressIndicatorBar> call(ListView<ProgressIndicatorBar> listView) {
                return new ProgressBarListCell();
            }
        });
		
		MultipleSelectionModel<ProgressIndicatorBar> multiSelectionModel = listviewQueue.getSelectionModel();
		multiSelectionModel.setSelectionMode(SelectionMode.MULTIPLE);
	}
	
	
	
	
	/***
	 * 
	 * Custom progress bar
	 *
	 */
	class ProgressIndicatorBar extends StackPane {
		
		private static final int DEFAULT_LABEL_PADDING = 1;
		
		private final WorkMonitor workDone;
		private final double totalWork;

		private final ProgressBar bar  = new ProgressBar();
		private final Text text = new Text();
		private final String labelFormatSpecifier;
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

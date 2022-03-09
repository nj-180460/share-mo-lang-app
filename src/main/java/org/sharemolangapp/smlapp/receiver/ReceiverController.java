package org.sharemolangapp.smlapp.receiver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import org.sharemolangapp.smlapp.StageInitializer.RootManager;
import org.sharemolangapp.smlapp.controller.GeneralUseBorderPane;
import org.sharemolangapp.smlapp.util.QRCodeUtil;

import com.google.zxing.WriterException;

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
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;



public class ReceiverController implements Initializable{

	
	private static boolean TRANSFER_MODE = false;
	
	private final RootManager rootManager = RootManager.getRootManager();
	
//	private final ArrayList<ProgressIndicatorBar> selectedProgresBar = new ArrayList<>();
	private ReceiverService receiverService;
	
	private GeneralUseBorderPane generalUseBorderPane;
	@FXML private Button receiverManageConnectionButton;
	@FXML private Button backHomeButton;
	@FXML private ImageView qrcodeImageServerContent;
	@FXML private Label labelReceiverHost;
	@FXML private Label labelReceiverPort;
//	@FXML private ListView<ProgressIndicatorBar> listOnline;
//	private ObservableList<ProgressIndicatorBar> observableListOnline;

	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
//		observableListOnline = listOnline.getItems();
		
//		listOnline.setCellFactory(new Callback<ListView<ProgressIndicatorBar>, ListCell<ProgressIndicatorBar>>() {
//            @Override
//            public ListCell<ProgressIndicatorBar> call(ListView<ProgressIndicatorBar> listView) {
//                return new ProgressBarListCell();
//            }
//        });
		
		receiverService = new ReceiverService();
		initializeData();
		
//		handleListviewQueue(listOnline);
	}
	
	
	public void closeAll() {
		receiverService.closeAll();
	}
	
	
	
	private void initializeData() {
		
		Alert alertOn = new Alert(AlertType.NONE);
		
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
	
	
	
	
	@FXML
	private void handlerReceiverManageConnection(ActionEvent actionEvent) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(rootManager.getRegisteredResources().get(RootManager.FXML_MANAGE_CONNECTION).getURL());
        Parent fxmlParent = fxmlLoader.load();
        
        ManageConnection dialogController = fxmlLoader.getController();
        dialogController.setParentController(this);
        
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
	
	
	
	
//	private void handleListviewQueue(ListView<ProgressIndicatorBar> listviewQueue) {
//		MultipleSelectionModel<ProgressIndicatorBar> multiSelectionModel = listviewQueue.getSelectionModel();
//		multiSelectionModel.setSelectionMode(SelectionMode.MULTIPLE);
//		ReadOnlyObjectProperty<ProgressIndicatorBar> readOnlyProp = multiSelectionModel.selectedItemProperty();
//		readOnlyProp.addListener( new ChangeListener<ProgressIndicatorBar>() {
//
//			@Override
//			public void changed(ObservableValue<? extends ProgressIndicatorBar> observable, ProgressIndicatorBar oldValue,
//					ProgressIndicatorBar newValue) {
//				selectedProgresBar.clear();
//				selectedProgresBar.addAll(multiSelectionModel.getSelectedItems());
//			}
//			
//		});
//	}
	
	
	
//	private ProgressIndicatorBar onProgressFile(String fileName) {
//		
//		final int TOTAL_WORK = 20;
//	    final String WORK_DONE_LABEL_FORMAT = fileName+": "+TOTAL_WORK+"/%.0f";
//
//	    final ProgressIndicatorBar progressBar = new ProgressIndicatorBar(
//	        workDone.getReadOnlyProperty(),
//	        TOTAL_WORK,
//	        WORK_DONE_LABEL_FORMAT
//	    );
//
//		
//		return progressBar;
//	}
	
	
	
	
	/***
	 * 
	 * Custom progress bar
	 *
	 */
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
//		private final String fileName;
//
//		
//		ProgressIndicatorBar(final WorkMonitor workDone, final double totalWork, final String labelFormatSpecifier) {
//			this.workDone  = workDone;
//		    this.totalWork = totalWork;
//		    this.labelFormatSpecifier = "(%s/"+toMB(totalWork)+") "+labelFormatSpecifier;
//		    this.fileName = labelFormatSpecifier;
//		    
//		    text.setTextAlignment(TextAlignment.LEFT);
//		    text.setText(String.format(this.labelFormatSpecifier, toMB(Math.ceil(workDone.getWorkDone()))));
//		    bar.setMaxWidth(Double.MAX_VALUE); // allows the progress bar to expand to fill available horizontal space.
//		    bar.setStyle("-fx-accent: #0F0;");
//		    bar.setProgress(workDone.getWorkDone() / totalWork);
//		    
//		    getChildren().setAll(bar, text);
//		}
//
//		// synchronizes the progress indicated with the work done.
//		private void syncProgress(double progress, String computedWorkDone) {
//			if (workDone == null || totalWork == 0) {
//				text.setText("");
//				bar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
//			} else {
//				text.setText(String.format(labelFormatSpecifier, computedWorkDone));
//				bar.setProgress(progress);
//			}
//			
//			bar.setMinHeight(text.getBoundsInLocal().getHeight() + DEFAULT_LABEL_PADDING * 2);
//			bar.setMinWidth (text.getBoundsInLocal().getWidth()  + DEFAULT_LABEL_PADDING * 2);
//		}
//		
//		private void startMonitoring() {
//		    workDone.monitorWorker( () -> {
//		    	String computedValue = toMB(workDone.getWorkDone());
//		    	double progress = workDone.getWorkDone() / totalWork;
//		    	Platform.runLater( () -> {
//		    		syncProgress(progress, computedValue);
//		    	});
//		    });
//		}
//		
//		private String toMB(double value) {
//			double computedValue =  (value/1024)/1024;
//			return String.format("%,.2f", computedValue)+"MB";
//		}
//	}
	
	
	
	/**
	 * 
	 * Custome cell on listview
	 *
	 */
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
	
	
	
	
	
	
	
//	class WorkMonitor {
//		
//		private final long totalWork;
//		
//		private long workDone;
//		private long oldValue = workDone;
//		
//		public WorkMonitor(long totalWork) {
//			this.totalWork = totalWork;
//		}
//		
//		public long getTotalWork() {
//			return totalWork;
//		}
//		
//		public void setWorkDone(long workDone) {
//			this.workDone = workDone;
//		}
//		
//		public long getWorkDone() {
//			return workDone;
//		}
//		
//		public void monitorWorker(Workable worker) {
//			ExecutorService exeService = Executors.newSingleThreadExecutor();
//			exeService.execute( () -> {
//				while(workDone != totalWork) {
//					try {
//						Thread.sleep(GenericUtils.TRANSFER_RATE_MS);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//					if(workDone != oldValue) {
//						oldValue = workDone;
//						worker.work();
//					}
//				}
//				TRANSFER_MODE = false;
//			});
//			exeService.shutdown();
//		}
//	}
	
}

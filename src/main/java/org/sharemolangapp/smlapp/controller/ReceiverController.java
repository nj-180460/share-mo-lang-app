package org.sharemolangapp.smlapp.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.sharemolangapp.smlapp.layer.ReceivableProcess;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.Duration;



public class ReceiverController implements Initializable{

	private final ArrayList<ProgressIndicatorBar> selectedProgresBar = new ArrayList<>();
	
	@FXML private ListView<ProgressIndicatorBar> listOnline;
	private ObservableList<ProgressIndicatorBar> observableListOnline;

	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		observableListOnline = listOnline.getItems();
		
		listOnline.setCellFactory(new Callback<ListView<ProgressIndicatorBar>, ListCell<ProgressIndicatorBar>>() {
            @Override
            public ListCell<ProgressIndicatorBar> call(ListView<ProgressIndicatorBar> listView) {
                return new ProgressBarListCell();
            }
        });
		
		
		observableListOnline.addAll(
				onProgressFile("File 1"),
				onProgressFile("File 2"),
				onProgressFile("File 3"),
				onProgressFile("File 4"),
				onProgressFile("File 5"));
		
		handleListviewQueue(listOnline);
	}
	
	
	
	private void handleListviewQueue(ListView<ProgressIndicatorBar> listviewQueue) {
		MultipleSelectionModel<ProgressIndicatorBar> multiSelectionModel = listviewQueue.getSelectionModel();
		multiSelectionModel.setSelectionMode(SelectionMode.MULTIPLE);
		ReadOnlyObjectProperty<ProgressIndicatorBar> readOnlyProp = multiSelectionModel.selectedItemProperty();
		readOnlyProp.addListener( new ChangeListener<ProgressIndicatorBar>() {

			@Override
			public void changed(ObservableValue<? extends ProgressIndicatorBar> observable, ProgressIndicatorBar oldValue,
					ProgressIndicatorBar newValue) {
				selectedProgresBar.clear();
				selectedProgresBar.addAll(multiSelectionModel.getSelectedItems());
			}
			
		});
	}
	
	
	
	private ProgressIndicatorBar onProgressFile(String fileName) {
		
		final int TOTAL_WORK = 20;
	    final String WORK_DONE_LABEL_FORMAT = fileName+": "+TOTAL_WORK+"/%.0f";

	    final ReadOnlyDoubleWrapper workDone  = new ReadOnlyDoubleWrapper();

	    final ProgressIndicatorBar progressBar = new ProgressIndicatorBar(
	        workDone.getReadOnlyProperty(),
	        TOTAL_WORK,
	        WORK_DONE_LABEL_FORMAT
	    );

	    final Timeline countDown = new Timeline(
	        new KeyFrame(Duration.millis(5000), new KeyValue(workDone, TOTAL_WORK))
	    );
	    countDown.play();
		
		return progressBar;
	}
	
	
	
	
	/***
	 * 
	 * Custom progress bar
	 *
	 */
	class ProgressIndicatorBar extends StackPane {
		
		final private static int DEFAULT_LABEL_PADDING = 1;
		
		final private ReadOnlyDoubleProperty workDone;
		final private double totalWork;

		final private ProgressBar bar  = new ProgressBar();
		final private Text text = new Text();
		final private String labelFormatSpecifier;

		
		ProgressIndicatorBar(final ReadOnlyDoubleProperty workDone, final double totalWork, final String labelFormatSpecifier) {
			this.workDone  = workDone;
		    this.totalWork = totalWork;
		    this.labelFormatSpecifier = labelFormatSpecifier;
		    
		    bar.setMaxWidth(Double.MAX_VALUE); // allows the progress bar to expand to fill available horizontal space.
		    bar.setStyle("-fx-accent: #0F0;");

		    syncProgress();
		    workDone.addListener(new ChangeListener<Number>() {
		    	@Override
		    	public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
		    		syncProgress();
		    	}
		    });
		    
		    getChildren().setAll(bar, text);
		}

		// synchronizes the progress indicated with the work done.
		private void syncProgress() {
			if (workDone == null || totalWork == 0) {
				text.setText("");
				bar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
			} else {
				text.setText(String.format(labelFormatSpecifier, Math.ceil(workDone.get())));
				bar.setProgress(workDone.get() / totalWork);
			}
			
			bar.setMinHeight(text.getBoundsInLocal().getHeight() + DEFAULT_LABEL_PADDING * 2);
			bar.setMinWidth (text.getBoundsInLocal().getWidth()  + DEFAULT_LABEL_PADDING * 2);
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
	
	
	
//	private class ProgressBarTask extends Task<Void>{
//
//		private final SendableProcess sendProcess;
//		
//		private ProgressBarTask(SendableProcess sendProcess) {
//			this.sendProcess = sendProcess;
//		}
//		
//		@Override
//		protected Void call() throws Exception {
//			sendProcess.process();
//			return null;
//		}
//		
//	}
}

package org.sharemolangapp.smlapp.receiver;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.Properties;

import org.sharemolangapp.smlapp.receiver.ReceiverController.WorkMonitor;
import org.sharemolangapp.smlapp.util.ConfigConstant;
import org.sharemolangapp.smlapp.util.GenericUtils;
import org.sharemolangapp.smlapp.util.JSONFactory;
import org.sharemolangapp.smlapp.util.NetworkUtility;



class ReceiverService {

	private static int NUMBER_OF_FILES = 0;
	
	private final ReceiverController receiverController;
	private final ReceiverNetwork receiverNetwork;
	private final Properties serverProperties;
	private final Properties clientProperties;
	private final ReceiveOnClientHandler receiveOnClientHandler;
	private final String yourName;
	
	private String feedback;
	
	
	ReceiverService(ReceiverController receiverController){
		this.receiverController = receiverController;
		this.receiverNetwork = new ReceiverNetwork(this);
		this.serverProperties = new Properties();
		this.clientProperties = new Properties();
		this.receiveOnClientHandler = new ReceiveOnClientHandler();
		this.feedback = ConfigConstant.NONE_RESPONSE;
		this.yourName = JSONFactory.Settings.getJsonParentValue(
				ConfigConstant.getJsonRelativePath(ConfigConstant.PREFERENCES_JSON_FILE),
				JSONFactory.Settings.YOUR_NAME_PNODE);
	}
	
	
	void setUpServerProperties() throws ReceiverNetworkException {
		
		String host = NetworkUtility.getMachineIPv4Address().getHostAddress();
		int port = NetworkUtility.freePort();
		
		if((host == null || host.isBlank()) && (port <= 0)) {
			throw new ReceiverNetworkException("IP Address and Port must be present.");
		}
		
		serverProperties.put("host", host);
		serverProperties.put("port", port);
		serverProperties.put("serverName", yourName);
		
	}
	
	
	ReceiveOnClientHandler getReceiveOnClientHandler() {
		return receiveOnClientHandler;
	}
	
	
	Properties getServerProperties() {
		return serverProperties;
	}
	
	Properties getClientProperties() {
		return clientProperties;
	}
	
	
	boolean startServer() throws NumberFormatException, UnknownHostException, IOException {
		return receiverNetwork.startServer();
	}
	
	
	void startClientService() {
		receiverNetwork.waitingForClient(receiveOnClientHandler);
	}
	
	
	boolean isConnected() {
		return receiverNetwork.isConnected();
	}
	
	
	synchronized void closeAll() {
		setRequest(ConfigConstant.NONE_RESPONSE);
		receiverNetwork.closeConnection();
	}
	
	
	void closeClientConnection() {
		receiverNetwork.closeClientConnection();
	}
	
	
	
	void setRequest(String feedback) {
		this.feedback = feedback;
	}
	
	String getResponse() {
		return feedback;
	}
	
	
	
	boolean serverConfirmation() {
		return receiverController.serverConfirmation();
	}

	
	
	
	
	
	
	void openFileExplorerLocation(String absolutePath) {
		
		if(GenericUtils.IS_WINDOWS) {
			try {
				Runtime.getRuntime().exec("explorer /select, "+absolutePath); // for windows os platform
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	
	
	
	
	class ReceiveOnClientHandler {
		
		private BufferedInputStream bufferedInputStream;
		private DataInputStream dataInputStream;
		private DataOutputStream dataOutputStream;
		
		
		void setClientStreams(
				DataInputStream dataInputStream,
				DataOutputStream dataOutputStream,
				BufferedInputStream bufferedInputStream) {
			this.dataInputStream = dataInputStream;
            this.dataOutputStream = dataOutputStream;
			this.bufferedInputStream = bufferedInputStream;
		}
		
		
		
		// 3 way handshake confirmation/verification
		void onRead() throws IOException {
			
			// code 1
			while((NUMBER_OF_FILES = dataInputStream.readInt()) >= 0){
				dataOutputStream.writeInt(NUMBER_OF_FILES);
	            dataOutputStream.flush();

	            while(NUMBER_OF_FILES > 0){
	            	
	                onWaitFile();

	                NUMBER_OF_FILES--;
	                if(NUMBER_OF_FILES > 0){
	                    dataOutputStream.writeInt(NUMBER_OF_FILES);
	                    dataOutputStream.flush();
	                }
	            }
			}
			
			// code 2 - preferred, unless OEFException solved
//			while(dataInputStream.available() > 0) { // this resolve OEFException being thrown, pero diri gud ma-solve hays
//				if((NUMBER_OF_FILES = dataInputStream.readInt()) >= 0){
//					dataOutputStream.writeInt(NUMBER_OF_FILES);
//		            dataOutputStream.flush();
//	
//		            while(NUMBER_OF_FILES > 0){
//		            	
//		                onWaitFile();
//	
//		                NUMBER_OF_FILES--;
//		                if(NUMBER_OF_FILES > 0){
//		                    dataOutputStream.writeInt(NUMBER_OF_FILES);
//		                    dataOutputStream.flush();
//		                }
//		            }
//				}
//			}
			
		}
		
		
		
		
		void onWaitFile() throws IOException {
			String fileProperty = dataInputStream.readUTF();
			String []fileProps = fileProperty.split(":");
			String fileName = fileProps[0];
			String fileSize = fileProps[1];

	        boolean validFileName = (fileName != null && !fileName.isBlank());
	        String response = (validFileName ? ConfigConstant.OK_RESPONSE : ConfigConstant.FILENAME_EMPTY_RESPONSE);
	        dataOutputStream.writeUTF(response);
	        dataOutputStream.flush();

	        if(validFileName){
	            readFile(fileName, fileSize, bufferedInputStream);
	        }
	    }
		
		
		
		
		private void readFile(String fileName, String fileSize, InputStream inputStream) throws IOException  {

			StringBuilder receivingFolder = new StringBuilder();
			receivingFolder.append(
					JSONFactory.Settings.getJsonParentValue(
						ConfigConstant.getJsonRelativePath(ConfigConstant.PREFERENCES_JSON_FILE),
						JSONFactory.Settings.OUTPUT_FOLDER_PNODE));
			receivingFolder.append(fileName);
			
	        File receivingFile = new File(receivingFolder.toString());	        

	        try(FileOutputStream outputStream = new FileOutputStream(receivingFile)){

		        receiverController.addMonitoringFile(receivingFile, fileSize);
		        WorkMonitor workMonitor = receiverController.getWorkMonitor();
	        	
		        byte[] bytes = new byte[GenericUtils.DEFAULT_BUFFER_SIZE];
	        	long workDone = 0;
	            int count;
	            while ((count = inputStream.read(bytes)) >= 0) {
	            	
	                outputStream.write(bytes, 0, count);
	                
	                workDone += count;
	                workMonitor.setWorkDone(workDone);
	                
	                if(count < bytes.length){ // EOF (end of file)
	                    break;
	                }
	                
	                dataOutputStream.writeUTF(ConfigConstant.RECEIVED_BYTES_RESPONSE);
	            }
	            
	            workMonitor.setWorkDone(workMonitor.getTotalWork());
	            outputStream.flush();

	        } catch(IOException io){
	            io.printStackTrace();
	            Files.deleteIfExists(receivingFile.toPath());
	        }
	        
	    }
		
	}
	
}

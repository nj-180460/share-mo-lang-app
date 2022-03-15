package org.sharemolangapp.smlapp.sender;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.sharemolangapp.smlapp.sender.SenderController.WorkMonitor;
import org.sharemolangapp.smlapp.util.ConfigConstant;
import org.sharemolangapp.smlapp.util.GenericUtils;



class SenderService {
	
	private static int NUMBER_OF_FILES = 0;
	private static boolean SERVER_ACCEPTED;
	
	private final SenderNetwork senderNetwork;
	private final SendOnServerHandler sendOnServerHandler;
	private final Properties serverProperties;
	
	
	SenderService(){
		sendOnServerHandler = new SendOnServerHandler();
		senderNetwork = new SenderNetwork(this, sendOnServerHandler);
		serverProperties = new Properties();
	}
	
	
	void setServerProperties(Properties serverProp) {
		
		// CHECK PROPERTIES FIRST BEFORE PROCEEDING TO SenderNetork)
		serverProperties.clear();
		serverProperties.putAll(serverProp);
	}
	
	Properties getServerProperties() {
		return serverProperties;
	}
	
	
	boolean testConnection() throws NumberFormatException, UnknownHostException, IOException, InterruptedException, ExecutionException {
		return senderNetwork.testConnection();
	}
	
	
	Boolean connect() throws NumberFormatException, UnknownHostException, IOException, InterruptedException, ExecutionException {
		return senderNetwork.connectNow(false);
	}
	
	
	boolean isConnected(){
		return senderNetwork.isActive();
	}
	
	
	void sendFileTo(File file, WorkMonitor workMonitor) throws IOException {
		sendOnServerHandler.onSend(file, workMonitor);
	}
	
	
	boolean isServerAccepted() {
		return SERVER_ACCEPTED;
	}
	
	
	void setServerAccepted(boolean isAccepted) {
		SERVER_ACCEPTED = isAccepted;
	}
	
	
	
	void setTotalQueuedFiles(int filesToSend) throws IOException {
		NUMBER_OF_FILES = filesToSend;
		sendOnServerHandler.setGroupFileProp();
	}
	
	
	void openFileExplorerLocation(File file) {
		
		if(GenericUtils.IS_WINDOWS) {
			try {
				Runtime.getRuntime().exec("explorer /select, "+file.getAbsolutePath()); // for windows os
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	
	void closeAllConnection() {
		SERVER_ACCEPTED = false;
		senderNetwork.closeClientConnection();
	}
	
	
	
	
	
	
	class SendOnServerHandler {
		
		private BufferedOutputStream bufferedOutputStream;
		private DataOutputStream dataOutputStream;
		private DataInputStream dataInputStream;
		private FileInputStream fileInputStream;
		
		
		SendOnServerHandler(){
			
		}
		
		
		void setStreams(InputStream inputStream, OutputStream outputStream) {
			dataInputStream = new DataInputStream(inputStream);
			bufferedOutputStream = new BufferedOutputStream(outputStream);
			dataOutputStream = new DataOutputStream(outputStream);
		}
		
		
		// 3 way handshake style
		private void setGroupFileProp() throws IOException {
            dataOutputStream.writeInt(NUMBER_OF_FILES);
            dataOutputStream.flush();
		}
		
		
		private void onSend(File file,  WorkMonitor workMonitor) throws IOException {
			
			NUMBER_OF_FILES = dataInputStream.readInt();
			
			try (FileInputStream fileInputStream = new FileInputStream(file)){
	        	this.fileInputStream = fileInputStream;
	        	
	            String fileName = file.getName();
	            StringBuilder fileProperty = new StringBuilder();
	            fileProperty.append(fileName);
	            fileProperty.append(":");
	            fileProperty.append(file.length());
	            
	            dataOutputStream.writeUTF(fileProperty.toString());
	            dataOutputStream.flush();

	            String serverResponse = dataInputStream.readUTF();

	            if(serverResponse.equals(ConfigConstant.OK_RESPONSE)){
	                writeToSend(workMonitor, fileInputStream);
	            } else if(serverResponse.equals(ConfigConstant.FILENAME_EMPTY_RESPONSE)){
	            	System.out.println(getClass().getName() +" LINE 147: Server response = " + serverResponse);
	            }
	        }
			
	    }
		
		
		
		// send files by bytes
		void writeToSend(WorkMonitor workMonitor, FileInputStream fileInputStream) throws IOException {
			
			long transferred = 0;
	        byte[] buffer = new byte[GenericUtils.DEFAULT_BUFFER_SIZE];
	        int read;
	        while ((read = fileInputStream.read(buffer, 0, GenericUtils.DEFAULT_BUFFER_SIZE)) > 0) {
	        	bufferedOutputStream.write(buffer, 0, read);
	            transferred += read;
	            workMonitor.setWorkDone(transferred);
	        }
	        bufferedOutputStream.flush();
		}
		
		
		
		synchronized void closeStream() {
			try {
				if(fileInputStream != null) {
					fileInputStream.close();
				}
				if(dataOutputStream != null) {
					dataOutputStream.close();
				}
				if(bufferedOutputStream != null) {
					bufferedOutputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}

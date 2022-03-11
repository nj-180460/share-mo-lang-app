package org.sharemolangapp.smlapp.sender;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.Properties;

import org.sharemolangapp.smlapp.sender.SenderController.WorkMonitor;
import org.sharemolangapp.smlapp.util.GenericUtils;



class SenderService {
	
	private static int NUMBER_OF_FILES = 0;
	
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
		serverProperties.putAll(serverProp);
	}
	
	Properties getServerProperties() {
		return serverProperties;
	}
	
	
	boolean testConnection() throws NumberFormatException, UnknownHostException, IOException {
		return senderNetwork.testConnection();
	}
	
	boolean connect() throws NumberFormatException, UnknownHostException, IOException {
		return senderNetwork.connectNow();
	}
	
	
	void sendFileTo(File file, WorkMonitor workMonitor) throws IOException {
		sendOnServerHandler.onSend(file, workMonitor);
	}
	
	
	
	int filesLeft() {
		return NUMBER_OF_FILES;
	}
	
	void setFilesLeft(int filesToSend) throws IOException {
		NUMBER_OF_FILES = filesToSend;
		sendOnServerHandler.setGroupFileProp(NUMBER_OF_FILES);
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
		
		
		// 3 way handshake
		private void setGroupFileProp(int numOfFiles) throws IOException {
            dataOutputStream.writeInt(NUMBER_OF_FILES);
            dataOutputStream.flush();
		}
		
		
		private void onSend(File file,  WorkMonitor workMonitor) throws IOException {

			NUMBER_OF_FILES = dataInputStream.readInt();

			try (FileInputStream fileInputStream = new FileInputStream(file)){
	        	this.fileInputStream = fileInputStream;
	        	
	            String fileName = file.getName();

	            dataOutputStream.writeUTF(fileName);
	            dataOutputStream.flush();

	            boolean goSignal = dataInputStream.readBoolean();

	            if(goSignal){
	                writeFile(workMonitor, fileInputStream);
	            }
	        }
	    }
		
		
		
		// send files by bytes
		void writeFile(WorkMonitor workMonitor, FileInputStream fileInputStream) throws IOException {
			
			long transferred = 0;
	        byte[] buffer = new byte[GenericUtils.DEFAULT_BUFFER_SIZE];
	        int read;
	        while ((read = fileInputStream.read(buffer, 0, GenericUtils.DEFAULT_BUFFER_SIZE)) > 0) {
	        	bufferedOutputStream.write(buffer, 0, read);
	            transferred += read;
	            workMonitor.setWorkDone(transferred);
	        }
	        bufferedOutputStream.flush();
			
			
//			try(FileOutputStream output = new FileOutputStream(toOutputfile);
//					FileInputStream input = new FileInputStream(file)){
//				long transferred = 0;
//		        byte[] buffer = new byte[GenericUtils.DEFAULT_BUFFER_SIZE];
//		        int read;
//		        while ((read = input.read(buffer, 0, GenericUtils.DEFAULT_BUFFER_SIZE)) >= 0) {
//		        	output.write(buffer, 0, read);
//		            transferred += read;
//		            workMonitor.setWorkDone(transferred);
//		        }
//		        
//			} catch(IOException ioex) {
//				throw new IOException(ioex);
//			}
		}
		
		
		
		
		
		void closeStream() {
			new Thread( () -> {
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
			}).start();
		}
		
	}
}

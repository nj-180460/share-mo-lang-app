package org.sharemolangapp.smlapp.receiver;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.Properties;

import org.sharemolangapp.smlapp.util.GenericUtils;
import org.sharemolangapp.smlapp.util.NetworkUtility;



class ReceiverService {

	private static int NUMBER_OF_FILES = 0;
	
	private final ReceiverNetwork receiverNetwork;
	private final Properties serverProp;
	private final ReceiveOnClientHandler receiveOnClientHandler;
	
	
	ReceiverService(){
		receiverNetwork = new ReceiverNetwork();
		serverProp = new Properties();
		receiveOnClientHandler = new ReceiveOnClientHandler();
	}
	
	
	void setUpServerProperties() throws ReceiverNetworkException {
		
		String host = NetworkUtility.getMachineIPv4Address().getHostAddress();
		int port = NetworkUtility.freePort();
		
		if((host == null || host.isBlank()) && (port <= 0)) {
			throw new ReceiverNetworkException("IP Address and Port must be present.");
		}
		
		serverProp.put("host", host);
		serverProp.put("port", port);
		
	}
	
	
	Properties getServerProperties() {
		return serverProp;
	}
	
	
	boolean startServer() throws NumberFormatException, UnknownHostException, IOException {
		return receiverNetwork.startServer(Integer.parseInt(serverProp.get("port").toString()));
	}
	
	void startWaitingForClient() {
		receiverNetwork.waitingForClient(receiveOnClientHandler);
	}
	
	
	void closeAll() {
		receiverNetwork.closeConnection();
	}
	
	void closeClientConnection() {
		receiverNetwork.closeClientConnection();
	}
	
	
//	void sendFileTo(File file, WorkMonitor workMonitor) throws IOException {
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
//		
//		
//		try(FileOutputStream output = new FileOutputStream(toOutputfile);
//				FileInputStream input = new FileInputStream(file)){
//			long transferred = 0;
//	        byte[] buffer = new byte[GenericUtils.DEFAULT_BUFFER_SIZE];
//	        int read;
//	        while ((read = input.read(buffer, 0, GenericUtils.DEFAULT_BUFFER_SIZE)) >= 0) {
//	        	output.write(buffer, 0, read);
//	            transferred += read;
//	            workMonitor.setWorkDone(transferred);
//	        }
//	        
//		} catch(IOException ioex) {
//			throw new IOException(ioex);
//		}
//	}
	
	
	
	public void openFileExplorerLocation(File file) {
		
		if(GenericUtils.IS_WINDOWS) {
			try {
				Runtime.getRuntime().exec("explorer /select, "+file.getAbsolutePath()); // for windows os
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	
	
	
	
	class ReceiveOnClientHandler {
		
		private BufferedInputStream bufferedInputStream;
		private DataInputStream dataInputStream;
		private DataOutputStream dataOutputStream;
		
		
		
		void setClientStreams(InputStream inputStream, OutputStream outputStream) {
			bufferedInputStream = new BufferedInputStream(inputStream);
			dataInputStream = new DataInputStream(inputStream);
            dataOutputStream = new DataOutputStream(outputStream);
		}
		
		
		
		// 3 way handshake
		void onRead() throws IOException {
			
			while((NUMBER_OF_FILES = dataInputStream.readInt()) >= 0) {
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
			System.out.println("terminates server waiting.");
			
		}
		
		
		
		
		void onWaitFile() throws IOException {
			String fileName = dataInputStream.readUTF();

	        boolean validFileName = (fileName != null && !fileName.isBlank());
	        dataOutputStream.writeBoolean(validFileName);
	        dataOutputStream.flush();

	        if(validFileName){
	            readFile(fileName, bufferedInputStream);
	        }
	    }
		
		
		
		
		private void readFile(String fileName, InputStream inputStream){

			StringBuilder receivingFolder = new StringBuilder();
			receivingFolder.append(System.getProperty("user.home"));
			receivingFolder.append(File.separator);
			receivingFolder.append("Desktop");
			receivingFolder.append(File.separator);
			receivingFolder.append("received");
			receivingFolder.append(File.separator);
			receivingFolder.append(fileName);
			
	        File receivingFile = new File(receivingFolder.toString());
	        byte[] bytes = new byte[GenericUtils.DEFAULT_BUFFER_SIZE];

	        try(FileOutputStream outputStream = new FileOutputStream(receivingFile)){

	            int count;
	            while ((count = inputStream.read(bytes)) >= 0) {
	                outputStream.write(bytes, 0, count);
	                if(count < bytes.length){
	                    break;
	                }
	            }
	            outputStream.flush();

	        }catch(IOException io){
	            io.printStackTrace();
	        }
	        
	    }
		
		
		
		
		
		
//		void write() throws IOException {
//			
//			StringBuilder receivingFolder = new StringBuilder();
//			receivingFolder.append(System.getProperty("user.home"));
//			receivingFolder.append(File.separator);
//			receivingFolder.append("Desktop");
//			receivingFolder.append(File.separator);
//			receivingFolder.append("received");
//			receivingFolder.append(File.separator);
//			
//			System.out.println("waiting for file properties...");
//			
//			
////			byte[] bufferedFileProperty = new byte[GenericUtils.DEFAULT_BUFFER_SIZE];
////			int readFileProperty = bufferedInput.read();
//			String fileProperty = dataInputStream.readUTF();
////			byte[] bufferedFileProperty = new byte[readFileProperty];
//			
//			System.out.println("receiver byte size: " + fileProperty.length());
////			fileProperty = new String(bufferedFileProperty);
//			System.out.println("receiver string size: " + fileProperty.length());
//			
//			String[] filePropSliced = fileProperty.split(":");
//			
//			String fileName = filePropSliced[0];
//			System.out.println("file name: "+fileName);
//			
//			System.out.println(fileProperty);
//			System.out.println(filePropSliced[0]);
//			System.out.println(filePropSliced[1]);
//			
//			long fileSize = 0;//Long.parseLong(filePropSliced[1]);
//			System.out.println("file size: "+(fileSize/1024)/1024);
////			
////			if(fileSize == 0) {
////				return;
////			}
////			
//			receivingFolder.append(fileName);
//			
//			File toOutputfile = new File(receivingFolder.toString());
//			
//			try(FileOutputStream output = new FileOutputStream(toOutputfile)){
//				
//				byte[] buffer = new byte[GenericUtils.DEFAULT_BUFFER_SIZE];
//				long transferred = 0;
//		        int read;
//		        while ((fileSize > 0) && ((read = bufferedInputStream.read(buffer)) >= 0)) {
//		        	output.write(buffer, 0, read);
//		            transferred += read;
//		            fileSize -= read; // read upto file size -  EOF - End Of File
//		        }
//		        
//		        output.flush();
//		        
//			} catch(IOException ioex) {
//				throw new IOException(ioex);
//			}
//		} 
		
		
		
//		void writeToLocal() throws IOException {
//			String fileName = "(file)";
//			StringBuilder receivingFolder = new StringBuilder();
//			receivingFolder.append(System.getProperty("user.home"));
//			receivingFolder.append(File.separator);
//			receivingFolder.append("Desktop");
//			receivingFolder.append(File.separator);
//			receivingFolder.append("received");
//			receivingFolder.append(File.separator);
//			receivingFolder.append(fileName+".png");
//			
//			File toOutputfile = new File(receivingFolder.toString());
//			
//			try(FileOutputStream output = new FileOutputStream(toOutputfile);
//					BufferedInputStream input = new BufferedInputStream(inputStream, GenericUtils.DEFAULT_BUFFER_SIZE)){
//				
//				
//				
//				
//				
//				long transferred = 0;
//		        byte[] buffer = new byte[GenericUtils.DEFAULT_BUFFER_SIZE];
//		        int read;
//		        while ((read = input.read(buffer)) >= 0) {
//		        	output.write(buffer, 0, read);
//		            transferred += read;
//		        }
//		        
//			} catch(IOException ioex) {
//				throw new IOException(ioex);
//			}
//		} 
		
	}
}

package org.sharemolangapp.smlapp.sender;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import org.sharemolangapp.smlapp.sender.SenderService.SendOnServerHandler;




class SenderNetwork {

	private final SenderService senderService;
	private final SendOnServerHandler sendOnServerHandler;
	private Socket socket;
	
	
	SenderNetwork(SenderService senderService, SendOnServerHandler sendOnServerHandler){
		this.senderService = senderService;
		this.sendOnServerHandler = sendOnServerHandler;
	}
	
	
	boolean connectNow() throws NumberFormatException, UnknownHostException, IOException {
		Properties serverProperties = senderService.getServerProperties();
		String host = serverProperties.get("host").toString().strip();
		int port = Integer.parseInt(String.valueOf(serverProperties.get("port")).strip());
		boolean isConnected = false;
		
		socket = new Socket(host, port);
		
		isConnected = socket.isConnected();
		if(isConnected) {
			sendOnServerHandler.setStreams(socket.getInputStream(), socket.getOutputStream());
		}
		
		return isConnected;
	}
	
	
//	void writeToSend(File file) throws FileNotFoundException, IOException {
//		int read = 0;
//		try(FileInputStream fileInputStream = new FileInputStream(file);
//				BufferedOutputStream bufferedOutpuStream = new BufferedOutputStream(socket.getOutputStream())){
//			
//			byte[] buffer = new byte[GenericUtils.DEFAULT_BUFFER_SIZE];
//			
//			while((read = fileInputStream.read(buffer)) >= 0) {
//				bufferedOutpuStream.write(buffer, 0, read);
//			}
//			
//			bufferedOutpuStream.flush();
//		}
//		
//	}
	
	
	boolean testConnection() throws NumberFormatException, UnknownHostException, IOException {
		boolean isConnected = connectNow();
		socket.close();
		return isConnected;
	}
	
	
	
//	// *** TEMPORARY LA INI
//	void tmpServer() {
//		new Thread( () -> {
//			int port = NetworkUtility.freePort();
//			System.out.println(port);
//			ServerSocket ss = null;
//			try {
//				ss = new ServerSocket(port);
//			} catch (IOException e) {
//				e.printStackTrace();
//			} finally {
//				try {
//					if(ss != null) {
//						Socket client = ss.accept();
//						System.out.println("Connected: "+client.isConnected());
//						System.out.println(client.getInetAddress());
//						ss.close();
//					}
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				
//			}
//		}).start();
//	}
	
	
	
	void closeClientConnection() {
		try {
			if(socket != null) {
				socket.close();
			}
			sendOnServerHandler.closeStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

package org.sharemolangapp.smlapp.sender;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.sharemolangapp.smlapp.sender.SenderService.SendOnServerHandler;
import org.sharemolangapp.smlapp.util.ConfigConstant;
import org.sharemolangapp.smlapp.util.GenericUtils;




class SenderNetwork {

	private final SenderService senderService;
	private final SendOnServerHandler sendOnServerHandler;
	private Future<Boolean> rqtServerAccpFuture;
	private Socket socket;
	
	
	SenderNetwork(SenderService senderService, SendOnServerHandler sendOnServerHandler){
		this.senderService = senderService;
		this.sendOnServerHandler = sendOnServerHandler;
	}
	
	
	Boolean connectNow(boolean isTest) throws NumberFormatException, UnknownHostException, IOException, InterruptedException, ExecutionException {
		Properties serverProperties = senderService.getServerProperties();
		String host = serverProperties.get("host").toString().strip();
		int port = Integer.parseInt(String.valueOf(serverProperties.get("port")).strip());
		Boolean isConnected = false;
		
		socket = new Socket();
		socket.connect(new InetSocketAddress(host, port), GenericUtils.CONNECTION_SO_TIMEOUT);
		socket.setSoTimeout(GenericUtils.READ_SO_TIMEOUT * 4 + GenericUtils.READ_SO_TIMEOUT); // 2 minutes and 30 seconds
		
		isConnected = !socket.isClosed();
		
		if(isConnected) {
			if(!isTest) { 
				isConnected = requestServerAcceptance(socket);
			}
//			monitorServerConnection();
//			sendOnServerHandler.setStreams(socket.getInputStream(), socket.getOutputStream());
		}
		
		return isConnected;
	}
	
	
	private Boolean requestServerAcceptance(final Socket socket) throws InterruptedException, ExecutionException {
		
		ExecutorService rqtServerAccpExecutor = Executors.newSingleThreadExecutor();
		rqtServerAccpFuture = rqtServerAccpExecutor.submit( new Callable<Boolean>() {
			
			@Override
			public Boolean call() throws Exception {
				
				Boolean isAccepted = false;
				
				try {
					
					DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
					dataOutputStream.writeUTF(ConfigConstant.CLIENT_REQUESTING_RESPONSE);
					dataOutputStream.flush();
					
					DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
					String serverResponse = dataInputStream.readUTF();
					
					if(serverResponse.equals(ConfigConstant.OK_RESPONSE)) {
						senderService.setServerAccepted(true);
						sendOnServerHandler.setStreams(socket.getInputStream(), socket.getOutputStream());
					} else {
						senderService.setServerAccepted(false);
						socket.close();
						sendOnServerHandler.closeStream();
					}
					
					isAccepted = senderService.isServerAccepted();
					
				} catch (IOException e) {
					senderService.setServerAccepted(false);
					e.printStackTrace();
					isAccepted = null;
				}
				
				return isAccepted;
			}
			
		});
		
		rqtServerAccpExecutor.shutdown();
		
		Boolean isAccepted = rqtServerAccpFuture.get();
		
		return isAccepted;
	}
	
	
	boolean isActive() {
		return !socket.isClosed();
	}
	
	
	boolean testConnection() throws NumberFormatException, UnknownHostException, IOException, InterruptedException, ExecutionException {
		boolean isConnected = connectNow(true);
		socket.close();
		return isConnected;
	}
	
	
	synchronized void closeClientConnection() {
		try {
			if(rqtServerAccpFuture != null) {
				rqtServerAccpFuture.cancel(true);
			}
			if(socket != null) {
				socket.close();
			}
			sendOnServerHandler.closeStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

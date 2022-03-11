package org.sharemolangapp.smlapp.receiver;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.sharemolangapp.smlapp.receiver.ReceiverService.ReceiveOnClientHandler;



class ReceiverNetwork {
	
	private static boolean SERVER_RUNNING = false;
	private static boolean CLIENT_CONNECTION_ESTABLISHED = false;
	
	private ServerSocket serverSocket;
	private ClientHandler clientHandler;
	
	ReceiverNetwork(){
		
	}
	
	
	boolean startServer(int port) {
		return initializeServer(port);
	}
	
	
	
	private boolean initializeServer(int port) {
		
		boolean hasInitialize = false;
		
		ExecutorService execService = Executors.newSingleThreadExecutor();
		Future<Boolean> serverFuture = execService.submit( () -> {
			
			boolean serverStarted = false;
			
			try {
				serverSocket = new ServerSocket(port);
//				serverSocket.setSoTimeout((int)Duration.ofSeconds(30).toMillis());
				serverStarted = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return serverStarted;
		});
		
		try {
			
			hasInitialize = serverFuture.get();
			SERVER_RUNNING = hasInitialize;
			
			execService.shutdown();
			execService.shutdownNow();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return hasInitialize;
	}
	
	
	
	void waitingForClient(ReceiveOnClientHandler receiveOnClientHandler) {
		
		if(!CLIENT_CONNECTION_ESTABLISHED) {
			ExecutorService execService = Executors.newSingleThreadExecutor();
			execService.execute( () -> {
				try {
					Socket clientSocket = serverSocket.accept();
					
					clientHandler = new ClientHandler(clientSocket);
					CLIENT_CONNECTION_ESTABLISHED = true;
					
					clientHandler.setReceiveOnClientHandler(receiveOnClientHandler);
					
					CLIENT_CONNECTION_ESTABLISHED = false;
					
				} catch (IOException e) {
					e.printStackTrace();
					CLIENT_CONNECTION_ESTABLISHED = false;
					SERVER_RUNNING = false;
				}
				
			});
			execService.shutdown();
		}
		
//		if(clientHandler != null) {
//			if(!clientHandler.isActive()) {
//				CLIENT_CONNECTION_ESTABLISHED = false;
//			}
//		}
	}
	
	
	boolean isConnected() {
		return (clientHandler != null ? clientHandler.isConnected() : false);
	}
	
	
	
	void closeConnection() {
		try {
			
			closeClientConnection();
			
			if(serverSocket != null) {
				serverSocket.close();
			}
			
			SERVER_RUNNING = false;
			
		} catch (IOException e) {
			e.printStackTrace();
			CLIENT_CONNECTION_ESTABLISHED = false;
			SERVER_RUNNING = false;
		}
	}

	
	void closeClientConnection() {
		if(clientHandler != null) {
			clientHandler.closeClientConnection();
		}
	}
	
	
	
	class ClientHandler{
		
		private final Socket clientSocket;
		
		private ReceiveOnClientHandler receiveOnClientHandler;
		
		
		ClientHandler(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}
		
		
		void setReceiveOnClientHandler(ReceiveOnClientHandler receiveOnClientHandler) throws IOException {
			this.receiveOnClientHandler = receiveOnClientHandler;
			this.receiveOnClientHandler.setClientStreams(clientSocket.getInputStream(), clientSocket.getOutputStream());
			this.receiveOnClientHandler.onRead(); // this should be in loop
		}
		
		
		boolean isActive() {
			return clientSocket != null ? clientSocket.isClosed() : false;
		}
		
		
		boolean isConnected() {
			return clientSocket != null ? clientSocket.isConnected() : false;
		}
		
		
		void closeClientConnection() {
			try {
				
				if(clientSocket != null) {
					clientSocket.close();
				}
				
				CLIENT_CONNECTION_ESTABLISHED = false;
				
			} catch (IOException e) {
				e.printStackTrace();
				CLIENT_CONNECTION_ESTABLISHED = false;
			}
		}
		
	}
}

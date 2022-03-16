package org.sharemolangapp.smlapp.receiver;


import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.sharemolangapp.smlapp.receiver.ReceiverService.ReceiveOnClientHandler;
import org.sharemolangapp.smlapp.util.ConfigConstant;



class ReceiverNetwork {
	
	private static boolean SERVER_RUNNING = false;
	private static boolean CLIENT_CONNECTION_ESTABLISHED = false;
	
	private final ReceiverService receiverService;
	
	private ServerSocket serverSocket;
	private ClientHandler clientHandler;
	private ExecutorService clientRequestExec;
	private ExecutorService clientMonitoringExec;
	
	
	
	ReceiverNetwork(ReceiverService receiverService){
		this.receiverService = receiverService;
	}
	
	
	boolean startServer() {
		String portstr = String.valueOf(receiverService.getServerProperties().get("port"));
		if(portstr != null && !portstr.equals("null")) {
			return initializeServer(Integer.parseInt(portstr));
		}
		return false;
	}
	
	
	
	private boolean initializeServer(int port) {
		
		boolean hasInitialize = false;
		
		ExecutorService execService = Executors.newSingleThreadExecutor();
		Future<Boolean> serverFuture = execService.submit( () -> {
			
			boolean serverStarted = false;
			
			try {
				serverSocket = new ServerSocket(port);
//				serverSocket.setSoTimeout(GenericUtils.CONNECTION_SO_TIMEOUT);
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
			clientRequestExec = Executors.newSingleThreadExecutor();
			clientRequestExec.execute( () -> {
				
				do {
					
					try (Socket clientSocket = serverSocket.accept()){
						
						clientHandler = new ClientHandler(clientSocket);
						DataInputStream dataInputStream = clientHandler.getDataInputStream();
						DataOutputStream dataOutputStream = clientHandler.getDataOutputStream();
						
						String clientRequestFirst = dataInputStream.readUTF();
						String []deconsClientRequestFirst = clientRequestFirst.split(":");
						String clientRequest = deconsClientRequestFirst[0];
						String clientName = deconsClientRequestFirst[1];
						
						receiverService.getClientProperties().put("host", clientSocket.getInetAddress().getHostAddress());
						receiverService.getClientProperties().put("clientName", clientName);
						
						receiverService.setRequest(clientRequest);
						
						StringBuilder responseInfo = new StringBuilder();
						
						if(receiverService.serverConfirmation()) {
							
							responseInfo.append(receiverService.getResponse());
							responseInfo.append(":");
							responseInfo.append(receiverService.getServerProperties().get("serverName").toString());
							
							dataOutputStream.writeUTF(responseInfo.toString());
							dataOutputStream.flush();
							
							CLIENT_CONNECTION_ESTABLISHED = true;
							monitorClientStatus(clientHandler);
							clientHandler.setReceiveOnClientHandler(receiveOnClientHandler);
							
						} else {
							
							responseInfo.append(receiverService.getResponse());
							responseInfo.append(":");
							responseInfo.append(receiverService.getServerProperties().get("serverName").toString());
							
							dataOutputStream.writeUTF(responseInfo.toString());
							dataOutputStream.flush();
							
						}
						
//						clientHandler = new ClientHandler(clientSocket);
//						CLIENT_CONNECTION_ESTABLISHED = true;
//						monitorClientStatus(clientHandler);
//						clientHandler.setReceiveOnClientHandler(receiveOnClientHandler);
						
						receiverService.setRequest(ConfigConstant.NONE_RESPONSE);
						CLIENT_CONNECTION_ESTABLISHED = false;
						receiverService.getClientProperties().clear();
						System.out.println("client ended");
						
					} catch (IOException e) {
						e.printStackTrace();
						CLIENT_CONNECTION_ESTABLISHED = false;
					}
				
				} while(SERVER_RUNNING);
				
			});
			
			clientRequestExec.shutdown();
			
		}
		
	}
	
	
	private synchronized void monitorClientStatus(ClientHandler clientHandler) {
		
		clientMonitoringExec = Executors.newSingleThreadExecutor();
		clientMonitoringExec.execute( () -> {
			
			while(clientHandler.isActive()) {
				
				if(!CLIENT_CONNECTION_ESTABLISHED || !SERVER_RUNNING) {
					break;
				}
				
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
								
			}
			
			receiverService.setRequest(ConfigConstant.NONE_RESPONSE);
			
			clientHandler.closeClientConnection();
			
		});
		
		clientMonitoringExec.shutdown();
	}
	
	
	boolean isConnected() {
		return (clientHandler != null ? clientHandler.isActive() : false);
	}
	
	
	
	synchronized void closeConnection() {
		try {
			
			if(serverSocket != null) {
				serverSocket.close();
			}
			
			receiverService.setRequest(ConfigConstant.NONE_RESPONSE);
			SERVER_RUNNING = false;
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} finally {
			
			closeClientConnection();
			if(clientRequestExec != null) {
				clientRequestExec.shutdownNow();
			}
			
			if(clientMonitoringExec != null) {
				clientMonitoringExec.shutdownNow();
			}
			
			receiverService.setRequest(ConfigConstant.NONE_RESPONSE);
			CLIENT_CONNECTION_ESTABLISHED = false;
			SERVER_RUNNING = false;
			
		}
		
	}

	
	synchronized void closeClientConnection() {
		
		if(clientHandler != null) {
			clientHandler.closeClientConnection();
			CLIENT_CONNECTION_ESTABLISHED = false;
		}
		
		if(clientRequestExec != null) {
			clientRequestExec.shutdownNow();
		}
	}
	
	
	
	class ClientHandler{
		
		private final Socket clientSocket;
		
		private final DataInputStream dataInputStream;
		private final DataOutputStream dataOutputStream;
		private final BufferedInputStream bufferedInputStream;
		private ReceiveOnClientHandler receiveOnClientHandler;
		
		
		ClientHandler(Socket clientSocket) throws IOException {
			this.clientSocket = clientSocket;
			this.dataInputStream = new DataInputStream(clientSocket.getInputStream());
			this.dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
			this.bufferedInputStream = new BufferedInputStream(clientSocket.getInputStream());
		}
		
		
		void setReceiveOnClientHandler(ReceiveOnClientHandler receiveOnClientHandler) throws IOException {
			this.receiveOnClientHandler = receiveOnClientHandler;
			this.receiveOnClientHandler.setClientStreams(dataInputStream, dataOutputStream, bufferedInputStream);
			this.receiveOnClientHandler.onRead();
		}
		
		
		ReceiveOnClientHandler getReceiveOnClientHandler() {
			return this.receiveOnClientHandler;
		}
		
		
		boolean isActive() {
			return !this.clientSocket.isClosed();
		}
		
		
		DataInputStream getDataInputStream() {
			return this.dataInputStream;
		}
		
		DataOutputStream getDataOutputStream() {
			return this.dataOutputStream;
		}
		
		BufferedInputStream getBufferedInputStream() {
			return this.bufferedInputStream;
		}
		
		synchronized void closeClientConnection() {
			try {
				
				if(this.clientSocket != null) {
					this.clientSocket.close();
				}
				
				CLIENT_CONNECTION_ESTABLISHED = false;
				
			} catch (IOException e) {
				e.printStackTrace();
				CLIENT_CONNECTION_ESTABLISHED = false;
			}
		}
		
	}
}

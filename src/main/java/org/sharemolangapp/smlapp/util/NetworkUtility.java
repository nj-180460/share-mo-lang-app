package org.sharemolangapp.smlapp.util;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Random;



public abstract class NetworkUtility {
	
	private static final int EPHEMERAL_PORT_RANGE_LOW = 49152;
	private static final int EPHEMERAL_PORT_RANGE_HIGH = 65535;
	
	
	private NetworkUtility() {
		throw new UnsupportedOperationException(getClass().getName() +" is not available. Use static methods only");
	}

	
	public static Inet4Address getMachineIPv4Address(){
		InetAddress inetAdd = null;
		Inet4Address ipAddr = null;
		try {
			inetAdd = InetAddress.getLocalHost();
			String hostName = inetAdd.getHostName();
			ipAddr = (Inet4Address)Inet4Address.getByName(hostName);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return ipAddr;
    }
	
	
	public static int freePort() {
		int terminator = 0;
		Random rand = new Random();
		rand.setSeed(System.currentTimeMillis());
		
		do{
			int randPortPicked = EPHEMERAL_PORT_RANGE_LOW + rand.nextInt(EPHEMERAL_PORT_RANGE_HIGH - EPHEMERAL_PORT_RANGE_LOW);
			try (ServerSocket serverSocket = new ServerSocket(randPortPicked)) {
		        Optional<ServerSocket> availablePorts = Optional.of(serverSocket);
		        if(availablePorts.isPresent() && (serverSocket.getLocalPort() == randPortPicked)) {
		        	return randPortPicked;
		        }
		    } catch (IOException e) {
		        System.err.println("Port already in used: " + e.getMessage());
		    }
			terminator++;
		} while(terminator >= 1000);
		
		return 0;
	}
	
	
	public static ArrayList<String> getMachineNetworkInterfaces(){
//    	return scanMachineNetworkInterfaces();
    	return getMachineLocalAddress();
    }
	
	
	private static ArrayList<String> getMachineLocalAddress() {
    	ArrayList<String> ipAddress = new ArrayList<>();
    	String localAddress = null;
    	try {
    		InetAddress inetAdd = InetAddress.getLocalHost();
    		String hostName = inetAdd.getHostName();
    		Inet4Address ipAddr = (Inet4Address)Inet4Address.getByName(hostName);
			localAddress = ipAddr.getHostAddress();
			ipAddress.add(localAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    	return ipAddress;
    }
	
	
    private static ArrayList<String> scanMachineNetworkInterfaces(){
    	ArrayList<String> ipAddressesList = new ArrayList<>();
    	Enumeration<NetworkInterface> inetEnum = null;
    	try{
    		inetEnum = NetworkInterface.getNetworkInterfaces();
    		while(inetEnum.hasMoreElements()) {
        		NetworkInterface networkInterface = (NetworkInterface) inetEnum.nextElement();
        		if (networkInterface.isLoopback() || !networkInterface.isUp()) {
        			continue;
        		}
        		List<InterfaceAddress> iaList = networkInterface.getInterfaceAddresses();
        		iaList.forEach( ni -> {
        			InetAddress ipAddr = ni.getAddress();
        			String ipAddressStr = ipAddr.getHostAddress();
        			if(ipAddressStr != null) { 
        				if(!(ipAddr.isLoopbackAddress() || ipAddr.isLinkLocalAddress() 
        						|| ipAddr.isMulticastAddress() || !(ipAddr instanceof Inet4Address))){
        					ipAddressesList.add(ipAddr.getHostAddress());
        				}
        			}
        		});
        	}
    	}catch(SocketException se){
            se.printStackTrace();
        }    	
    	return ipAddressesList;
    }    
}

package ar;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.rmi.UnexpectedException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ar.elements.User;
import ar.sessions.AdminSession;
import ar.sessions.ClientSession;
import ar.sessions.Session;
import ar.sessions.utils.IpAndMask;

public class POPXY {

	private final static String defaultOriginServer = "pop3.alu.itba.edu.ar";
	private final static int defaultWelcomeSocketPort = 1110;
	private final static int defaultAdminPort = 12345;
	private final static int defaultOriginPort = 110;
	private final static int defaultStatsPort = 10101;
	
	private static int welcomeSocketPort = defaultWelcomeSocketPort;
	private static int adminPort = defaultAdminPort; 
	private static int originPort = defaultOriginPort;
	private static int statsPort = defaultStatsPort;  
	
	private static Logger logger = Logger.getLogger(POPXY.class.getName());
	
	private static POPXY instance = null;
	
	private static ExecutorService threadPool = Executors.newFixedThreadPool(5);
	
	
	private Map<String, User> users = new HashMap<String, User>();
	private Set<IpAndMask> blackIps = new HashSet<IpAndMask>();
	
	private static Thread clientsThread;
	private static Thread adminsThread;

	public static void main(String[] args) 
		throws Exception{
		
		//Seteo las configuraciones globales del proxy
		User.initGlobalConfiguration();
		
		//Tomar conf;
		POPXY proxy = POPXY.getInstance();
		try {
			PropertyConfigurator.configure("resources/log4j.properties");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error loading logger");
			//return;
		}
		ServerSocketChannel welcomeSocket = ServerSocketChannel.open();
		ServerSocketChannel adminSocket = ServerSocketChannel.open();
		try{
			
			welcomeSocket.socket().bind(new InetSocketAddress(welcomeSocketPort));
			welcomeSocket.configureBlocking(true);
			
			clientsThread = new ClientWelcomeSocket(threadPool, welcomeSocket);
					
			adminSocket.socket().bind(new InetSocketAddress(adminPort));
			adminSocket.configureBlocking(false);
			
			adminsThread = new AdminWelcomeSocket(adminSocket);
			
			threadPool.execute(clientsThread);
			threadPool.execute(adminsThread);
		}
		catch(NotYetBoundException nybe){
			//TODO:
		}
		catch(IOException ioe){
			//TODO:
		}
		catch(Exception e){
			//TODO:
		}
		
		do {
			clientsThread.join(60000);
			adminsThread.join(60000);
		}while(clientsThread.isAlive() || adminsThread.isAlive());
	}

	public static POPXY getInstance() {
		if(instance == null) {
			instance = new POPXY();
		}
		return instance;
	}
	
	public User getUser(String userName) {
		User user = users.get(userName);
		if(user == null){
			user = new User(userName);
			users.put(userName, user);
		}
		return user;
	}

	public boolean userIsBlocked(String username) {
		User user = this.getUser(username);
		if(user == null){
			return false;
		}else{
			return user.isBlocked();
		}
	}

	public String getDefaultOriginServer() {
		return POPXY.defaultOriginServer;
	}

	public int getDefaultOriginServerPort() {
		return POPXY.defaultOriginPort;
	}
	
	public void setWellcomePort(Integer port) {
		// TODO Auto-generated method stub
		
	}

	public void setAdminPort(Integer port) {
		// TODO Auto-generated method stub
		
	}

	public void setOriginPort(Integer port) {
		// TODO Auto-generated method stub
		
	}

	public void setStatsPort(Integer port) {
		// TODO Auto-generated method stub
		
	}

	public  int getAdminPort() {
		return adminPort;
	}

	public void setAdminPort(int adminPort) {
		POPXY.adminPort = adminPort;
	}

	public int getWelcomeSocketPort() {
		return welcomeSocketPort;
	}

	public void setWelcomeSocketPort(int welcomeSocketPort) {
		POPXY.welcomeSocketPort = welcomeSocketPort;
	}

	public int getStatsPort() {
		return statsPort;
	}

	public static void setStatsPort(int statsPort) {
		POPXY.statsPort = statsPort;
	}

	public void activateApp(String string) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean isOnTheBlackList(String ip) {
		for(IpAndMask net: this.blackIps){
			if(net.matchNet(ip)){
				return true;
			}
		}
		
		return false;
	}

	public void addIpToBlackList(String ip, String mask) {
		IpAndMask newOne = new IpAndMask(ip, mask);
		blackIps.add(newOne);		
	}

	public void deleteIpFromBlackList(String ip, String mask) {
		IpAndMask newOne = new IpAndMask(ip, mask);
		blackIps.remove(newOne);
	}
	
	public static Logger getLogger() {
		return logger;
	}
	
}
package ar;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.ServerSocketChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ar.elements.User;
import ar.sessions.utils.IpAndMask;

public class POPXY {

	private static int welcomeSocketPort;
	private static int adminPort; 
	private static int statsPort;  
	
	private static Logger logger = Logger.getLogger(POPXY.class.getName());
	
	private static POPXY instance = null;
	
	private static ExecutorService threadPool = Executors.newFixedThreadPool(5);
	
	
	private Map<String, User> users = new HashMap<String, User>();
	private Set<IpAndMask> blackIps = new HashSet<IpAndMask>();
	
	private static Thread clientsThread;
	private static Thread adminsThread;
	private static Thread statsThread;
	
	private static ServerSocketChannel welcomeSocket;
	private static ServerSocketChannel adminSocket;
	private static ServerSocketChannel statsSocket;

	public static void main(String[] args) 
		throws Exception{
		
		//Seteo los puertos en los que escucho nuevas conexiones.
		initPorts();
		
		//Seteo las configuraciones globales de los usuarios
		User.initGlobalConfiguration();
		
		//Tomar conf;
		try {
			PropertyConfigurator.configure("resources/log4j.properties");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error loading logger");
			//return;
		}
		welcomeSocket = ServerSocketChannel.open();
		adminSocket = ServerSocketChannel.open();
		statsSocket = ServerSocketChannel.open();
		try{
			
			welcomeSocket.socket().bind(new InetSocketAddress(welcomeSocketPort));
			welcomeSocket.configureBlocking(true);
			
			clientsThread = new ClientWelcomeSocket(threadPool, welcomeSocket);
					
			adminSocket.socket().bind(new InetSocketAddress(adminPort));
			adminSocket.configureBlocking(false);
			

			statsSocket.socket().bind(new InetSocketAddress(statsPort));
			statsSocket.configureBlocking(false);
			
			adminsThread = new AdminWelcomeSocket(adminSocket);
			statsThread = new StatsWelcomeSocket(statsSocket);
			
			threadPool.execute(clientsThread);
			threadPool.execute(adminsThread);
			threadPool.execute(statsThread);
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
		
//		do {
//			clientsThread.join(60000);
//			adminsThread.join(60000);
//		}while(clientsThread.isAlive() || adminsThread.isAlive());
	}

	public  int getAdminPort() {
		return adminPort;
	}
	
	public int getWelcomeSocketPort() {
		return welcomeSocketPort;
	}
	
	public int getStatsPort() {
		return statsPort;
	}
	
	public static void initPorts() {
		//Levanto del archivo properties con la conf de los puertos.
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("resources/popxy.properties"));
			welcomeSocketPort = Integer.parseInt(properties.getProperty("WelcomeSocketPort"));
			adminPort = Integer.parseInt(properties.getProperty("AdminPort"));
			statsPort = Integer.parseInt(properties.getProperty("StatsPort"));	
		} catch (Exception e) {
			System.out.println("No se pudo leer archivo de configuracion del proxy\n");
			welcomeSocketPort = 1110;
			adminPort = 12345;
			statsPort = 10101;	
		}
}

	
	public static void changeWelcomePort(int welcomeSocketPort) {
		POPXY.welcomeSocketPort = welcomeSocketPort;
		((ClientWelcomeSocket) clientsThread).changePort();		
	}
	
	public static void resetWelcomeSocket(){
		try {
			welcomeSocket = ServerSocketChannel.open();
			welcomeSocket.socket().bind(new InetSocketAddress(welcomeSocketPort));
			welcomeSocket.configureBlocking(true);
			clientsThread = new ClientWelcomeSocket(threadPool, welcomeSocket);
			threadPool.execute(clientsThread);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void changeAdminPort(int adminPort) {
	//	POPXY.adminPort = adminPort;
	}
		
	public static void changeStatsPort(int statsPort) {
	//	POPXY.statsPort = statsPort;
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
	
	public boolean existingUser(String userName) {
		return users.containsKey(userName);
	}

	public boolean userIsBlocked(String username) {
		User user = this.getUser(username);
		if(user == null){
			return false;
		}else{
			return user.isBlocked();
		}
	}
	
	
	public boolean isOnTheBlackList(String ip) {
		for(IpAndMask net: this.blackIps){
			if(net.matchNet(ip)){
				return true;
			}
		}
		
		return false;
	}
	
	public Set<IpAndMask> getBlackIps(){
		return this.blackIps;
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

	public void resetUsers() {
		this.users.clear();		
	}

	public Set<String> getUsernames() {
		return this.users.keySet();
	}

	public Collection<User> getUsers() {
		return this.users.values();
	}

	
}

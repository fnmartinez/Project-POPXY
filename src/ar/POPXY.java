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
	
	
	private Map<String, User> users = new HashMap<String, User>();
	private Set<IpAndMask> blackIps = new HashSet<IpAndMask>();
	
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
		Selector selector = Selector.open();
		ServerSocketChannel welcomeSocket = ServerSocketChannel.open();
		ServerSocketChannel adminSocket = ServerSocketChannel.open();
		try{
			
			welcomeSocket.socket().bind(new InetSocketAddress(welcomeSocketPort));
			welcomeSocket.configureBlocking(false);
			
			adminSocket.socket().bind(new InetSocketAddress(adminPort));
			adminSocket.configureBlocking(false);
			
			welcomeSocket.register(selector, SelectionKey.OP_ACCEPT);
			adminSocket.register(selector, SelectionKey.OP_ACCEPT);
			
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
		
		while(true){
			
			if(selector.select() == 0) {
				System.out.println("Error");
				continue;
			}
			
			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
			
			while(keys.hasNext()) {
				SelectionKey key = keys.next();
				keys.remove();
				
				if(key.isAcceptable()) {
					if(((ServerSocketChannel)key.channel()).socket().getLocalPort() == adminPort){
						//TODO: Create admin session
						new AdminSession(key);
					} else if (((ServerSocketChannel)key.channel()).socket().getLocalPort() == welcomeSocketPort) {
						//TODO: create client session
						new ClientSession(key);
					} else {
						throw new UnexpectedException("ouch!");
					}
				}
				
				if(key.isConnectable()) {
					//TODO:
					Session s = (Session)key.attachment();
					s.handleConnection();
				}
				
				if(key.isReadable()) {
					//TODO:
					Session s = (Session)key.attachment();
					s.handleRead();
				}
				
				if(key.isWritable()) {
					//TODO:
					Session s = (Session)key.attachment();
					s.handleWrite();
				}
				
			}
		}
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
		User user = users.get(username);
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
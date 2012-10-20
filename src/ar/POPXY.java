package ar;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.rmi.UnexpectedException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ar.elements.User;
import ar.sessions.AdminSession;
import ar.sessions.ClientSession;
import ar.sessions.Session;

public class POPXY {
	
	private final static int defaultWelcomeSocketPort = 1110;
	private final static int defaultAdminPort = 12345;
	private final static int defaultOriginPort = 110;
	
	private static int welcomeSocketPort = defaultWelcomeSocketPort;
	private static int adminPort = defaultAdminPort; 
	private static int originPort = defaultOriginPort; 
	
	private static POPXY instance = null;
	
	
	private Map<String, User> users = new HashMap<String, User>();
	
	public static void main(String[] args) 
		throws Exception{
		
		//Tomar conf;
		POPXY proxy = POPXY.getInstance();
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
				
				keys.remove();
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
		// TODO Auto-generated method stub
		return null;
	}

	public int getDefaultOriginServerPort() {
		// TODO Auto-generated method stub
		return 0;
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
}
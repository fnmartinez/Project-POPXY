package ar;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.rmi.UnexpectedException;
import java.util.Iterator;

import ar.sessions.AdminSession;
import ar.sessions.ClientSession;
import ar.sessions.Session;

public class POPXY {
	
	final static int defaultWelcomeSocketPort = 110;
	final static int defaultAdminPort = 12345;
	
	static int welcomeSocketPort = defaultWelcomeSocketPort;
	static int adminPort = defaultAdminPort; 

	//static Map<String, User> users = new HashMap<String, User>();
	
	public static void main(String[] args) 
		throws Exception{
		
		//Tomar conf;
		Selector selector = Selector.open();
		ServerSocketChannel welcomeSocket = ServerSocketChannel.open();
		ServerSocketChannel adminSocket = ServerSocketChannel.open();
		try{
			
			welcomeSocket.socket().bind(new InetSocketAddress(welcomeSocketPort));
			welcomeSocket.configureBlocking(false);
			
			adminSocket.socket().bind(new InetSocketAddress(adminPort));
			adminSocket.configureBlocking(false);
			
			welcomeSocket.register(selector, SelectionKey.OP_ACCEPT, null);
			adminSocket.register(selector, SelectionKey.OP_ACCEPT, null);
			
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
				System.out.println("Ay Carumba!");
				continue;
			}
			
			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
			
			while(keys.hasNext()) {
				SelectionKey key = keys.next();
				
				if(key.isAcceptable()) {
					if(((ServerSocketChannel)key.channel()).socket().getLocalPort() == adminPort){
						//TODO: Create admin session
						new AdminSession(key, selector);
					} else if (((ServerSocketChannel)key.channel()).socket().getLocalPort() == welcomeSocketPort) {
						//TODO: create client session
						new ClientSession(key, selector);
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
}
package ar.sessions;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import ar.elements.User;

public class ClientSession implements Session {

	private SocketChannel client;
	private SocketChannel originServer;
	
	private User u;
	
	int state;
	
	public ClientSession(SelectionKey key, Selector selector) {
		// TODO Auto-generated constructor stub
	}

	public void handleConnection() {
		// TODO Auto-generated method stub

	}

	public void handleWrite() {
		// TODO Auto-generated method stub

	}

	public void handleRead() {
		// TODO Auto-generated method stub

	}

}

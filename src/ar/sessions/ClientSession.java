package ar.sessions;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import ar.POPXY;
import ar.elements.User;

public class ClientSession implements Session {

	private SocketChannel client;
	private SocketChannel originServer;
	private Selector selector;
	private POPXY proxy;
	
	private User u;
	
	int state;
	
	public ClientSession(SelectionKey key, Selector selector, POPXY proxy) throws IOException {
		// TODO Auto-generated constructor stub
		this.selector = selector;
		this.proxy = proxy;
		this.client = ((ServerSocketChannel)key.channel()).accept();
		
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

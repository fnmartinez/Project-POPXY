package ar;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

import ar.sessions.AdminSession;
import ar.sessions.Session;


public class AdminWelcomeSocket extends Thread implements WelcomeSocket {
	
	private ServerSocketChannel serverSocketChannel;
	private Selector selector;
	
	public AdminWelcomeSocket(ServerSocketChannel serverSocketChannel) throws IOException {
		this.serverSocketChannel = serverSocketChannel;
		this.selector = Selector.open();
	}
	

	public void run() {
		
		try {
			this.serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
		} catch (ClosedChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while (true) {

			try {
				if (selector.select() == 0) {
					System.out.println("Error");
					continue;
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

			while (keys.hasNext()) {
				SelectionKey key = keys.next();

				if (key.isValid() && key.isAcceptable()) {
						try {
							new AdminSession(key);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
				if (key.isValid() && key.isConnectable()) {
					Session s = (Session) key.attachment();
					s.handleConnection();
				}

				if (key.isValid() && key.isReadable()) {
					Session s = (Session) key.attachment();
					s.handleRead();
				}

				if (key.isValid() && key.isWritable()) {
					Session s = (Session) key.attachment();
					s.handleWrite();
				}

				keys.remove();

			}
		}
	}

}

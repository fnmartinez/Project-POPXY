package ar;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

import ar.sessions.ClientSession;

public class ClientWelcomeSocket extends Thread implements WelcomeSocket {
	
	private ExecutorService threadPool;
	private ServerSocketChannel serverSocketChannel;
	private boolean portDidntChange = true;
	
	public ClientWelcomeSocket(ExecutorService threadPool, ServerSocketChannel serverSocketChannel) {
		this.threadPool = threadPool;
		this.serverSocketChannel = serverSocketChannel;
	}

	public void run() {
		
		while(portDidntChange) {
			SocketChannel s;
			try {
				s = this.serverSocketChannel.accept();
				String ip = s.socket().getInetAddress().getHostAddress();
				POPXY popxy = POPXY.getInstance();
				if(popxy.isOnTheBlackList(ip)){
					System.out.println("IP bloqueada!");
					s.close();
				} else {
					threadPool.execute(new ClientSession(s));
				}
			} catch (IOException e) {
				System.out.println("Se cerro el welcome socket del puerto "+this.serverSocketChannel.socket().getLocalPort());
			}
		}
		POPXY.resetWelcomeSocket();

	}
	
	synchronized public void changePort(){
		this.portDidntChange = false;
		try {
			this.serverSocketChannel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

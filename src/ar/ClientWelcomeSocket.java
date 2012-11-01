package ar;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

import ar.sessions.ClientSession;

public class ClientWelcomeSocket extends Thread implements WelcomeSocket {
	
	private ExecutorService threadPool;
	private ServerSocketChannel serverSocketChannel;
	
	public ClientWelcomeSocket(ExecutorService threadPool, ServerSocketChannel serverSocketChannel) {
		this.threadPool = threadPool;
		this.serverSocketChannel = serverSocketChannel;
	}

	public void run() {
		
		while(true) {
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


}

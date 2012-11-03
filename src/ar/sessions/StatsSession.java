package ar.sessions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import ar.POPXY;
import ar.elements.Stats;
import ar.elements.User;
import ar.protocols.ConfigurationProtocol;
import ar.sessions.utils.BufferUtils;

public class StatsSession implements Session {

	private static final int BUF_SIZE = 1024;
	private static final int NO_ESTABLISHED_CONNECTION = 1;
	private static final int ESTABLISHED_CONNECTION = 1;
	private static final int CLOSED_CONNECTION = 2;

	private int state = NO_ESTABLISHED_CONNECTION;
	private ByteBuffer buffer;
	private Selector selector;
	private SocketChannel channel;
	private SelectionKey key;

	public StatsSession(SelectionKey key) throws IOException {
		this.buffer = ByteBuffer.allocate(BUF_SIZE);
		selector = key.selector();
		channel = ((ServerSocketChannel) key.channel()).accept();
		channel.configureBlocking(false);
		this.key = channel.register(this.selector, SelectionKey.OP_WRITE, this);
	}

	public void handleConnection() {
		this.answer(ConfigurationProtocol.getWellcomeMsg());
		this.state = ESTABLISHED_CONNECTION;
	}

	public void handleWrite() {
		try {
			buffer.flip();
			channel.write(buffer);
			buffer.clear();

			if (this.state == CLOSED_CONNECTION) {
				handleEndConnection();
				return;
			}

			channel.register(this.selector, SelectionKey.OP_READ, this);

		} catch (ClosedChannelException e) {
			this.handleEndConnection();
		} catch (IOException e) {
			this.handleEndConnection();
			e.printStackTrace();
		}

	}

	public void handleRead() {
		long bytesRead = 0;

		buffer.clear();
		try {
			bytesRead = channel.read(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (bytesRead == -1) { // El administrador cerro la conexion!
			handleEndConnection();
			return;
		}
		buffer.flip();
		String cmd = BufferUtils.byteBufferToString(buffer);

		String[] parameters = cmd.trim().split("\\s");
		if (parameters.length == 0) {
			this.answer(ConfigurationProtocol.getInvalidCommandMsg());
			return;
		}

		if (parameters[0].equalsIgnoreCase("EXIT")) {
			this.answer(ConfigurationProtocol.getExitMsg());
			this.state = CLOSED_CONNECTION;
		} else if (parameters[0].equalsIgnoreCase("RESET")) {
			this.commandReset(parameters);
		} else if (parameters[0].equalsIgnoreCase("GET")) {
			this.commandGet(parameters);
		} else {
			this.answer(ConfigurationProtocol.getInvalidCommandMsg());
		}

		return;
	}

	private void commandGet(String[] parameters) {
		POPXY popxy = POPXY.getInstance();
		if (parameters.length < 2) {
			this.answer(ConfigurationProtocol.getInvalidArgumentMsg());
			return;
		}

		Stats s = null;
		if (parameters[1].equalsIgnoreCase("-g")) {
			s = User.getGlobalStats();
		} else {
			if (popxy.existingUser(parameters[1])) {
				User user = popxy.getUser(parameters[1]);
				s = user.getStats();
			} else {
				this.answer(ConfigurationProtocol.getInvalidUserMsg());
				return;
			}
		}

		String rta = ConfigurationProtocol.OK_MSG;

		if (parameters.length == 2) {
			rta = rta + "\r\n";
			rta = rta + "Bytes: " + s.getTransferedBytes() + "\r\n";
			rta = rta + "Mails-Read: " + s.getMailsReadCant() + "\r\n";
			rta = rta + "Mails-Dropped: " + s.getMailsDeletedCant() + "\r\n";
			rta = rta + "Conections: " + s.getLoginCant();
		} else if (parameters[2].equalsIgnoreCase("bytes")) {
			rta = rta + s.getTransferedBytes();
		} else if (parameters[2].equalsIgnoreCase("mails-read")) {
			rta = rta  + s.getMailsReadCant();
		} else if (parameters[2].equalsIgnoreCase("mails-dropped")) {
			rta = rta + s.getMailsDeletedCant();
		} else if (parameters[2].equalsIgnoreCase("conections")) {
			rta = rta  + s.getLoginCant();
		}

		rta += ConfigurationProtocol.FEED_TERMINATION;
		this.answer(rta);
	}

	private void commandReset(String[] parameters) {
		POPXY popxy = POPXY.getInstance();
		if (parameters.length < 2) {
			this.answer(ConfigurationProtocol.getInvalidArgumentMsg());
			return;
		}

		Stats s = null;
		if (parameters[1].equalsIgnoreCase("-g")) {
			s = User.getGlobalStats();
		} else {
			if (popxy.existingUser(parameters[1])) {
				User user = popxy.getUser(parameters[1]);
				s = user.getStats();
			} else {
				this.answer(ConfigurationProtocol.getInvalidUserMsg());
				return;
			}
		}
		s.resetStats();
		this.answer(ConfigurationProtocol.getOkMsg());
	}

	public void handleEndConnection() {
		// TODO
		System.out.println("Se cerro la conexion de estadisticas.");
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void answer(String answer) {
		this.buffer.clear();
		this.buffer.put(answer.getBytes());
		this.key.interestOps(SelectionKey.OP_WRITE);
	}
}

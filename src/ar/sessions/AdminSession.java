package ar.sessions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;

import ar.POPXY;
import ar.elements.User;
import ar.protocols.ConfigurationProtocol;
import ar.sessions.utils.BufferUtils;
import ar.sessions.utils.ConfigurationCommands;

public class AdminSession implements Session {

	private static final int NO_ESTABLISHED_CONNECTION = 0;
	private static final int ESTABLISHED_CONNECTION = 1;
	private static final int CLOSED_CONNECTION = 2;

	private static final int HEAD_COMMAND_SIZE = 4;
	private static final int BUF_SIZE = 512;

	private static final int WELLCOME_CHANNEL = 0;
	private static final int CONGIF_CHANNEL = 1;
	private static final int ORIGIN_CHANNEL = 2;
	private static final int STATS_CHANNEL = 3;

	private int state = NO_ESTABLISHED_CONNECTION;
	private SocketChannel adminChannel;
	private Selector selector;
	private ByteBuffer commandBuf;
	private ByteBuffer parametersBuf;
	private ByteBuffer answerBuf;
	private SelectionKey key;

	public AdminSession(SelectionKey key) throws IOException {

		this.commandBuf = ByteBuffer.allocate(HEAD_COMMAND_SIZE);
		this.parametersBuf = ByteBuffer.allocate(BUF_SIZE);
		this.answerBuf = this.parametersBuf;

		this.selector = key.selector();
		this.adminChannel = ((ServerSocketChannel) key.channel()).accept();
		adminChannel.configureBlocking(false);

		this.answer(ConfigurationProtocol.getWellcomeMsg());

		this.key = adminChannel.register(this.selector, SelectionKey.OP_WRITE,
				this);

		this.state = ESTABLISHED_CONNECTION;
	}

	public void handleConnection() {

	}

	public void handleWrite() {
		try {
			answerBuf.flip();
			adminChannel.write(answerBuf);
			answerBuf.clear();

			if (this.state == CLOSED_CONNECTION) {
				handleEndConection();
				return;
			}

			this.key = adminChannel.register(this.selector,
					SelectionKey.OP_READ, this);

		} catch (ClosedChannelException e) {
			this.handleEndConection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void handleRead() {
		long bytesRead = 0;

		commandBuf.clear();
		parametersBuf.clear();

		ByteBuffer[] buffers = { commandBuf, parametersBuf };

		try {
			bytesRead = adminChannel.read(buffers);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (bytesRead == -1) { // El administrador cerro la conexion!
			handleEndConection();
		} else if (bytesRead > 0) {

			commandBuf.flip();
			parametersBuf.flip();

			String commandString = BufferUtils.byteBufferToString(commandBuf);
			ConfigurationCommands command = ConfigurationProtocol
					.getCommand(commandString);
			if (command == null) {
				this.answer(ConfigurationProtocol.getInvalidCommandMsg());
				this.key.interestOps(SelectionKey.OP_WRITE);
				return;
			}

			if (command == ConfigurationCommands.EXIT) {
				this.answer(ConfigurationProtocol.getExitMsg());
				this.key.interestOps(SelectionKey.OP_WRITE);
				this.state = CLOSED_CONNECTION;
				return;
			}

			if (command == ConfigurationCommands.STATUS) {
				this.answer(ConfigurationProtocol.getStatusMsg(POPXY
						.getInstance()));
				this.key.interestOps(SelectionKey.OP_WRITE);
				return;
			}

			if (command == ConfigurationCommands.RESET) {
				User.resetGlobalConfiguration();
				this.answer(ConfigurationProtocol.getOkMsg());
				this.key.interestOps(SelectionKey.OP_WRITE);
				return;
			}

			String subCommandAndParameters = BufferUtils
					.byteBufferToString(parametersBuf);

			ConfigurationCommands subCommand = ConfigurationProtocol
					.getSubCommand(subCommandAndParameters);
			if (subCommand == null) {
				this.answer(ConfigurationProtocol.getInvalidSubCommandMsg());
				this.key.interestOps(SelectionKey.OP_WRITE);
				return;
			}
			String[] parameters = ConfigurationProtocol.getParameters(
					subCommandAndParameters, subCommand);
			if (parameters == null) {
				this.answer(ConfigurationProtocol.getInvalidArgumentMsg());
				this.key.interestOps(SelectionKey.OP_WRITE);
				return;
			}

			switch (subCommand) {
			case TIME_LOGIN:
				this.timeLogin(command, parameters);
				break;
			case CANT_LOGIN:
				this.cantLogin(command, parameters);
				break;
			case BLACK_IP:
				this.blackIp(command, parameters);
				break;
			case RM_FILTER_DATE: //
			case RM_FILTER_SENDER: //
			case RM_FILTER_HEADER: // SAME REMOVE FILTER
			case RM_FILTER_CONTENT: //
			case RM_FILTER_SIZE: //
			case RM_FILTER_DISPOSITION:
				this.removeFilter(command, subCommand, parameters);
				break;
			case ORIGIN_SERVER:
				this.setOriginServer(command, parameters);
				break;
			case ORIGIN_SERVER_PORT:
				this.setListeningPort(ORIGIN_CHANNEL, command, parameters);
				break;
			case CONFIG_LISTENING_PORT:
				this.setListeningPort(CONGIF_CHANNEL, command, parameters);
				break;
			case WELLCOME_LISTENING_PORT:
				this.setListeningPort(WELLCOME_CHANNEL, command, parameters);
				break;
			case STA_LISTENING_PORT:
				this.setListeningPort(STATS_CHANNEL, command, parameters);
				break;
			case APP:
				this.setApp(command, parameters);
				break;
			default:
				System.out.println("ERRORRRR");
				return;
			}
			;

			this.key.interestOps(SelectionKey.OP_WRITE);
			return;
		}

	}

	private void answer(String answer) {
		this.answerBuf.clear();
		this.answerBuf.put(answer.getBytes());

	}

	private void handleEndConection() {
		// TODO
		System.out.println("Se cerro la conexion del administrador!!!");
		try {
			adminChannel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void setListeningPort(int channel, ConfigurationCommands command,
			String[] parameters) {

		if (command == ConfigurationCommands.SET) {
			POPXY p = POPXY.getInstance();
			// El primer parametro es el puerto: SET PORT xxxxx
			Integer port = Integer.parseInt(parameters[0]);
			switch (channel) {
			case WELLCOME_CHANNEL:
				p.setWellcomePort(port);
				break;
			case CONGIF_CHANNEL:
				p.setAdminPort(port);
				break;
			case ORIGIN_CHANNEL:
				User.setGlobalServerPort(port);
				break;
			case STATS_CHANNEL:
				p.setStatsPort(port);
				break;
			default:
				System.out.println("ERROR!!!");
				break;
			}
			this.answer(ConfigurationProtocol.getOkMsg());
			return;
		}

		this.answer(ConfigurationProtocol.getInvalidCommandsMsg());
		return;
	}

	private void setOriginServer(ConfigurationCommands command,
			String[] parameters) {

		POPXY popxy = POPXY.getInstance();
		User user = null;

		String[] serverAndPort = parameters[0].split(":");
		Integer port = null;
		if (serverAndPort.length == 2) {
			port = Integer.parseInt(serverAndPort[1]);
		}

		// Se le setea a un unico usuario
		if (parameters.length == 2) {
			for (int i = 2; i < parameters.length; i++) {
				user = popxy.getUser(parameters[i]);
				// Si el usuario no existe
				if (user == null) {
					answer(ConfigurationProtocol.getInvalidUserMsg());
					break;
				}

				if (command == ConfigurationCommands.SET) {
					user.setServerAddress(serverAndPort[0]);
					if (serverAndPort.length == 2)
						user.setServerPort(port);
					this.answer(ConfigurationProtocol.getOkMsg());

				} else if (command == ConfigurationCommands.DELETE) {
					user.setServerAddress(User.getGlobalServerAddress());
					this.answer(ConfigurationProtocol.getOkMsg());
				}
			}
		} else { // CONFIG GLOBAL
			// Es una configuracion GLOBAL
			if (command == ConfigurationCommands.SET) {
				User.setGlobalServerAddress(serverAndPort[0]);
				if (serverAndPort.length == 2)
					User.setGlobalServerPort(port);
				this.answer(ConfigurationProtocol.getOkMsg());

			} else {
				this.answer(ConfigurationProtocol.getInvalidCommandsMsg());
			}
		}
		return;
	}

	private void timeLogin(ConfigurationCommands command, String[] parameters) {
		POPXY popxy = POPXY.getInstance();
		User user = null;

		// Si me pasa como parametro un usuario(no es global):
		if (parameters.length > 2) {
			for (int i = 2; i < parameters.length; i++) {
				user = popxy.getUser(parameters[i]);
				// Si el usuario no existe
				if (user == null) {
					answer(ConfigurationProtocol.getInvalidUserMsg());
					break;
				}

				if (command == ConfigurationCommands.SET) {
					user.addInterval(Integer.parseInt(parameters[0]),
							Integer.parseInt(parameters[1]));
					this.answer(ConfigurationProtocol.getOkMsg());
				} else if (command == ConfigurationCommands.DELETE) {
					user.removeInterval(Integer.parseInt(parameters[0]),
							Integer.parseInt(parameters[1]));
					this.answer(ConfigurationProtocol.getOkMsg());
				}
			}

		} else {

			// Es una configuracion global
			if (command == ConfigurationCommands.SET) {
				User.addGlobalInterval(Integer.parseInt(parameters[0]),
						Integer.parseInt(parameters[1]));
				this.answer(ConfigurationProtocol.getOkMsg());

			} else if (command == ConfigurationCommands.DELETE) {
				User.removeGlobalInterval(Integer.parseInt(parameters[0]),
						Integer.parseInt(parameters[1]));
				this.answer(ConfigurationProtocol.getOkMsg());
			}

		}

		return;
	}

	private void setApp(ConfigurationCommands command, String[] parameters) {

		POPXY popxy = POPXY.getInstance();
		User user = null;

		// Si me pasa como parametro un usuario(no es global):
		if (parameters.length > 1) {
			for (int i = 1; i < parameters.length; i++) {
				user = popxy.getUser(parameters[i]);
				// Si el usuario no existe
				if (user == null) {
					answer(ConfigurationProtocol.getInvalidUserMsg());
					break;
				}

				if (command == ConfigurationCommands.SET) {
					user.setApp(parameters[0], true);
					this.answer(ConfigurationProtocol.getOkMsg());
				} else if (command == ConfigurationCommands.DELETE) {
					user.setApp(parameters[0], false);
					this.answer(ConfigurationProtocol.getOkMsg());
				}
			}

		} else {

			// Es una configuracion global
			if (command == ConfigurationCommands.SET) {
				User.setGlobalApp(parameters[0], true);
				this.answer(ConfigurationProtocol.getOkMsg());

			} else if (command == ConfigurationCommands.DELETE) {
				User.setGlobalApp(parameters[0], false);
				this.answer(ConfigurationProtocol.getOkMsg());
			}

		}

		return;
	}

	private void blackIp(ConfigurationCommands command, String[] parameters) {

		POPXY popxy = POPXY.getInstance();
		String mask = null;

		if (parameters.length == 1) {
			mask = "255.255.255.255";
		} else {
			mask = parameters[1];
		}

		if (command == ConfigurationCommands.SET) {
			popxy.addIpToBlackList(parameters[0], mask);
			this.answer(ConfigurationProtocol.getOkMsg());
			return;
		} else if (command == ConfigurationCommands.DELETE) {
			popxy.deleteIpFromBlackList(parameters[0], mask);
			this.answer(ConfigurationProtocol.getOkMsg());
			return;
		}
		this.answer(ConfigurationProtocol.getInvalidCommandsMsg());
		return;

	}

	private void cantLogin(ConfigurationCommands command, String[] parameters) {
		POPXY popxy = POPXY.getInstance();
		User user = null;

		// Si me pasa como parametro un usuario(no es global):
		if (parameters.length > 1) {
			for (int i = 1; i < parameters.length; i++) {
				user = popxy.getUser(parameters[i]);
				// Si el usuario no existe
				if (user == null) {
					answer(ConfigurationProtocol.getInvalidUserMsg());
					break;
				}

				if (command == ConfigurationCommands.SET) {
					user.setLoginMax(Integer.parseInt(parameters[0]));
					this.answer(ConfigurationProtocol.getOkMsg());
				} else if (command == ConfigurationCommands.DELETE) {
					user.deleteLoginMax();
					this.answer(ConfigurationProtocol.getOkMsg());
				}

			}
		} else {
			// Es una configuracion global

			if (command == ConfigurationCommands.SET) {
				User.setGlobalLoginMax(Integer.parseInt(parameters[0]));
				this.answer(ConfigurationProtocol.getOkMsg());
			} else if (command == ConfigurationCommands.DELETE) {
				User.deleteGlobalLoginMax();
				this.answer(ConfigurationProtocol.getOkMsg());
			}
		}

		return;

	}

	private void removeFilter(ConfigurationCommands command,
			ConfigurationCommands subCommand, String[] parameters) {

		switch (subCommand) {
		case RM_FILTER_DATE:
			this.deletionFilterDate(command, parameters);
			break;
		case RM_FILTER_SENDER:
			this.deletionFilterSender(command, parameters);
			break;
		case RM_FILTER_HEADER:
			this.deletionFilterHeader(command, parameters);
			break;
		case RM_FILTER_CONTENT:
			this.deletionFilterContent(command, parameters);
			break;
		case RM_FILTER_SIZE:
			this.deletionFilterSize(command, parameters);
			break;
		case RM_FILTER_DISPOSITION:
			this.deletionFilterDisposition(command, parameters);
			break;
		default:
			System.out.println("ERROR!!!");
			break;

		}

	}

	// Parametros de la forma text/plain application/pdf image/jpg o sea lo que
	// va desp de Content-Type:
	private void deletionFilterDisposition(ConfigurationCommands command,
			String[] parameters) {
		POPXY popxy = POPXY.getInstance();
		User user;
		// Si me pasa como parametro un usuario(no es global):
		if (parameters.length > 1) {
			for (int i = 1; i < parameters.length; i++) {
				user = popxy.getUser(parameters[i]);
				if (command == ConfigurationCommands.SET) {
					user.getDeletionConfiguration().addStructure(parameters[0]);
					this.answer(ConfigurationProtocol.getOkMsg());
				} else if (command == ConfigurationCommands.DELETE) {
					user.getDeletionConfiguration().removeSender(parameters[0]);
					this.answer(ConfigurationProtocol.getOkMsg());
				}
			}

		} else {

			// Es una configuracion global
			if (command == ConfigurationCommands.SET) {
				User.getGlobalDeletionConfiguration().addStructure(
						parameters[0]);
				this.answer(ConfigurationProtocol.getOkMsg());

			} else if (command == ConfigurationCommands.DELETE) {
				User.getGlobalDeletionConfiguration().removeSender(
						parameters[0]);
				this.answer(ConfigurationProtocol.getOkMsg());
			}
		}
		return;
	}

	private void deletionFilterSize(ConfigurationCommands command,
			String[] parameters) {
		POPXY popxy = POPXY.getInstance();
		User user;
		// Si me pasa como parametro un usuario(no es global):
		if (parameters.length > 1) {
			for (int i = 1; i < parameters.length; i++) {
				user = popxy.getUser(parameters[i]);
				if (command == ConfigurationCommands.SET) {
					user.getDeletionConfiguration().setSizeRestriction(
							Integer.valueOf(parameters[0]));
					this.answer(ConfigurationProtocol.getOkMsg());
				} else if (command == ConfigurationCommands.DELETE) {
					user.getDeletionConfiguration().setSizeRestriction(-1);
					this.answer(ConfigurationProtocol.getOkMsg());
				}
			}

		} else {

			// Es una configuracion global
			if (command == ConfigurationCommands.SET) {
				User.getGlobalDeletionConfiguration().setSizeRestriction(
						Integer.valueOf(parameters[0]));
				this.answer(ConfigurationProtocol.getOkMsg());

			} else if (command == ConfigurationCommands.DELETE) {
				User.getGlobalDeletionConfiguration().setSizeRestriction(-1);
				this.answer(ConfigurationProtocol.getOkMsg());
			}
		}
		return;

	}

	// parametros: contentType (text image application etc)
	private void deletionFilterContent(ConfigurationCommands command,
			String[] parameters) {
		POPXY popxy = POPXY.getInstance();
		User user;
		// Si me pasa como parametro un usuario(no es global):
		if (parameters.length > 1) {
			for (int i = 1; i < parameters.length; i++) {
				user = popxy.getUser(parameters[i]);
				if (command == ConfigurationCommands.SET) {
					user.getDeletionConfiguration().addContentType(
							parameters[0]);
					this.answer(ConfigurationProtocol.getOkMsg());
				} else if (command == ConfigurationCommands.DELETE) {
					user.getDeletionConfiguration().removeContentType(
							parameters[0]);
					this.answer(ConfigurationProtocol.getOkMsg());
				}
			}

		} else {

			// Es una configuracion global
			if (command == ConfigurationCommands.SET) {
				User.getGlobalDeletionConfiguration().addContentType(
						parameters[0]);
				this.answer(ConfigurationProtocol.getOkMsg());

			} else if (command == ConfigurationCommands.DELETE) {
				User.getGlobalDeletionConfiguration().removeContentType(
						parameters[0]);
				this.answer(ConfigurationProtocol.getOkMsg());
			}
		}
		return;

	}

	// parametros: headerName headerValue
	private void deletionFilterHeader(ConfigurationCommands command,
			String[] parameters) {
		POPXY popxy = POPXY.getInstance();
		User user;
		// Si me pasa como parametro un usuario(no es global):
		if (parameters.length > 2) {
			for (int i = 2; i < parameters.length; i++) {
				user = popxy.getUser(parameters[i]);
				if (command == ConfigurationCommands.SET) {
					user.getDeletionConfiguration().addHeader(parameters[0],
							parameters[1]);
					this.answer(ConfigurationProtocol.getOkMsg());
				} else if (command == ConfigurationCommands.DELETE) {
					user.getDeletionConfiguration().removeHeader(parameters[0],
							parameters[1]);
					this.answer(ConfigurationProtocol.getOkMsg());
				}
			}

		} else {

			// Es una configuracion global
			if (command == ConfigurationCommands.SET) {
				User.getGlobalDeletionConfiguration().addHeader(parameters[0],
						parameters[1]);
				this.answer(ConfigurationProtocol.getOkMsg());

			} else if (command == ConfigurationCommands.DELETE) {
				User.getGlobalDeletionConfiguration().removeHeader(
						parameters[0], parameters[1]);
				this.answer(ConfigurationProtocol.getOkMsg());
			}
		}
		return;

	}

	private void deletionFilterDate(ConfigurationCommands command,
			String[] parameters) {
		// TODO Auto-generated method stub

	}

	// parametros: sender
	private void deletionFilterSender(ConfigurationCommands command,
			String[] parameters) {
		POPXY popxy = POPXY.getInstance();
		User user;
		// Si me pasa como parametro un usuario(no es global):
		if (parameters.length > 1) {
			for (int i = 1; i < parameters.length; i++) {
				user = popxy.getUser(parameters[i]);
				if (command == ConfigurationCommands.SET) {
					user.getDeletionConfiguration().addSender(parameters[0]);
					this.answer(ConfigurationProtocol.getOkMsg());
				} else if (command == ConfigurationCommands.DELETE) {
					user.getDeletionConfiguration().removeSender(parameters[0]);
					this.answer(ConfigurationProtocol.getOkMsg());
				}
			}

		} else {

			// Es una configuracion global
			if (command == ConfigurationCommands.SET) {
				User.getGlobalDeletionConfiguration().addSender(parameters[0]);
				this.answer(ConfigurationProtocol.getOkMsg());

			} else if (command == ConfigurationCommands.DELETE) {
				User.getGlobalDeletionConfiguration().removeSender(
						parameters[0]);
				this.answer(ConfigurationProtocol.getOkMsg());
			}
		}
		return;
	}

}

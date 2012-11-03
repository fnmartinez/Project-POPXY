package ar.sessions;


public interface Session {
	
	public void handleConnection();
	
	public void handleWrite();
	
	public void handleRead();

	public void handleEndConnection();

}

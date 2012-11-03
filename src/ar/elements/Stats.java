package ar.elements;

public class Stats {
	

	private long transferedBytes;
	private long loginCant;
	private long mailsReadCant;
	private long mailsDeletedCant;
	
	public Stats(){
		this.resetStats();
	}
	
	public void resetStats(){
		this.transferedBytes = 0;
		this.loginCant = 0;
		this.mailsDeletedCant = 0;
		this.mailsReadCant = 0;
	}
	
	public void addTransferedBytes(long transferedBytes){
		this.transferedBytes += transferedBytes;
	}
	
	public void incrementMailsReadCant(){
		mailsReadCant++;
	}
	
	public void incrementMailsDeletedCant(){
		mailsDeletedCant++;
	}
	
	public void incrementLoginCant(){
		loginCant++;
	}
	public long getTransferedBytes() {
		return transferedBytes;
	}
	
	public long getLoginCant() {
		return loginCant;
	}
	
	public long getMailsReadCant() {
		return mailsReadCant;
	}
	
	public long getMailsDeletedCant() {
		return mailsDeletedCant;
	}
}

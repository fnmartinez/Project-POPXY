package ar.elements;

public class Stats {
	
	public long transferedBytes;
	public long accessCant;
	public long loginCant;
	public long mailsReadCant;
	public long mailsDeletedCant;
	
	public Stats(){
		this.resetStats();
	}
	
	public void resetStats(){
		this.transferedBytes = 0;
		this.accessCant = 0;
		this.loginCant = 0;
		this.mailsDeletedCant = 0;
		this.mailsReadCant = 0;
	}
	
	public void addTransferedBytes(long transferedBytes){
		this.transferedBytes += transferedBytes;
	}
	
	public void incrementAccessCant(){
		accessCant++;
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
}

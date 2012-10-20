package ar;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public class Response {
	
	private ByteBuffer[] buffers;
	private ByteChannel channel;
	private int operation;
	private State state;
	
	public ByteBuffer[] getBuffers() {
		return buffers;
	}
	public void setBuffers(ByteBuffer[] buffers) {
		this.buffers = buffers;
	}
	public ByteChannel getChannel() {
		return channel;
	}
	public void setChannel(ByteChannel channel) {
		this.channel = channel;
	}
	public int getOperation() {
		return operation;
	}
	public void setOperation(int operation) {
		this.operation = operation;
	}
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	

}

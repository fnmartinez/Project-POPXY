package ar;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public class Action {
	
	private ByteBuffer buffers;
	private ByteBuffer multilineBuffer;
	private ByteChannel channel;
	private int operation;
	private State state;
	private boolean multilineResponse = false;
	
	public ByteBuffer getBuffers() {
		return buffers;
	}
	public void setBuffers(ByteBuffer buffers) {
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
	public void setMultilineResponse(boolean bool) {
		this.multilineResponse = bool;
	}
	public boolean isMultilineResponse() {
		return this.multilineResponse;
	}
	public void setMultilineBuffer(ByteBuffer multilineBuffer) {
		this.multilineBuffer = multilineBuffer;
	}
	public ByteBuffer getMultilineBuffer() {
		return this.multilineBuffer;
	}
	
}

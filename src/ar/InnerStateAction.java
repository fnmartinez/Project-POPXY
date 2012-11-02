package ar;

public class InnerStateAction extends Action {

	private Object attachment;
	
	public InnerStateAction(Action a) {
		InnerStateAction isa = new InnerStateAction();
		isa.setBuffers(a.getBuffers());
		isa.setChannel(a.getChannel());
		isa.setMultilineBuffer(a.getMultilineBuffer());
		isa.setMultilineResponse(a.isMultilineResponse());
		isa.setOperation(a.getOperation());
		isa.setState(a.getState());
	}

	public InnerStateAction() {
		super();
	}

	public Object getAttachment() {
		return attachment;
	}

	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}

}

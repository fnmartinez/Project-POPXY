package ar;

public class InnerStateAction extends Action {

	public InnerStateAction(Action a) {
		this.setBuffers(a.getBuffers());
		this.setChannel(a.getChannel());
		this.setOperation(a.getOperation());
		this.setState(a.getState());

	}
	
	public InnerStateAction() {
		super();
	}
}

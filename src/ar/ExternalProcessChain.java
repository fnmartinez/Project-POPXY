package ar;

import java.io.File;
import java.util.List;


public class ExternalProcessChain {
	

	private List<ExternalProcess> processes;
	
	public ExternalProcessChain(String[] processes) {
		
		for(String p: processes) {
			this.processes.add(new ExternalProcess(p));
		}
	}
	
	public File process(File input, String prefix, String sufix) {
		
		File output;
		for(ExternalProcess ep: processes){
			output = ep.process(input, prefix, sufix);
			input = output;
		}
		return input;
	}
	
	private class ExternalProcess {

		
		public ExternalProcess(String p) {
			// TODO Auto-generated constructor stub
		}

		public File process(File input, String prefix, String sufix) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}

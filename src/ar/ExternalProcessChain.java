package ar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;


public class ExternalProcessChain {
	

	private List<ExternalProcess> processes;
	
	public ExternalProcessChain(String[][] processes) {
		
		this.processes = new LinkedList<ExternalProcess>();
		
		for(String[] p: processes) {
			this.processes.add(new ExternalProcess(p));
		}
	}
	
	public File process(File input, String prefix, String sufix) {
		File output;
//		for(ExternalProcess ep: processes){
//			output = ep.process(input, prefix, sufix);
//			input.delete();
//			input = output;
//		}
		return input;
	}
	
	private class ExternalProcess {

		private String[] cmd;
		public ExternalProcess(String[] p) {
			this.cmd = p;
		}

		public File process(File input, String prefix, String sufix) throws IOException {

			File output = File.createTempFile((prefix == null)? "":prefix, (sufix == null)? "": sufix);
			Process p = Runtime.getRuntime().exec(this.cmd);
			BufferedWriter pbw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			BufferedReader pbr = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			BufferedReader fbr = new BufferedReader(new FileReader(input));
			BufferedWriter fbw = new BufferedWriter(new FileWriter(output));
			
			String line;
			
			while((line = fbr.readLine()) != null) {
				pbw.write(fbr.readLine());
			}
			
			return output;
		}
		
	}
}

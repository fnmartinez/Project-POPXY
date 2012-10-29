package ar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class ExternalProcessChain {
	

	private List<ExternalProcess> processes;
	
	public ExternalProcessChain(Set<String[]> processes) {
		
		this.processes = new LinkedList<ExternalProcess>();
		
		for(String[] p: processes) {
			this.processes.add(new ExternalProcess(p));
		}
	}
	
	public RandomAccessFile process(RandomAccessFile input, String prefix, String sufix) {

		for(ExternalProcess ep: processes){
			RandomAccessFile output;
			try {
				output = ep.process(input, prefix, sufix);
				input = output;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return input;
	}
	
	private class ExternalProcess {

		private String[] cmd;
		public ExternalProcess(String[] p) {
			this.cmd = p;
		}

		public RandomAccessFile process(RandomAccessFile input, String prefix, String sufix) throws IOException {

			File f = File.createTempFile(prefix, sufix);
			RandomAccessFile output = new RandomAccessFile(f, "rw"); 
			Process p = Runtime.getRuntime().exec(this.cmd);
			BufferedWriter pbw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			BufferedReader pbr = new BufferedReader(new InputStreamReader(p.getInputStream()));
						
			String line;
			
			while((line = input.readLine()) != null) {
				pbw.write(line+"\r\n");
			}
			
			while((line = pbr.readLine()) != null) {
				output.write((line+"\r\n").getBytes());
			}
			
			return output;
		}
		
	}
}

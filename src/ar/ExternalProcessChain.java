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
import java.util.Set;


public class ExternalProcessChain {
	

	private List<ExternalProcess> processes;
	
	public ExternalProcessChain(Set<String[]> processes) {
		
		this.processes = new LinkedList<ExternalProcess>();
		
		for(String[] p: processes) {
			this.processes.add(new ExternalProcess(p));
		}
	}
	
	public File process(File input, String prefix, String sufix) throws IOException {

		for(ExternalProcess ep: processes){
			File output = File.createTempFile(prefix, sufix);
			try {
				output = ep.process(input, prefix, sufix);
				input.delete();
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

		public File process(File input, String prefix, String sufix) throws IOException {

			File output = File.createTempFile(prefix, sufix);
//			RandomAccessFile output = new RandomAccessFile(f, "rw"); 
//			output.seek(0);
			Process p = Runtime.getRuntime().exec(this.cmd);
			BufferedReader fbr = new BufferedReader(new FileReader(input));
			BufferedWriter fbw = new BufferedWriter(new FileWriter(output));
			BufferedWriter pbw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			BufferedReader pbr = new BufferedReader(new InputStreamReader(p.getInputStream()));
						

			String line;
			
//			input.seek(0);
			while((line = fbr.readLine()) != null) {
				pbw.write(line+"\r\n");
				pbw.flush();
				while(pbr.ready() && (line =pbr.readLine()) != null) {
					fbw.write((line+"\r\n"));
				}
			}
			pbw.close();

			while((line = pbr.readLine()) != null) {
				fbw.write((line+"\r\n"));
			}
		
			pbr.close();
//			output.seek(0);
			
			return output;
		}
		
	}
}

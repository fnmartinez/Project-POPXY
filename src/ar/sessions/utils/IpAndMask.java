package ar.sessions.utils;

public class IpAndMask {
	
	private String ip;
	private String mask;
	
	public IpAndMask(String ip, String mask) {		
		super();
		this.ip = ip;
		this.mask = mask;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMask() {
		return mask;
	}

	public void setMask(String mask) {
		this.mask = mask;
	}
	
	public boolean matchNet(String ipArg){
		if(!isValidIp(ipArg)){
			return false;
		}
		
		Integer[] ipArgOct = toInteger(ipArg);
		Integer[] ipNetOct = toInteger(this.ip);
		Integer[] maskOct = toInteger(this.mask);
		
		int oct;
		for(int i = 0; i < ipArgOct.length; i++){
			oct = ipArgOct[i] & maskOct[i];
			if(oct != ipNetOct[i]){
				return false;
			}
		}
		
		return true;
		
	}
	
	private Integer[] toInteger(String ip){
		
		Integer ipOctetos[] = new Integer[4];
		String aux[] = ip.split("\\.");
		
		int i = 0;
		for(String octeto: aux){
			try{
			ipOctetos[i] = Integer.parseInt(octeto);
			}
			catch(NumberFormatException e){
				return null;
			}
			i++;
		}
		
		return ipOctetos;
	}
	
	public static boolean isValidIp(String ip) {
		String aux[] = ip.split("\\.");
		int i = 0;
		Integer oct = null;
		for(String octeto: aux){
			try{
				oct = Integer.parseInt(octeto);
			}
			catch(NumberFormatException e){
				return false;
			}
			if(oct < 0 || oct > 255){
				return false;
			}
			i++;
		}
		if(i == 4)
			return true;
		
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + ((mask == null) ? 0 : mask.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IpAndMask other = (IpAndMask) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (mask == null) {
			if (other.mask != null)
				return false;
		} else if (!mask.equals(other.mask))
			return false;
		return true;
	}

}

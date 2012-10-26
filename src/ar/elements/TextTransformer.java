package ar.elements;

public class TextTransformer {
	
	public String l33t(String message) {
		char[] msg = message.toCharArray();
		for (int i = 0; i < message.length(); i++) {
			switch (msg[i]) {
			case 'a':
				msg[i] = '4';
				break;
			case 'e':
				msg[i] = '3';
				break;
			case 'i':
				msg[i] = '1';
				break;
			case 'o':
				msg[i] = '0';
				break;
			case '[':
				i = checkImageContent(i, msg);
				break;
			}
		}
		return String.valueOf(msg);
	}
	
    private int checkImageContent(int index, char[] msg) {
        String line = "", imageName = "";
        int i = index;
        for (; i < msg.length && i < index + 8; i++) {
                line += msg[i];
        }

        if (!line.equals("[image: ")) {
                return index;
        }

        for (; i < msg.length && msg[i] != ']'; i++) {
                imageName += msg[i];
        }
        
        if(i < msg.length && msg[i] == ']') {
                return i;
        }

        return index;
}
}

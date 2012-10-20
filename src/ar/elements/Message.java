package ar.elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class Message {

    private final Map<String, List<String>> headers;
    private SortedSet<Content> orderedContent;
    private Map<Content.Type, List<Content>> contentMap;
    private String body;
    
    public Message(){
    	this.headers = new HashMap<String, List<String>>();
    	this.orderedContent = new TreeSet<Content>();
    	this.contentMap = new HashMap<Content.Type, List<Content>>();
    }
	
}

package ar.Filters;

import java.nio.channels.Channel;

public interface NetFilter {
	public boolean filter(Channel ch);
}

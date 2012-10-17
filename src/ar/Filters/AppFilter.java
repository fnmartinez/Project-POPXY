package ar.Filters;

import java.nio.ByteBuffer;

import ar.elements.User;

public interface AppFilter {
	public boolean filter(String string, User user);
}

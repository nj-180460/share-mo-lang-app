package org.sharemolangapp.smlapp.receiver;

public interface ReadableProcess<T> {
	void read(T readable);
	default void read() {}
}

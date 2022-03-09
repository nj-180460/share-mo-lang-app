package org.sharemolangapp.smlapp.sender;

import org.sharemolangapp.smlapp.layer.Processable;

public interface SendableProcess extends Processable{
	@Override
	public void process();
}

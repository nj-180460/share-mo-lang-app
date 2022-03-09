package org.sharemolangapp.smlapp.receiver;

import org.sharemolangapp.smlapp.layer.Processable;

public interface ReceivableProcess extends Processable{
	@Override
	public void process();
}

package de.ancash.nbtnexus.serde.comparator;

import com.lmax.disruptor.EventHandler;

public class CompareEventHandler implements EventHandler<CompareEvent> {

	@Override
	public void onEvent(CompareEvent arg0, long arg1, boolean arg2) throws Exception {
		arg0.r.run();
	}

}

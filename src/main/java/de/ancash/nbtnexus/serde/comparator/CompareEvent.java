package de.ancash.nbtnexus.serde.comparator;

import de.ancash.disruptor.MultiConsumerDisruptor;

public class CompareEvent {

	MultiConsumerDisruptor<CompareEvent> mcd;
	Runnable r;

}

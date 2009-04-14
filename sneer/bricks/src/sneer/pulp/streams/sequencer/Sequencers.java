package sneer.pulp.streams.sequencer;

import sneer.brickness.OldBrick;
import sneer.software.lang.Consumer;

public interface Sequencers extends OldBrick {

	<T> Sequencer<T> createSequencerFor(Consumer<T> consumer,short bufferSize, short maxGap);
	
}

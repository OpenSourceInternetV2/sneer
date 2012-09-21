package snype.whisper.speextuples;

import basis.lang.arrays.ImmutableByteArray2D;
import sneer.bricks.expression.tuples.Tuple;

public class SpeexPacket extends Tuple {
	
	public final ImmutableByteArray2D frames;
	public final String room;
	public final short sequence;

	public SpeexPacket(ImmutableByteArray2D frames_, String room_, short sequence_) {
		frames = frames_;
		room = room_;
		sequence = sequence_;
	}

}
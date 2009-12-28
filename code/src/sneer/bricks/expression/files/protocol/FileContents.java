package sneer.bricks.expression.files.protocol;

import sneer.bricks.hardware.cpu.algorithms.crypto.Sneer1024;
import sneer.bricks.hardware.ram.arrays.ImmutableByteArray;
import sneer.bricks.pulp.tuples.Tuple;
import sneer.foundation.brickness.Seal;

public class FileContents extends Tuple {

	public final Sneer1024 hashOfFile;
	public final int blockNumber;
	public final ImmutableByteArray bytes;
	public final String debugInfo;

	public FileContents(Seal adressee_, Sneer1024 hashOfFile_, int blockNumber_, ImmutableByteArray bytes_, String debugInfo_) {
		super(adressee_);
		hashOfFile = hashOfFile_;
		blockNumber = blockNumber_;
		bytes = bytes_;
		debugInfo = debugInfo_;
	}

}

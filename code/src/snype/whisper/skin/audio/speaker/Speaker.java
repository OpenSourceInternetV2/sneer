package snype.whisper.skin.audio.speaker;

import java.io.Closeable;

import javax.sound.sampled.LineUnavailableException;

import basis.brickness.Brick;
import basis.lang.Consumer;


@Brick
public interface Speaker {

	public interface Line extends Consumer<byte[]>, Closeable {}

	/** Returns a line to play packets of PCM-encoded sound: 8000Hz, 16 bits, 2 Channels (Stereo), Signed, Little Endian */
	Line acquireLine() throws LineUnavailableException;

}

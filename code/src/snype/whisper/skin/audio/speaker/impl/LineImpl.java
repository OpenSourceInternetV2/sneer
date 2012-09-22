package snype.whisper.skin.audio.speaker.impl;

import static basis.environments.Environments.my;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import snype.whisper.skin.audio.kernel.Audio;
import snype.whisper.skin.audio.speaker.Speaker.Line;

class LineImpl implements Line {

	LineImpl() throws LineUnavailableException {
		_delegate = my(Audio.class).tryToOpenPlaybackLine();
	}

	
	private final SourceDataLine _delegate;

	
	@Override
	public void consume(byte[] packet) {
		_delegate.write(packet, 0, packet.length);
	}
	
	@Override
	public void close() {
		_delegate.close();
	}

}
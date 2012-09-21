package snype.whisper.skin.audio.loopback.impl;

import static basis.environments.Environments.my;

import java.io.ByteArrayOutputStream;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import sneer.bricks.hardware.cpu.lang.contracts.Contract;
import sneer.bricks.hardware.cpu.threads.Threads;
import snype.whisper.skin.audio.kernel.Audio;
import basis.lang.Closure;

class Player {
	
	static private ByteArrayOutputStream _buffer;
	static private SourceDataLine _sourceDataLine;
	private static Contract _stepperContract;

	static void stop() {
		_stepperContract.dispose();
		if (_sourceDataLine != null)
			_sourceDataLine.close();
	}

	static boolean start(ByteArrayOutputStream buffer) {
		try {
			_sourceDataLine = my(Audio.class).tryToOpenPlaybackLine();
		} catch (LineUnavailableException e) {
			return false;
		}

		_buffer = buffer;
		
		_stepperContract = my(Threads.class).startStepping(new Closure() { @Override public void run() {
			playBuffer();
		}});
		return true;
	}

	static private void playBuffer() {
		byte[] audioData = readBuffer();
		if (audioData.length == 0) {
			my(Threads.class).sleepWithoutInterruptions(100);
			return;
		}

		_sourceDataLine.write(audioData, 0, audioData.length);
	}


	private static byte[] readBuffer() {
		byte[] result;
		synchronized (_buffer) {
			result = _buffer.toByteArray();
			_buffer.reset();
		}
		return result;
	}

}

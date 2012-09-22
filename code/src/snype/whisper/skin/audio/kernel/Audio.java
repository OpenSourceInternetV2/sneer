package snype.whisper.skin.audio.kernel;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import basis.brickness.Brick;


@Brick
public interface Audio {

	AudioFormat defaultAudioFormat();

	TargetDataLine tryToOpenCaptureLine() throws LineUnavailableException;
	SourceDataLine tryToOpenPlaybackLine() throws LineUnavailableException;
	SourceDataLine tryToOpenPlaybackLine(AudioFormat audioFormat) throws LineUnavailableException;
	

}

package uk.co.boeing.xugglerplaybox.working;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class AudioCatcher {

	AudioFormat audioFormat;
	TargetDataLine targetDataLine;
	Thread captureThread;
	private boolean kill;
	
	private class AudioCapture {
		final long nanoTime;
		final byte[] audio;
		AudioCapture(long time, byte[] audio) {
			this.nanoTime = time;
			this.audio = audio;
		}
	}
	private final ConcurrentLinkedQueue<AudioCapture> audioCaptures;
	
	public static void main(String[] args) {
		AudioCatcher audioCatcher = new AudioCatcher(44100);
		audioCatcher.start();
		// put this in to close the audio catcher when saving a file 
		/*
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		audioCatcher.targetDataLine.close();
		*/
	}

	//8000,11025,16000,22050,44100
	public AudioCatcher(final int audioSampleRate) {
		//Get things set up for capture
		this.audioCaptures = new ConcurrentLinkedQueue<AudioCatcher.AudioCapture>();
		// setup the audio format structure
		int sampleSizeInBits = 16;
		//8,16
		int channels = 2;
		//1,2
		boolean signed = true;
		//true,false
		boolean bigEndian = false;
		//true,false
		audioFormat = new AudioFormat(audioSampleRate,
				sampleSizeInBits,
				channels,
				signed,
				bigEndian);
		// open this line to the audio capture
		DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
		try {
			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
		} catch (LineUnavailableException e) {
			System.err.println("[AudioCatcher] got error connecting to audio: " + e.getMessage());
			targetDataLine = null;
		}
		if (null != targetDataLine) {
			// Create a thread to capture the microphone
			// data into an audio file and start the
			// thread running
			captureThread = new Thread(new Runnable() {
				@Override
				public void run() {
					byte[] externalData = new byte[audioSampleRate];
	
					try{
						targetDataLine.open(audioFormat);
						targetDataLine.start();
						/*
						AudioSystem.write(
								new AudioInputStream(targetDataLine),
								AudioFileFormat.Type.WAVE,
								new File("/home/douglas/Documents/temp.wav"));*/
						int numBytesRead;
						// Read the next chunk of data from the TargetDataLine.
						do {
							numBytesRead = targetDataLine.read(externalData, 0, externalData.length);
							if (numBytesRead == externalData.length) {
								setAudio(externalData, System.nanoTime());
							}
						}
						while (false == kill && numBytesRead > 0);
					} catch (Exception e){
						e.printStackTrace();
					}
				}
			}, "AudioCatcher");
		}
	}


	
	private void setAudio(byte[] sourceData, long systemNanoTime) {
		short[] shorts = new short[sourceData.length/2];
		// to turn bytes to shorts as either big endian or little endian. 
		ByteBuffer.wrap(sourceData).asShortBuffer().get(shorts);
		synchronized (this) {
			this.audioCaptures.add(new AudioCapture(systemNanoTime, sourceData));
		}
	}

	public short[] getAudio(long nanoTime) {
		short[] toReturn = null;
		synchronized (this) {
			AudioCapture bestAudio = null;
			while (false == kill) {
				// while there are values on the queue, get the latest, not exceeding the requested time
				AudioCapture audio = this.audioCaptures.peek();
				if (null == audio || audio.nanoTime > nanoTime) {
					// this has exceeded the time, leave this on the queue, stop looking though
					break;
				}
				else {
					bestAudio = audio;
				}
				// if here this audio is good, remove from the queue
				this.audioCaptures.remove();
			}
			if (null != bestAudio) {
				// use this data, the closest without going over
				toReturn = new short[bestAudio.audio.length/2];
				// to turn bytes to shorts as either big endian or little endian. 
				ByteBuffer.wrap(bestAudio.audio).asShortBuffer().get(toReturn);
			}
		}
		return toReturn;
	}
	
	public void start() {
		if (null != this.captureThread) {
			this.kill = false;
			this.captureThread.start();
		}
	}
}

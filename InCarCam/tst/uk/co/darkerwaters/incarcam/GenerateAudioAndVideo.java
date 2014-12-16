package uk.co.darkerwaters.xugglerplaybox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;

import com.xuggle.mediatool.IMediaViewer;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IAudioSamples;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;

/**
 * Generate audio and video frames and use the {@link IMediaWriter} to
 * encode that media and write it out to a file.
 *
 * @author trebor
 * @author aclarke
 */

public class GenerateAudioAndVideo
{
	// the log

	private static final Logger log = LoggerFactory.getLogger(
			GenerateAudioAndVideo.class);
	{ log.trace("<init>"); }

	/**
	 * Create and display a number of bouncing balls on the 
	 */

	public static void main(String[] args)
	{
		// the number of balls to bounce around

		final int ballCount = 2;

		// total duration of the media

		final long duration = DEFAULT_TIME_UNIT.convert(60, SECONDS);

		// video parameters

		final int videoStreamIndex = 0;
		final int videoStreamId = 0;
		final long frameRate = DEFAULT_TIME_UNIT.convert(15, MILLISECONDS);
		final int width = 320;
		final int height = 200;

		// audio parameters

		final int audioStreamIndex = 1;
		final int audioStreamId = 0;
		final int channelCount = 1;
		final int sampleRate = 44100; // Hz
		final int sampleCount = 1000;

		// the clock time of the next frame

		long nextFrameTime = 0;

		// the total number of audio samples

		long totalSampleCount = 0;

		// create a media writer and specify the output file

		final IMediaWriter writer = ToolFactory.makeWriter("myballs.mov");

		// add a viewer so we can see the media as it is created

		writer.addListener(ToolFactory.makeViewer(
				IMediaViewer.Mode.AUDIO_VIDEO, true, 
				javax.swing.WindowConstants.EXIT_ON_CLOSE));

		// add the video stream

		writer.addVideoStream(videoStreamIndex, videoStreamId,
				width, height);

		// add the audio stream

		writer.addAudioStream(audioStreamIndex, audioStreamId,
				channelCount, sampleRate);

		// create some balls to show on the screen

		Balls balls = new MovingBalls(ballCount, width, height, sampleCount);

		// loop through clock time, which starts at zero and increases based
		// on the total number of samples created thus far

		for (long clock = 0; clock < duration; clock = IAudioSamples
				.samplesToDefaultPts(totalSampleCount, sampleRate))
		{
			// while the clock time exceeds the time of the next video frame,
			// get and encode the next video frame

			while (clock >= nextFrameTime)
			{
				BufferedImage frame = balls.getVideoFrame(frameRate);
				writer.encodeVideo(videoStreamIndex, frame, nextFrameTime, 
						DEFAULT_TIME_UNIT);
				nextFrameTime += frameRate;
			}

			// compute and encode the audio for the balls

			short[] samples = balls.getAudioFrame(sampleRate);
			writer.encodeAudio(audioStreamIndex, samples, clock, 
					DEFAULT_TIME_UNIT);
			totalSampleCount += sampleCount;
		}

		// manually close the writer

		writer.close();
	}  
}


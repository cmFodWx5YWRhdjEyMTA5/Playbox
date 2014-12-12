package uk.co.boeing.xugglerplaybox.working;

import java.awt.image.BufferedImage;

import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IError;
import com.xuggle.xuggler.IMetaData;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.Utils;

/**
 * 
 * The class automatically detects the operating system and uses the default
 * value for the web cam corresponding to the operating system but can also be
 * forced to have different driver using overloaded constructor
 *
 */
public class ProcessDualInput {
	/** the default resolution */
	final static String RES_720 = "1280x720";

	private WebcamCapture[] webcamCaptureThreads;
	private boolean kill;
	private FileSaver fileSaverThread;
	
	private static int WebcamIndex = 0;

	private class WebcamCapture {
		final Thread thread;
		final String displayDriver;
		final String displayDevice;
		final String resolution;
		final boolean isAudio;
		final int index;
		WebcamCapture(String displayDriver, String displayDevice, String resolution, boolean isAudio) {
			this.index = WebcamIndex++;
			this.displayDriver = displayDriver;
			this.displayDevice = displayDevice;
			this.resolution = resolution;
			this.isAudio = isAudio;
			this.thread = new Thread(createCapture(this), this.displayDevice);
		}
	}

	public static void main(String[] args) {
		ProcessDualInput ProcessDualInput = new ProcessDualInput(GetDriver(),
				(GetDevice(1) + ":1280x720:true"),
				(GetDevice(0) + ":320x240:false"));
	}

	public static String GetDriver() {
		String displayDriver = null;
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			displayDriver = "vfwcap";
		} else if (System.getProperty("os.name").toLowerCase().contains("nux")) {
			displayDriver = "video4linux2";
		}
		return displayDriver;
	}

	public static String GetDevice(int index) {
		String displayDevice = null;
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			displayDevice = Integer.toString(index);
		} else if (System.getProperty("os.name").toLowerCase().contains("nux")) {
			displayDevice = "/dev/video" + Integer.toString(index);
		}
		return displayDevice;
	}

	public ProcessDualInput(String driver, String... devices) {
		// Check whether the Xuggler used is GPL or not
		if (false == IVideoResampler.isSupported(IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION)) {
			throw new RuntimeException("you must install the GPL version of Xuggler (with IVideoResampler support) for this demo to work");
		}
		// now setup this application to do the work
		this.webcamCaptureThreads = new WebcamCapture[devices.length];
		for (int i = 0; i < devices.length; ++i) {
			String[] deviceStrings = devices[i].split(":");
			boolean isAudio = Boolean.parseBoolean(deviceStrings[2]);
			this.webcamCaptureThreads[i] = new WebcamCapture(driver, deviceStrings[0], deviceStrings[1], isAudio);
		};
		this.kill = false;
		// create the thread to save the file
		this.fileSaverThread = new FileSaver(devices.length, true);
		this.fileSaverThread.start(false);
		// and create the threads to capture the web-cam images
		for (WebcamCapture webcam : this.webcamCaptureThreads) {
			webcam.thread.start();
		}
	}

	private Runnable createCapture(final WebcamCapture webcam) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					// Create a Xuggler container object
					IContainer container = IContainer.make();

					// Tell Xuggler about the device format
					IContainerFormat format = IContainerFormat.make();
					throwIfFail(
							format.setInputFormat(webcam.displayDriver),
							"couldn't open webcam device: " + webcam.displayDriver);

					// Parameters for device
					IMetaData params = IMetaData.make();
					params.setValue("framerate", "30/1");
					params.setValue("video_size", webcam.resolution);

					// Open up the container, throwing if it fails
					throwIfFail(
							container.open(webcam.displayDevice, IContainer.Type.READ, format, false, true, params, null),
							"could not open file: " + webcam.displayDevice);
					

					// query how many streams found
					int numStreams = container.getNumStreams();
					// find the first video and audio streams
					int videoStreamId = -1;
					int audioStreamId = -1;
					IStreamCoder videoCoder = null;
					IStreamCoder audioCoder = null;
					for (int i = 0; i < numStreams; i++) {
						// Find the stream object
						IStream stream = container.getStream(i);
						// Get the pre-configured decoder that can decode this stream;
						IStreamCoder coder = stream.getStreamCoder();
						// create the audio / video decoders as required
						if (videoStreamId == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
							videoStreamId = i;
							videoCoder = coder;
						}
						else if (webcam.isAudio && audioStreamId == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
							audioStreamId = i;
							audioCoder = coder;
						}
					}
					if (videoStreamId == -1) {
						throw new RuntimeException("could not find video stream in container: " + webcam.displayDevice);
					}

					// open up our decoder so it can do work.
					throwIfFail(
							videoCoder.open(),
							"could not open video decoder for container: " + webcam.displayDevice);
					// and open the audio decoder if we want one
					if (null != audioCoder) {
						throwIfFail(
								audioCoder.open(),
								"could not open audio decoder for container: " + webcam.displayDevice);
					}

					IVideoResampler resampler = getVideoResampler(videoCoder);
					if (resampler == null) {
						throw new RuntimeException("could not create color space resampler for: " + webcam.displayDevice);
					}
					// Now, we start walking through the container looking at each packet.
					IPacket packet = IPacket.make();
					while(false == kill && container.readNextPacket(packet) >= 0) {
						// Now we have a packet, let's see if it belongs to our video stream
						if (packet.getStreamIndex() == videoStreamId) {
							// We allocate a new picture to get the data out of Xuggler
							processVideoPacket(webcam, videoCoder, resampler, packet, System.nanoTime());
						}
						else if (null != audioCoder && packet.getStreamIndex() == audioStreamId) {
							// this is our audio stream
							processAudioPacket(audioCoder, packet);
						}
						//else This packet isn't part of our video or audio streams, so we just silently drop it.
					}
					// close down the video coder
					if (videoCoder != null) {
						videoCoder.close();
						videoCoder = null;
					}
					// close down the container
					if (container != null) {
						container.close();
						container = null;
					}

				} catch (Exception excp) {
					// report why exited
					excp.printStackTrace();
				}
			}
		};
	}

	protected void throwIfFail(int result, String reason) {
		if (result < 0) {
			// Convert the non friendly integer return value into a slightly more friendly error name
			IError error = IError.make(result);
			throw new IllegalArgumentException(reason + "; Error: " + error.getDescription());
		}
	}

	private IVideoResampler getVideoResampler(IStreamCoder videoCoder) {
		IVideoResampler resampler = null;
		if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24) {
			// if this stream is not in BGR24, we're going to need to
			// convert it. The VideoResampler does that for us.
			resampler = IVideoResampler.make(videoCoder.getWidth(),
					videoCoder.getHeight(), IPixelFormat.Type.BGR24,
					videoCoder.getWidth(), videoCoder.getHeight(),
					videoCoder.getPixelType());
		}
		return resampler;
	}

	private void processVideoPacket(final WebcamCapture webcam, IStreamCoder videoCoder, IVideoResampler resampler, IPacket packet, long frameTime) {
		// create the video picture from the packed
		IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(), videoCoder.getWidth(), videoCoder.getHeight());
		int offset = 0;
		while(offset < packet.getSize()) {
			// Now, we decode the video, checking for any errors.
			int bytesDecoded = videoCoder.decodeVideo(picture, packet, offset);
			if (bytesDecoded < 0) {
				System.err.println("[DualInput] got error decoding video");
				break;
			}
			else {
				offset += bytesDecoded;
				// Some decoders will consume data in a packet, but will not be able to construct
				// a full video picture yet.  Therefore you should always check if you
				// got a complete picture from the decoder
				if (picture.isComplete()) {
					processVideoPicture(webcam, resampler, frameTime, picture);
				}
			}
		}
	}

	private void processVideoPicture(final WebcamCapture webcam, IVideoResampler resampler, long imageTime, IVideoPicture picture) {
		IVideoPicture picToProcess = picture;
		// If the resampler is not null, that means we didn't get the
		// video in BGR24 format and need to convert it into BGR24 format.
		if (resampler != null) {
			// we must resample
			picToProcess = IVideoPicture.make(resampler.getOutputPixelFormat(), picture.getWidth(), picture.getHeight());
			if (resampler.resample(picToProcess, picture) < 0) {
				System.err.println("[DualInput] could not resample video.");
			}
		}
		if (null != picToProcess && picToProcess.getPixelType() != IPixelFormat.Type.BGR24) {
			System.err.println("[DualInput] could not decode video as BGR 24 bit data");
		}
		else {
			// And finally, convert the BGR24 to an Java buffered image
			BufferedImage javaImage = Utils.videoPictureToImage(picToProcess);
			// and put this in the queue to process and save
			if (null != javaImage) {
				fileSaverThread.setImage(webcam.index, imageTime, javaImage);
			}
		}
	}

	private void processAudioPacket(IStreamCoder audioCoder, IPacket packet) {
		// make the audio samples to decode them from this stream
		IAudioSamples samples = IAudioSamples.make(1024, audioCoder.getChannels());
		/*
		 * A packet can actually contain multiple sets of samples (or frames of samples
		 * in audio-decoding speak).  So, we may need to call decode audio multiple
		 * times at different offsets in the packet's data.  We capture that here.
		 */
		int offset = 0;
		// Keep going until we've processed all data
		while (offset < packet.getSize())
		{
			int bytesDecoded = audioCoder.decodeAudio(samples, packet, offset);
			if (bytesDecoded < 0) {
				System.err.println("[DualInput] got error decoding audio");
				break;
			}
			else {
				offset += bytesDecoded;
				/*
				 * Some decoder will consume data in a packet, but will not be able to construct
				 * a full set of samples yet.  Therefore you should always check if you
				 * got a complete set of samples from the decoder
				 */
				if (samples.isComplete()) {
					//TODO send the audio samples to the file saver to save
				}
			}
		}
	}
}

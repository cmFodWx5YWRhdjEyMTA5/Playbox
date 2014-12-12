package uk.co.boeing.xugglerplaybox.working;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.xuggle.xuggler.Configuration;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;

public class ImageStreamer {
	public static final String K_DEFAULT_STREAM_URL = "rtsp://127.0.0.1:1234/live/test";
	public static final Point K_DEFAULT_STREAM_SIZE = new Point(640, 480);

	private boolean kill = false;

	private BufferedImage currentImage;

	private final Thread streamingThread;
	
	public ImageStreamer() {
		this(K_DEFAULT_STREAM_URL, K_DEFAULT_STREAM_SIZE);
	}
	public ImageStreamer(String url, Point size) {
		// find the required properties file to stream the content
		Properties properties = new Properties();
		InputStream is = ImageStreamer.class.getResourceAsStream("/libx264-normal.ffpreset");
		try {
			// load the properties
			properties.load(is);
		} catch (IOException e) {
			System.err.println("[ImageStreamer] You need the libx264-normal.ffpreset file from the Xuggle distribution in your classpath.");
			properties = null;
		}
		final IContainer container = IContainer.make();
		IContainerFormat containerFormat_live = IContainerFormat.make();
		containerFormat_live.setOutputFormat("flv", url, null);
		container.setInputBufferLength(0);
		int retVal = container.open(url, IContainer.Type.WRITE, containerFormat_live);
		if (retVal < 0 || null == properties) {
			// report this error and exit without the thread being created
			System.err.println("[ImageStreamer] Could not open output container for live stream");
			this.streamingThread = null;
		}
		else {
			// find the codec to use and create the stream
			ICodec codec = ICodec.findEncodingCodec(ICodec.ID.CODEC_ID_H264);
			IStream stream = container.addNewStream(codec);
			final IStreamCoder coder = stream.getStreamCoder();
			coder.setNumPicturesInGroupOfPictures(5);
			coder.setCodec(codec);
			coder.setBitRate(200000);
			coder.setPixelType(IPixelFormat.Type.YUV420P);
			coder.setHeight(size.y);
			coder.setWidth(size.x);
			System.out.println("[ImageStreamer] video size is " + size.x + "x" + size.y);
			coder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
			coder.setGlobalQuality(0);
			final IRational frameRate = IRational.make(25, 1);
			coder.setFrameRate(frameRate);
			coder.setTimeBase(IRational.make(frameRate.getDenominator(), frameRate.getNumerator()));
			
			Configuration.configure(properties, coder);
			
			coder.open();

			// create the thread to stream the images
			this.streamingThread = new Thread(createRunnableStreamer(container, coder), "streamer");
		}
	}
	private Runnable createRunnableStreamer(final IContainer container, final IStreamCoder coder) {
		return new Runnable() {
			@Override
			public void run() {
				// write the header for the stream
				container.writeHeader();
				long firstTimeStamp = System.currentTimeMillis();
				int i = 0;
				while (false == kill) {
					// while we are to stream an image, get the image and stream it out
					BufferedImage image;
					synchronized (ImageStreamer.this) {
						image = ImageStreamer.this.currentImage;
						ImageStreamer.this.currentImage = null;
					}
					if (null != image) {
						//start the encoding process
						long now = System.currentTimeMillis();
						IPacket packet = IPacket.make();
						IConverter converter = ConverterFactory.createConverter(image, IPixelFormat.Type.YUV420P);
						long timeStamp = (now - firstTimeStamp) * 1000; 
						IVideoPicture outFrame = converter.toPicture(image, timeStamp);
						if (i == 0) {
							//make first frame keyframe
							outFrame.setKeyFrame(true);
						}
						outFrame.setQuality(0);
						coder.encodeVideo(packet, outFrame, 0);
						outFrame.delete();
						if (packet.isComplete()) {
							// write the packet to the stream container to send the image
							container.writePacket(packet);
						}
						// increment the frame counter
						++i;
					}
				}
				// have exited the stream, write the end of the frame
				container.writeTrailer();
			}
		};
	}
	public void start() {
		if (null != streamingThread) {
			streamingThread.start();
		}
	}

	public void setImage(BufferedImage image) {
		synchronized (this) {
			this.currentImage = image;
		}
	}
}

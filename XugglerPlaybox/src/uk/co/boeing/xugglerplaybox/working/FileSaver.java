package uk.co.boeing.xugglerplaybox.working;

import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.Configuration;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.demos.VideoImage;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;

public class FileSaver {
	public static final String K_DEFAULT_STREAM_URL = "rtsp://127.0.0.1:1234/live/test";
	public static final Point K_DEFAULT_STREAM_SIZE = new Point(640, 480);
	/** the amount of time (nano seconds) a file will represent */
	private static final long K_FILETIME = 10000000000l;

	private boolean kill = false;

	private final Thread savingThread;

	private static final String outputFilename = "/home/douglas/Documents/outstream";
	
	private final ArrayList<ConcurrentLinkedQueue<CaptureImage>> imageCaches;
	
	private ImageStreamer imageStreamerThread;
	
	private AudioCatcher audioCaptureThread;
	
	private final int audioSampleRate = 44100;
	
	private final VideoWindow window;
	
	private class CaptureImage {
		final long imageTimeNano;
		final BufferedImage image;
		CaptureImage(long timeNano, BufferedImage image) {
			this.imageTimeNano = timeNano;
			this.image = image;
		}
	}
	
	public FileSaver(int noImageSources, boolean isCreateWindow) {
		this(K_DEFAULT_STREAM_URL, K_DEFAULT_STREAM_SIZE, noImageSources, isCreateWindow);
	}
	
	public FileSaver(String url, Point size, int noImageSources, boolean isCreateWindow) {
		// first create all the caching for the images that we will save to this file
		this.imageCaches = new ArrayList<ConcurrentLinkedQueue<CaptureImage>>(noImageSources);
		for (int i = 0; i < noImageSources; ++i) {
			this.imageCaches.add(new ConcurrentLinkedQueue<CaptureImage>());
		}
		this.imageStreamerThread = null;
		this.audioCaptureThread = new AudioCatcher(audioSampleRate);
		this.audioCaptureThread.start();
		if (isCreateWindow) {
			this.window = new VideoWindow();
		}
		else {
			this.window = null;
		}
		// create the thread to store the images
		this.savingThread = new Thread(createRunnableFileSaver(outputFilename), "filesaver");
	}
	
	private Runnable createRunnableFileSaver(final String outputFolder) {
		return new Runnable() {
			@Override
			public void run() {
				BufferedImage logoImage;
				try { 
					URL resource = getClass().getClassLoader().getResource("logo.png");
					logoImage = ImageIO.read(new File(resource.getFile()));
				}  
				catch (Exception e) {
					e.printStackTrace();
					logoImage = null;
				} 
				long startTime = System.nanoTime();
				IMediaWriter mediaWriter = null;

				int counter = 0;
				long frames = 0l;
				long lastImageStoreTime = 0l;
				BufferedImage combinedImage;
				CaptureImage[] syncImages = new CaptureImage[imageCaches.size()];
				
				while (false == kill) {
					// gather the images from the running threads
					long imageTime = lastImageStoreTime;
					for (int i = 0; i < syncImages.length; ++i) {
						// get the latest image from the camera
						CaptureImage image = getNextImage(i);
						if (null != image) {
							// this is a new camera image, use this and update the last image time
							syncImages[i] = image;
							imageTime = Math.max(image.imageTimeNano, imageTime);
						}
					}
					if (imageTime > lastImageStoreTime) {
						// there is at least one new image to process and store, so store it here, combine the images
						combinedImage = null;
						for (int i = 0; i < syncImages.length; ++i) {
							if (null == combinedImage) {
								// just use this to start with
								combinedImage = syncImages[i] == null ? null : syncImages[i].image;
							}
							else if (null != syncImages[i]) {
								// put this image into the combined one to combine them
								combineImages(combinedImage, syncImages[i].image);
							}
						}
						if (null == combinedImage) {
							// no image
							continue;
						}
						++frames;
						if (null != logoImage) {
							long elapsedSec = (System.nanoTime() - startTime) / 1000000000l;
							double fps = frames / (double)(elapsedSec);
							addLogo(combinedImage, logoImage, String.format("%.2f", fps) + "fps   " + String.valueOf(imageTime));
						}
						if (null == mediaWriter) {
							// make the writer for the new file
							mediaWriter = ToolFactory.makeWriter(outputFolder + String.format("_%04d", ++counter) + ".mp4");
							// We tell it we're going to add one video stream, with id 0, 
							// at position 0, and that it will have a fixed frame rate of FRAME_RATE. 
							//mediaWriter.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, videoCoder.getWidth(), videoCoder.getHeight());
							mediaWriter.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 1280, 720);
							mediaWriter.addAudioStream(1, 0, 2, 44100);
						}
						// encode the image to stream #0 in the output file
						long fileTime = System.nanoTime() - startTime;
						mediaWriter.encodeVideo(0, combinedImage, fileTime, TimeUnit.NANOSECONDS);
						short[] audioSamples = audioCaptureThread.getAudio(imageTime);
						if (null != audioSamples) {
							mediaWriter.encodeAudio(1, audioSamples, fileTime, TimeUnit.NANOSECONDS);
						}
					
						if (null != imageStreamerThread) {
							// send this image to the streamer
							imageStreamerThread.setImage(combinedImage);
						}
						if (null != window) {
							// update our window too
							window.updateJavaWindow(combinedImage);
						}
						if (fileTime > K_FILETIME) {
							// have gone past a minute, save out the file and start a new one 
							mediaWriter.close();
							mediaWriter = null;
							startTime = System.nanoTime();
							frames = 0;
							System.out.println(".");
						}
						else if (frames % 10 == 0) {
							System.out.print(".");
						}
					}
				}
				if (null != mediaWriter) {
					// tell the writer to close and write the trailer if  needed 
					mediaWriter.close();
				}
			}
		};
	}
	private CaptureImage getNextImage(int imageSource) {
		// return the image from the specified source
		return this.imageCaches.get(imageSource).poll();
	}
	public void start(boolean isStreamImages) {
		if (null != savingThread) {
			this.kill = false;
			savingThread.start();
		}
		if (isStreamImages) {
			// create the thread to stream the file
			this.imageStreamerThread = new ImageStreamer();
			this.imageStreamerThread.start();
		}
	}

	public void setImage(int imageSource, long timeNano, BufferedImage image) {
		this.imageCaches.get(imageSource).add(new CaptureImage(timeNano, image));
	}

	private void combineImages(BufferedImage sourceImage, BufferedImage imageToAdd) {
		// get the graphics for the image 
		Graphics2D g = sourceImage.createGraphics();

		// get a rect for the image we are adding
		Rectangle imageRect = new Rectangle(0, 0, imageToAdd.getWidth(), imageToAdd.getHeight());
		imageRect.translate((int)(imageRect.width * 0.2f), (int)(imageRect.height * 0.2f));
		g.drawImage(imageToAdd,imageRect.x, imageRect.y, imageRect.width, imageRect.height, null);
	}

	private static void addLogo(BufferedImage sourceImage, BufferedImage logoImage, String diagnostics) {
		// get the graphics for the image 
		Graphics2D g = sourceImage.createGraphics();
		g.drawImage(logoImage, sourceImage.getWidth() - logoImage.getWidth(), sourceImage.getHeight() - logoImage.getHeight(), null);
		if (null != diagnostics) {
			g.drawString(diagnostics, 30, 30);
		}
	}
	
	public class VideoWindow {
		private  VideoImage mScreen = null;

		public VideoWindow()
		{
			mScreen = new VideoImage();
			mScreen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		}

		public void updateJavaWindow(BufferedImage javaImage)
		{
			mScreen.setImage(javaImage);
		}

		public void closeJavaWindow(){
			mScreen.dispose();
		}
	}
}

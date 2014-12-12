package uk.co.boeing.xugglerplaybox;

import java.awt.Color; 
import java.awt.Graphics2D; 
import java.awt.geom.Rectangle2D; 
import java.awt.image.BufferedImage; 
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.IMediaWriter; 
import com.xuggle.mediatool.ToolFactory; 
import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.Utils;
  
public class ModifyStreamExample { 
  
    //private static final String inputStream = "http://192.168.0.19:80/webcam.mjpeg";
	private static final String inputStream = "rtp://127.0.0.1:1234";
	//private static final String inputStream = "rtmp://192.168.0.10:1935/live/";
	//private static final String inputStream = "http://192.168.0.10:8080/1/stream.html";
    private static final String outputFilename = "/home/douglas/Documents/outstream";
    private static final String imageFilename = "/home/douglas/Downloads/download.jpg";
	private static BufferedImage logoImage;
  
    public static void main(String[] args) {
    	
    	try { 
            logoImage = ImageIO.read(new File(imageFilename));
        }  
        catch (Exception e) {
            e.printStackTrace();
            logoImage = null;
        } 
    	
    	// Create a Xuggler container object
        IContainer container = IContainer.make();

        // Open up the container
        if (container.open(inputStream, IContainer.Type.READ, null) < 0)
        	throw new IllegalArgumentException("could not open file: " + inputStream);
  
		// query how many streams the call to open found
		int numStreams = container.getNumStreams();

		// and iterate through the streams to find the first video stream
		int videoStreamId = -1;
		IStreamCoder videoCoder = null;
		for (int i = 0; i < numStreams; i++) {
			// Find the stream object
			IStream stream = container.getStream(i);
			// Get the pre-configured decoder that can decode this stream;
			IStreamCoder coder = stream.getStreamCoder();

			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
				videoStreamId = i;
				videoCoder = coder;
				break;
			}
		}
		if (videoStreamId == -1)
			throw new RuntimeException("could not find video stream in container: " + inputStream);
        
		/*
	     * Now we have found the video stream in this file.  Let's open up our decoder so it can
	     * do work.
	     */
	    if (videoCoder.open() < 0)
	      throw new RuntimeException("could not open video decoder for container: " + inputStream);
	    
	    IVideoResampler resampler = null;
	    if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24)
	    {
	      // if this stream is not in BGR24, we're going to need to
	      // convert it.  The VideoResampler does that for us.
	      resampler = IVideoResampler.make(videoCoder.getWidth(), 
	          videoCoder.getHeight(), IPixelFormat.Type.BGR24,
	          videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
	      if (resampler == null)
	        throw new RuntimeException("could not create color space resampler for: " + inputStream);
	    }
	    
	    // Now, we start walking through the container looking at each packet.
	    IPacket packet = IPacket.make();
	    long firstTimestampInStream = Global.NO_PTS;
	    long systemClockStartTime = 0;
	    int counter = 0;
	    long startTime = System.nanoTime();
	    IMediaWriter mediaWriter = null;
	    while(container.readNextPacket(packet) >= 0) {
	    	// Now we have a packet, let's see if it belongs to our video stream
	    	if (packet.getStreamIndex() == videoStreamId) {
	    		// We allocate a new picture to get the data out of Xuggler
	    		IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(), videoCoder.getWidth(), videoCoder.getHeight());
	    		int offset = 0;
	    		while(offset < packet.getSize()) {
	    			// Now, we decode the video, checking for any errors.
	    			int bytesDecoded = videoCoder.decodeVideo(picture, packet, offset);
	    			if (bytesDecoded < 0)
	    				throw new RuntimeException("got error decoding video in: " + inputStream);
	    			offset += bytesDecoded;
	    			// Some decoders will consume data in a packet, but will not be able to construct
	    			// a full video picture yet.  Therefore you should always check if you
	    			// got a complete picture from the decoder
	    			if (picture.isComplete()) {
	    				IVideoPicture newPic = picture;
	    				// If the resampler is not null, that means we didn't get the
	    				//video in BGR24 format and need to convert it into BGR24 format.
	    				if (resampler != null) {
	    					// we must resample
	    					newPic = IVideoPicture.make(resampler.getOutputPixelFormat(), picture.getWidth(), picture.getHeight());
	    					if (resampler.resample(newPic, picture) < 0)
	    						throw new RuntimeException("could not resample video from: " + inputStream);
	    				}
	    				if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
	    					throw new RuntimeException("could not decode video as BGR 24 bit data in: " + inputStream);

	    				// And finally, convert the BGR24 to an Java buffered image
	    				BufferedImage javaImage = Utils.videoPictureToImage(newPic);
	    				if (null != logoImage) {
	    					addLogo(javaImage);
	    				}
	    				if (null == mediaWriter) {
	    					// make the writer for the new file
	    				    mediaWriter = ToolFactory.makeWriter(outputFilename + ++counter + ".mp4");
	    				    // We tell it we're going to add one video stream, with id 0, 
	    			        // at position 0, and that it will have a fixed frame rate of FRAME_RATE. 
	    				    //mediaWriter.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, videoCoder.getWidth(), videoCoder.getHeight());
	    				    mediaWriter.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 1024, 768);
	    				}
	    				// encode the image to stream #0 in the output file
	    	            mediaWriter.encodeVideo(0, javaImage, System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
	    				if (System.nanoTime() - startTime > 60000000000l) {
	    					// have gone past a minute, save out the file and start a new one 
	    			        mediaWriter.close();
	    			        mediaWriter = null;
	    			        startTime = System.nanoTime();
	    	        		System.out.println(".");
	    	        	}
	    	        	else {
	    	        		System.out.print(".");
	    	        	}
	    			}
	    		}
	    	}
	    	else {
	    		//This packet isn't part of our video stream, so we just silently drop it.
	    	}
	    }
	    if (null != mediaWriter) {
		    // tell the writer to close and write the trailer if  needed 
	        mediaWriter.close();
	    }
    }

	private static void addLogo(BufferedImage image) {
		// get the graphics for the image 
        Graphics2D g = image.createGraphics();
          
        Rectangle2D bounds = new Rectangle2D.Float(0, 0, logoImage.getWidth(), logoImage.getHeight());

        // compute the amount to inset the time stamp and  
        // translate the image to that position 
        double inset = bounds.getHeight();
        g.translate(inset, image.getHeight() - inset);

        g.setColor(Color.WHITE);
        g.fill(bounds);
        g.setColor(Color.BLACK);
        g.drawImage(logoImage, 0, 0, null);
	}
} 

package uk.co.darkerwaters.xugglerplaybox;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IError;
import com.xuggle.xuggler.IMetaData;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.Utils;
import com.xuggle.xuggler.demos.VideoImage;

/**
 * 
 * The class automatically detects the operating system and uses the default value for the web cam corresponding to the operating system
 * but can also be forced to have different driver using overloaded constructor
 *
 */
public class DisplayWebcam implements Runnable {

        private String displayDriver;
        private String displayDevice;
        private int imageNo;
        private boolean recording;
        private boolean snap;
        private File outputFile;
        private IMediaWriter writer;
        private boolean doOnce;
        private long startTime;
        private Thread webcam;
        private boolean kill;
        
        public static void main(String[] args)
        {
        	DisplayWebcam displayWebcam = new DisplayWebcam();
        }
        
        public DisplayWebcam( ){
                if(System.getProperty("os.name").toLowerCase().contains("win")){
                        displayDriver="vfwcap";
                        displayDevice="0";
                }
                else{
                        if(System.getProperty("os.name").toLowerCase().contains("nux")){
                                displayDriver="video4linux2";
                                displayDevice="/dev/video0";
                        }
                }
                doOnce=false;
                kill=false;
                snap=false;
                imageNo=0;
                webcam=new Thread(this,"Web Camera");
                webcam.start();
        }
        
        public DisplayWebcam(String driver,String device){
                displayDriver=driver;
                displayDevice=device;
                doOnce=false;
                snap=false;
                kill=false;
                imageNo=0;
                webcam=new Thread(this,"Web Camera");
                webcam.start();
        }
        
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
                try{
                //Check whether the Xuggler used is GPL or not
                    if (!IVideoResampler.isSupported(IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION))
                        throw new RuntimeException("you must install the GPL version of Xuggler (with IVideoResampler support) for this demo to work");

                      // Create a Xuggler container object
                      IContainer container = IContainer.make();

                      // Tell Xuggler about the device format
                      IContainerFormat format = IContainerFormat.make();
                      if (format.setInputFormat(displayDriver) < 0)
                        throw new IllegalArgumentException("couldn't open webcam device: " + displayDriver);

                      //Parameters for device

                      IMetaData params = IMetaData.make();
                      
                      params.setValue("framerate", "30/1");
                      params.setValue("video_size", "1280x720");

                      // Open up the container
                      int retval = container.open(displayDevice, IContainer.Type.READ, format,
                          false, true, params, null);
                      if (retval < 0)
                      {
                        // Convert the non friendly integer return value into
                        // a slightly more friendly error name
                        IError error = IError.make(retval);
                        throw new IllegalArgumentException("could not open file: " + displayDevice + "; Error: " + error.getDescription());
                      }

                      // query how many streams found
                      int numStreams = container.getNumStreams();
                      
                      // find the first video stream
                      int videoStreamId = -1;
                      IStreamCoder videoCoder = null;
                      for(int i = 0; i < numStreams; i++)
                      {
                        // Find the stream object
                        IStream stream = container.getStream(i);
                        // Get the pre-configured decoder that can decode this stream;
                        IStreamCoder coder = stream.getStreamCoder();

                        if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO)
                        {
                          videoStreamId = i;
                          videoCoder = coder;
                          break;
                        }
                      }
                      if (videoStreamId == -1)
                        throw new RuntimeException("could not find video stream in container: "+displayDevice);

                      /*
                  * open up our decoder so it can
                  * do work.
                  */
                      if (videoCoder.open() < 0)
                        throw new RuntimeException("could not open video decoder for container: "+displayDevice);

                      IVideoResampler resampler = null;
                      if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24)
                      {
                        // if this stream is not in BGR24, we're going to need to
                        // convert it. The VideoResampler does that for us.
                        resampler = IVideoResampler.make(videoCoder.getWidth(), videoCoder.getHeight(), IPixelFormat.Type.BGR24,
                            videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
                        if (resampler == null)
                          throw new RuntimeException("could not create color space resampler for: " + displayDevice);
                      }
                      /*
                  *  we draw a window on screen
                  */
                      openJavaWindow();

                      /*
                  * Now, we start walking through the container looking at each packet.
                  */
                      IPacket packet = IPacket.make();
                      while(container.readNextPacket(packet) >= 0 && !kill)
                      {
                        /*
                  * Now we have a packet, let's see if it belongs to our video stream
                  */
                        if (packet.getStreamIndex() == videoStreamId)
                        {
                          /*
                  * We allocate a new picture to get the data out of Xuggler
                  */
                          IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(),
                              videoCoder.getWidth(), videoCoder.getHeight());

                          int offset = 0;
                          while(offset < packet.getSize())
                          {
                            /*
                  * Now, we decode the video, checking for any errors.
                  *
                  */
                            int bytesDecoded = videoCoder.decodeVideo(picture, packet, offset);
                            if (bytesDecoded < 0)
                              throw new RuntimeException("got error decoding video in: " + displayDevice);
                            offset += bytesDecoded;

                            if (picture.isComplete())
                            {
                              IVideoPicture newPic = picture;
                              /*
                  * If the resampler is not null, that means we didn't get the video in BGR24 format and
                  * need to convert it into BGR24 format.
                  */
                              if (resampler != null)
                              {
                                // we must resample
                                newPic = IVideoPicture.make(resampler.getOutputPixelFormat(), picture.getWidth(), picture.getHeight());
                                if (resampler.resample(newPic, picture) < 0)
                                  throw new RuntimeException("could not resample video from: " + displayDevice);
                              }
                              if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
                                throw new RuntimeException("could not decode video as BGR 24 bit data in: " + displayDevice);

                              // Convert the BGR24 to an Java buffered image
                              BufferedImage javaImage = Utils.videoPictureToImage(newPic);

                              // and display it on the Java Swing window
                              updateJavaWindow(javaImage);
                              if(recording){
                                                        if(!doOnce){
                                                                doOnce=true;
                                                                startTime=System.nanoTime();
                                                                //Writer to be used to record
                                                            writer=ToolFactory.makeWriter(outputFile.getAbsolutePath());
                                                                writer.addVideoStream(0, 0, IRational.make(9, 1), videoCoder.getWidth(), videoCoder.getHeight());
                                                            
                      
                                                        }
                                                        //Encode the image as TYPE_3BYTE_BGR and write to stream
                                                        BufferedImage encodedScreen=convertToType(javaImage,BufferedImage.TYPE_3BYTE_BGR);
                                                        writer.encodeVideo( 0, encodedScreen, System.nanoTime()-startTime, TimeUnit.NANOSECONDS);
                                                }
                              if(snap){
                                  File storeimage;
                                  if(outputFile.isFile()){
                                          storeimage=new File(outputFile.getParentFile().getAbsolutePath()+
                                                          System.getProperty("file.separator")+"Web Camera snap"+imageNo+".png");
                                  }
                                  else{                                  
                                                  storeimage=new File(outputFile.getAbsolutePath()+
                                                                  System.getProperty("file.separator")+"Web Camera snap"+imageNo+".png");  
                                         
                                  }
                                  
                                  BufferedImage encodedScreen=convertToType(javaImage,BufferedImage.TYPE_3BYTE_BGR);
                                  ImageIO.write(encodedScreen, "png", storeimage);
                                  imageNo++;
                                  snap=false;
                              }
                            }
                          }
                        }
                        else
                        {
                          /*
                  * This packet isn't part of video stream, so we just silently drop it.
                  */
                          do {} while(false);
                        }

                      }
                    
                      if (videoCoder != null)
                      {
                        videoCoder.close();
                        videoCoder = null;
                      }
                      if (container !=null)
                      {
                        container.close();
                        container = null;
                      }
                      closeJavaWindow();
                      if(writer !=null){
                          writer.close();  
                      }
                      
                }catch(Exception excp){
                        JOptionPane.showMessageDialog(null, excp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        excp.printStackTrace();
                }
        }
        
         private  VideoImage mScreen = null;

          private  void updateJavaWindow(BufferedImage javaImage)
          {
            mScreen.setImage(javaImage);
          }

          /**
        * Opens a window on screen.
        */
          private void openJavaWindow()
          {
            mScreen = new VideoImage();
            mScreen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            mScreen.addWindowListener(new WindowListener(){

                        @Override
                        public void windowActivated(WindowEvent arg0) {
                                // TODO Auto-generated method stub
                                
                        }

                        @Override
                        public void windowClosed(WindowEvent arg0) {
                                // TODO Auto-generated method stub
                                
                        }

                        @Override
                        public void windowClosing(WindowEvent arg0) {
                                recordStop();
                                kill=true;
                        }

                        @Override
                        public void windowDeactivated(WindowEvent arg0) {
                                // TODO Auto-generated method stub
                                
                        }

                        @Override
                        public void windowDeiconified(WindowEvent arg0) {
                                // TODO Auto-generated method stub
                                
                        }

                        @Override
                        public void windowIconified(WindowEvent arg0) {
                                // TODO Auto-generated method stub
                                
                        }

                        @Override
                        public void windowOpened(WindowEvent arg0) {
                                // TODO Auto-generated method stub
                                
                        }
                
            });
          }

     private void closeJavaWindow(){
                mScreen.dispose();
        }
        
        public void recordStart(File out){
                outputFile=out;
                recording=true;
        }
        
        public void recordStop(){
                recording=false;
                doOnce=false;
        }
        
        public void snap(File out){
                snap=true;
                outputFile=out;
        }
        
        public void kill(){
                kill=true;
        }
        /**
         * convertToType
         * <p>
         * Convert the screen {@link BufferedImage} to required {@link BufferedImage} type for encoding
         * @param screen Image to be converted
         * @param targetType Image to be converted to targetType
         * @return
         */
        private BufferedImage convertToType(BufferedImage screen, int targetType) {
                BufferedImage returnimage;
                
                if(screen.getType()==targetType){
                        returnimage=screen;
                }
                else{
                        returnimage=new BufferedImage(screen.getWidth(),screen.getHeight(),targetType);
                        returnimage.createGraphics().drawImage(screen, 0, 0, null);
                }
                
                return returnimage;
        }

}

package uk.co.darkerwaters;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImagePanel extends JPanel{
	private static final long serialVersionUID = 5361662653638618359L;
	private BufferedImage image = null;
	
	private ImageFile currentImage = null;

    public ImagePanel() {
       
    }
    public synchronized void setImage(final ImageFile imageFile) {
    	if (null != imageFile) {
    		if (null == this.currentImage ||
    			false == this.currentImage.file.equals(imageFile.file)) {
    			// this is a change, load it
    			Thread loadingThread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							BufferedImage loadedImage = ImageIO.read(imageFile.file);
				            imageLoaded(imageFile, loadedImage);
				         } catch (IOException e) {
				        	 Main.error.append("Failed to read image " + imageFile.getName() + ": " + e.getMessage());
				         }
					}
				});
    			loadingThread.run();
    		}
    	}
    }
    	
    private synchronized void imageLoaded(ImageFile loadedFile, BufferedImage loadedImage) {
    	if (this.currentImage == null || this.currentImage.file.equals(loadedFile.file)) {
    		// this is the correct one
    		if (null != this.image) {
				this.image.flush();
			}
    		this.image = loadedImage;
	        ImagePanel.this.updateUI();
	     }
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null); // see javadoc for more info on the parameters            
    }

}
package uk.co.darkerwaters;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultCaret;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter.ExifOverflowException;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata.GPSInfo;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;

/*
 * FileChooserDemo.java uses these files:
 *   images/Open16.gif
 *   images/Save16.gif
 */
public class Main extends JPanel implements ActionListener {
    private static final long serialVersionUID = 5770429033884005460L;
	
    static private final String newline = "\n";
	private static SimpleDateFormat filenameDateFormat = new SimpleDateFormat("yyyy-MM-dd HHmmss");
	private static final SimpleDateFormat K_ASCII_EXIF_DATE = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
	
	private boolean isCancelProcessing = false;
	private boolean isImageManipulating = false;
	private int fileProcessing = -1;
	
    JButton openButton, cancelButton, goButton;
    JCheckBox locateCheck, nameCheck, dateCheck;
    JTextArea filenameFormatText;
    JTextArea log;
    JTextArea error;
    JFileChooser fc;
    JProgressBar progress;
    
    public class ImagePanel extends JPanel{
		private static final long serialVersionUID = 5361662653638618359L;
		private BufferedImage image = null;

        public ImagePanel() {
           
        }
        public void setImage(ImageFile imageFile) {
        	try {                
        		if (null != image) {
        			image.flush();
        			image = null;
        		}
                image = ImageIO.read(imageFile.file);
                this.updateUI();
             } catch (IOException e) {
                  error.append("Failed to read image " + imageFile.getName() + ": " + e.getMessage());
             }
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null); // see javadoc for more info on the parameters            
        }

    }
    
    private static class GpxTrack {
    	String name;
    	Date startTimeLocal;
    	Date endTimeLocal;
    	int timeOffset;
    	Trackpoint[] track;
		public String timezoneString;
    	
    	@Override
    	public String toString() {
    		return name + 
    				" starting at " + filenameDateFormat.format(startTimeLocal) +
    				" in " + timezoneString + 
    				" with " + (timeOffset / (60 * 60 * 1000)) + "hr offset"; 
    	}
    }
    
    private static class ImageFile {
    	File file;
		Boolean isLocationSet = null;
    	
    	public ImageFile(File file) {
			this.file = file;
		}

		@Override
    	public String toString() {
    		return file.getName() + 
    				(this.isLocationSet == null ? "" : (this.isLocationSet ? " located" : " not located")); 
    	}

		public String getName() {
			return this.file.getName();
		}
    }
    
    DefaultListModel<ImageFile> imageListModel = new DefaultListModel<ImageFile>() {
		private static final long serialVersionUID = 1038301917406489264L;
    };
    DefaultListModel<GpxTrack> trackListModel = new DefaultListModel<GpxTrack>() {
		private static final long serialVersionUID = -1428733837511891224L;
    };
    
    private JList<ImageFile> imageList;
    private JList<GpxTrack> trackList;
    private ImagePanel previewImage;

    public Main() {
        super(new BorderLayout());

        //Create the log first, because the action listeners
        //need to refer to it.
        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        DefaultCaret caret = (DefaultCaret) log.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        error = new JTextArea(5,20);
        error.setMargin(new Insets(5,5,5,5));
        error.setEditable(false);
        caret = (DefaultCaret) error.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        filenameFormatText = new JTextArea(1, 1);
        filenameFormatText.setMargin(new Insets(5,5,5,5));
        filenameFormatText.setEditable(true);
        filenameFormatText.setText(filenameDateFormat.toPattern());

        //Create a file chooser
        fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        //Create the open button.
        openButton = new JButton("Open a File / Directory...", createImageIcon("images/Open16.gif"));
        openButton.addActionListener(this);

        //Create the check boxes
        locateCheck = new JCheckBox("Locate images", true);
        nameCheck = new JCheckBox("Set names", true);
        dateCheck = new JCheckBox("Set datess", true);

        //Create the cancel button.
        cancelButton = new JButton("Cancel / Clear...", createImageIcon("images/Cancel16.gif"));
        cancelButton.addActionListener(this);
        
        //create the GO button
        goButton = new JButton("GO...", createImageIcon("images/Go16.gif"));
        goButton.addActionListener(this);

        //For layout purposes, put the buttons in a separate panel
        JPanel topPanel = new JPanel(new GridLayout(3, 0));
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(openButton);
        buttonPanel.add(locateCheck);
        buttonPanel.add(nameCheck);
        buttonPanel.add(dateCheck);
        buttonPanel.add(goButton);
        buttonPanel.add(cancelButton);
        
        JPanel editPanel = new JPanel(); //use FlowLayout
        editPanel.add(new JLabel("Filename Format:"));
        editPanel.add(filenameFormatText);
        
        topPanel.add(buttonPanel);
        topPanel.add(editPanel);
        
        progress = new JProgressBar();
        topPanel.add(progress);
        
        //create the image list
        imageList = new JList<ImageFile>(imageListModel);
        imageList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    List<ImageFile> selectedValuesList = imageList.getSelectedValuesList();
                    if (false == selectedValuesList.isEmpty()) {
                    	previewImage.setImage(selectedValuesList.get(0));
                    }
                }
            }
        });
        // create the track list
        trackList = new JList<GpxTrack>(trackListModel);
        trackList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    List<GpxTrack> selectedValuesList = trackList.getSelectedValuesList();
                    System.out.println(selectedValuesList);
                }
            }
        });
        // create the preview image
        this.previewImage = new ImagePanel();
        //For layout purposes, put the lists in a separate panel
        JPanel listPanel = new JPanel(new GridLayout(0, 2));
        listPanel.add(new JScrollPane(imageList));
        JPanel rightListPanel = new JPanel(new GridLayout(2, 1));
        rightListPanel.add(new JScrollPane(trackList));
        rightListPanel.add(previewImage);
        listPanel.add(rightListPanel);
        
        //For layout purposes, put the logs in a separate panel
        JPanel logPanel = new JPanel(new GridLayout(0, 2));
        logPanel.add(new JScrollPane(log));
        logPanel.add(new JScrollPane(error));

        //Add the buttons and the log to this panel.
        add(topPanel, BorderLayout.PAGE_START);
        add(listPanel, BorderLayout.CENTER);
        add(logPanel, BorderLayout.PAGE_END);
        
        setSize(1200, 1600);
    }

    public void actionPerformed(ActionEvent e) {
    	
    	String text = filenameFormatText.getText();
    	
    	try {
    		SimpleDateFormat testFormat = new SimpleDateFormat(text);
    		filenameDateFormat = testFormat;
    	}
    	catch (Exception e1) {
    		error.append("Failed to parse the text \"" + text + "\" as a simple date format: " + e1.getMessage());
    	}
    	

        //Handle open button action.
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(Main.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
            	isCancelProcessing = false;
            	performFileProcessing(new ImageFile(fc.getSelectedFile()));
            } else {
                log.append("Open command cancelled by user." + newline);
            }

        //Handle locate button action.
        } else if (e.getSource() == goButton) {
        	isCancelProcessing = false;
        	performImageProcessing();
        } else if (e.getSource() == cancelButton) {
        	if (this.isImageManipulating || this.fileProcessing > -1) {
        		this.isCancelProcessing = true;
        	}
        	else {
        		this.imageListModel.removeAllElements();
	        	this.trackListModel.removeAllElements();
	        	this.log.removeAll();
	        	this.log.setText("Cleared..." + newline);
	        	this.error.removeAll();
	        	this.error.setText("Cleared..." + newline);
	        	this.progress.setValue(0);
        	}
        }
    }

    private void processFile(ImageFile imageFile) {
    	this.progress.setValue(this.fileProcessing++);
    	if (this.fileProcessing > this.progress.getMaximum()) {
    		this.fileProcessing = this.progress.getMinimum();
    	}
    	if (null == imageFile || null == imageFile.file) {
    		error.append("Error: null file." + newline);
    	}
    	else if (imageFile.file.isHidden()) {
    		// ignore this
    	}
    	else if (imageFile.file.isFile()) {
    		// process the image / gpx file
    		String mimeType = new MimetypesFileTypeMap().getContentType(imageFile.file);
		    // mimeType should now be something like "image/png"
    		mimeType = URLConnection.guessContentTypeFromName(imageFile.file.getAbsolutePath());
			
		    if (mimeType != null && mimeType.substring(0,5).equalsIgnoreCase("image")){
		    	//its an image
		    	log.append("Processing: image " + imageFile.getName() + newline);
		    	processImageFile(imageFile);
		    }
		    else {
		    	// check the extension
		    	String filename = imageFile.getName();
		    	int dotIndex = filename.lastIndexOf('.');
		    	if (dotIndex >= 0) {
		    		String extension = filename.substring(dotIndex, filename.length()) ;
		    		if (extension.equalsIgnoreCase(".gpx") || extension.equalsIgnoreCase(".tcx")) {
		    			log.append("Processing: track " + imageFile.getName() + newline);
		    			processTrackFile(imageFile.file, extension);
		    		}
		    		else if (extension.equalsIgnoreCase(".mp4") || 
		    				 extension.equalsIgnoreCase(".mov")) {
		    			log.append("Processing: movie " + imageFile.getName() + newline);
		    			processImageFile(imageFile);
		    		} else {
		    			// check for a filename matching our string
		    			Date filenameDate = getFilenameDate(imageFile);
		    			if (null != filenameDate) {
		    				// this is something worth processing
		    				log.append("Processing: file " + imageFile.getName() + newline);
		    				processImageFile(imageFile);
		    			}
		    		}
		    	}
		    }
    	}
    	else if (imageFile.file.isDirectory()) {
    		// process all the child files
    		log.append("Processing: directory " + imageFile.getName() + "." + newline);
    		for (File child : imageFile.file.listFiles()) {
    			if (false == this.isCancelProcessing) {
    				processFile(new ImageFile(child));
    			}
    		}
    	}
	}

	private void processImageFile(ImageFile file) {
		// remeber to process later
		imageListModel.addElement(file);
		this.imageList.ensureIndexIsVisible(this.imageListModel.size() - 1);
	}
    
    private void performFileProcessing(final ImageFile file) {
		this.progress.setMinimum(0);
		this.progress.setMaximum(10);
    	Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
		    	Main.this.fileProcessing = 0;
				processFile(file);
				// done
				Main.this.progress.setValue(0);
				Main.this.fileProcessing = -1;
			}
		});
    	thread.start();
    }
    
    private void performImageProcessing() {
    	this.isImageManipulating = true;
    	final boolean isLocate = locateCheck.isSelected();
    	final boolean isDate = dateCheck.isSelected();
    	final boolean isName = nameCheck.isSelected();
    	
		this.progress.setMinimum(0);
		final ImageFile[] toProcess = new ImageFile[this.imageListModel.size()];
		for (int i = 0; i < toProcess.length; ++i) {
			toProcess[i] = this.imageListModel.get(i);
		}
		this.progress.setMaximum(toProcess.length);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				int count = 0;
				for (ImageFile imageFile : toProcess) {
					if (Main.this.isCancelProcessing) {
						// cancel and clear everything
						Main.this.isCancelProcessing = false;
						Main.this.isImageManipulating = false;
						break;
					}
					if (isLocate) {
						locateImage(imageFile);
					}
					if (isDate) {
						setFileDate(imageFile);
					}
					if (isName) {
						setFileName(imageFile);
					}
					// update the progress
					Main.this.progress.setValue(++count);
				}
				Main.this.isImageManipulating = false;
				Main.this.progress.setValue(0);
			}
		});
		thread.start();
    }
    
    private void setFileName(ImageFile imageFile) {
		try {
			// get the name of this file
			Date fileDate = getFileDate(imageFile);
			if (null != fileDate) {
				// reset the name on the image file
				String newFilename = filenameDateFormat.format(fileDate);
				String oldFilename = imageFile.getName();
				String filePath = imageFile.file.getAbsolutePath();
				filePath = filePath.substring(0, filePath.length() - oldFilename.length());
				int dotIndex = oldFilename.lastIndexOf('.');
				String extension = "";
		    	if (dotIndex >= 0) {
		    		extension = oldFilename.substring(dotIndex, oldFilename.length()) ;
		    	}
				if (oldFilename.startsWith(newFilename)) {
					// this is no change, so don't change
					log.append("New filename " + newFilename + " is not different enough from current " + oldFilename + " to bother changing." + newline);
				}
				else {
					// change the filename
					File newFile;
					int filenameCounter = 0;
					do {
						String actualFilename = newFilename;
						actualFilename += (filenameCounter <= 0 ? "" : String.format("(%03d)", filenameCounter)) + extension;
						newFile = new File(filePath + actualFilename);
						++filenameCounter;
					} while (newFile.exists() && filenameCounter < 999);
					try {
						imageFile.file.renameTo(newFile);
						imageFile.file = newFile;
						log.append("File " + oldFilename + " renamed to " + newFilename + newline);
					}
					catch (Exception fileEx) {
						error.append("New filename " + newFilename + " couldn't be set: " + fileEx.getMessage() + newline);
					}
				}
			}
		} catch (Exception e) {
			error.append("Error: " + e.getMessage() + "." + newline);
		}
	}
    
    private void setFileDate(ImageFile imageFile) {
		try {
			// get the dates of this file
			Date fileDate = getFileDate(imageFile);
			if (null != fileDate) {
				// reset the times on the image file
				imageFile.file.setLastModified(fileDate.getTime());
				BasicFileAttributeView attributes = Files.getFileAttributeView(Paths.get(imageFile.file.getPath()), BasicFileAttributeView.class);
				FileTime time = FileTime.fromMillis(fileDate.getTime());
		        attributes.setTimes(time, time, time);
		        // log the success
		        log.append("Reset file date for " + imageFile.getName() + "" + newline);
			}
		} catch (Exception e) {
			error.append("Error: " + e.getMessage() + "." + newline);
		}
	}
	
	private void locateImage(ImageFile imageFile) {
		try {
			// get the dates of this file
			long lastModified = imageFile.file.lastModified();
			Date fileDate = getFileDate(imageFile);
			// if this has a location?
			Trackpoint location = getGpxLocation(fileDate);
			if (null != location) {
				/*//TODO remove this temp check for location differences
				Trackpoint gpsLocation = getGpsLocation(imageFile);
				if (null == gpsLocation) {
					// different
					error.append("Different locations " + gpsLocation + " -- " + location + newline);
				}
				else if (Math.round(gpsLocation.getLatitude() * 10000.0) != Math.round(location.getLatitude() * 10000.0) ||
						Math.round(gpsLocation.getLongitude() * 10000.0) != Math.round(location.getLongitude() * 10000.0)) {
					// different
					error.append("Different locations " + gpsLocation + " -- " + location + newline);
				}
				*/
				// create a temp file to put the image to change into
				File tempFile = File.createTempFile(imageFile.getName(), "jpg");
				tempFile.delete();
				// move the image to change here
				if (false == moveFile(imageFile.file, tempFile)) {
					error.append("Error: failed to move the image " + imageFile.getName() + "." + newline);
				}
				else {
					// create the new file with the new exif data
					try {
						changeExifMetadata(tempFile, imageFile.file, location);
					}
					catch (Exception e) {
						// failed to create exif data, just put the file back
						imageFile.isLocationSet = false;
						if (false == moveFile(tempFile, imageFile.file)) {
							throw new Exception("Failed to rename temp file back to image file, lost " + imageFile.getName());
						}
						throw new Exception("Failed to create exif data for " + imageFile.getName() + ": " + e.getMessage());
					}
					// bin the original file, first just rename it so we can recover
					if (false == tempFile.delete()) {
						error.append("Error: failed to delete the temp image " + tempFile.getName() + "." + newline);
					}
					// reset the times on the image file
					imageFile.file.setLastModified(lastModified);
					BasicFileAttributeView attributes = Files.getFileAttributeView(Paths.get(imageFile.file.getPath()), BasicFileAttributeView.class);
					FileTime time = FileTime.fromMillis(lastModified);
			        attributes.setTimes(time, time, time);
			        // log the success
			        log.append("Located: added location " + location.getLatitude() + " : " + location.getLongitude() + " data to " + imageFile.getName() + newline);
			        imageFile.isLocationSet = true;
				}
			}
		} catch (Exception e) {
			error.append("Error: " + e.getMessage() + "." + newline);
		}
	}

	private boolean moveFile(File srcFile, File destFile) {
		boolean isSuccess = false;
		try {
			FileUtils.moveFile(srcFile, destFile);
			isSuccess = true;
		}
		catch (IOException e) {
			if (e instanceof FileExistsException) {
				destFile.delete();
				try {
					FileUtils.moveFile(srcFile, destFile);
					isSuccess = true;
				}
				catch (IOException e2) {
					// try another method
					isSuccess = srcFile.renameTo(destFile);
				}
			}
			else {
				// try another method
				isSuccess = srcFile.renameTo(destFile);
			}
		}
		return isSuccess;
	}

	private Trackpoint getGpxLocation(Date locationDate) {
		for (int i = 0; i < this.trackListModel.getSize(); ++i) {
			GpxTrack track = this.trackListModel.get(i);
			if (track.startTimeLocal.before(locationDate) ||
				track.startTimeLocal.equals(locationDate)) {
				// this track starts before
				if (track.endTimeLocal.after(locationDate) ||
					track.endTimeLocal.equals(locationDate)) {
					// this track ends after, this track covers this location
					Trackpoint closestPoint = null;
					for (Trackpoint point : track.track) {
						// for each point, find the time that is closest without going over
						if (point.getTime().after(locationDate)) {
							// gone over
							break;
						}
						else {
							closestPoint = point;
						}
					}
					if (closestPoint != null) {
						return closestPoint;
					}
				}
			}
		}
		// no valid track / point
		return null;
	}

	private void processTrackFile(File file, String extension) {
		// add this track data
		Trackpoint[] track = null;
		if (extension.equalsIgnoreCase(".gpx")) {
			try {
				track = GpxReader.readTrack(file);
			} catch (IOException e) {
				error.append("Error: " + e.getMessage() + "." + newline);
			}
		}
		else if (extension.equalsIgnoreCase(".tcx")) {
			try {
				track = TcxReader.readTrack(file);
			} catch (IOException e) {
				error.append("Error: " + e.getMessage() + "." + newline);
			}
		}
		if (null != track && track.length > 0) {
			// add the the list of tracks
			GpxTrack gpxTrack = new GpxTrack();
			// and work out the timezone offset
			gpxTrack.name = file.getName();
			gpxTrack.timeOffset = 0;
			gpxTrack.timezoneString = TimezoneMapper.latLngToTimezoneString(track[0].getLatitude(), track[0].getLongitude());
			if (null != gpxTrack.timezoneString) {
				TimeZone timeZone = TimeZone.getTimeZone(gpxTrack.timezoneString);
				if (null != timeZone) {
					gpxTrack.timeOffset = timeZone.getOffset(track[0].getTime().getTime());
				}
			}
			
			// offset all the track times to the local filetime string
			for (Trackpoint point : track) {
				long pointTime = point.getTime().getTime() + gpxTrack.timeOffset;
				String newTimeString = filenameDateFormat.format(new Date(pointTime));
				try {
					point.setTime(filenameDateFormat.parse(newTimeString));
				} catch (ParseException e) {
					error.append("Error: " + e.getMessage() + "." + newline);
				}
			}
			// this is the track now
			gpxTrack.track = track;
			gpxTrack.startTimeLocal = track[0].getTime();
			gpxTrack.endTimeLocal = track[track.length - 1].getTime();
			// add to the list
			this.trackListModel.addElement(gpxTrack);
			this.trackList.ensureIndexIsVisible(this.trackListModel.size() - 1);
			// and log this action
			log.append("Added: track starting " + filenameDateFormat.format(gpxTrack.startTimeLocal) + "." + newline);
		}
	}
	
	public Date getFileDate(final ImageFile file) {
		Date fileDate = null;
		try {
			fileDate = getExifDate(file);
			if (null == fileDate) {
				// there is no EXIF date, try the name
				fileDate = getFilenameDate(file);
				if (null == fileDate) {
					// there is no filename date, try the file
					fileDate = new Date(file.file.lastModified());
				}
			}
		}
		catch (Exception e) {
			error.append("Error getting date for \"" + (file == null ? "null" : file.getName()) + "\": "+ e.getMessage() + "." + newline);
		}
		
		return fileDate;
	}
	
	public Date getExifDate(final ImageFile jpegImageFile) {
		Date foundDate = null;

        // note that metadata might be null if no metadata is found.
		IImageMetadata metadata = null;
		JpegImageMetadata jpegMetadata = null;
		try {
			metadata = Imaging.getMetadata(jpegImageFile.file);
			jpegMetadata = (JpegImageMetadata) metadata;
		} catch (ImageReadException | IOException e) {
			// failed, this is OK, catching with the null check
		}
		// assume no EXIF and no GPS
		jpegImageFile.isLocationSet = false;
		if (null != jpegMetadata) {
            // note that exif might be null if no Exif metadata is found.
            final TiffImageMetadata exif = jpegMetadata.getExif();

            try {
	            if (null != exif) {
	            	// while we are here, check our GPS coordinates
	            	jpegImageFile.isLocationSet = null != exif.getGPS();
	            	// now check for the time from this data
		        	TiffField field = exif.findField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
		        	if (null == field) {
		        		field = exif.findField(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
		        	}
					if (null != field) {
						// get the date from this
						Object value = field.getValue();
						try {
							if (null != value && value.toString().matches("\\d{4}:\\d{2}:\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
								foundDate = K_ASCII_EXIF_DATE.parse(value.toString());
							}
						} catch (Exception e) {
							error.append("Error: Failed to parse EXIF date " + e + newline);
						}
					}
	            }
            }
            catch (ImageReadException e) {
    			// failed, this is OK, catching with the null check
            }
        }
		return foundDate;
    }
	
	public Trackpoint getGpsLocation(final ImageFile jpegImageFile) {
		// note that metadata might be null if no metadata is found.
		IImageMetadata metadata = null;
		JpegImageMetadata jpegMetadata = null;
		try {
			metadata = Imaging.getMetadata(jpegImageFile.file);
			jpegMetadata = (JpegImageMetadata) metadata;
		} catch (ImageReadException | IOException e) {
			// failed, this is OK, catching with the null check
		}
		if (null != jpegMetadata) {
            // note that exif might be null if no Exif metadata is found.
            final TiffImageMetadata exif = jpegMetadata.getExif();

            try {
	            if (null != exif) {
	            	// while we are here, check our GPS coordinates
	            	GPSInfo gps = exif.getGPS();
	            	if (null != gps) {
	            		return Trackpoint.fromWGS84(gps.getLatitudeAsDegreesNorth(), gps.getLongitudeAsDegreesEast(), 0, null);
	            	}
	            }
            }
            catch (ImageReadException e) {
    			// failed, this is OK, catching with the null check
            }
        }
		return null;
    }
    
    private Date getFilenameDate(ImageFile file) {
    	String filename = file.getName();
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex >= 0) {
			filename = filename.substring(0, dotIndex);
		}
		Date fileDate = null;
		String pattern = filenameDateFormat.toPattern();
		if (filename.length() > pattern.length()) {
			// just the numbers please
			filename = filename.substring(0, pattern.length());
		}
		// this is a nice date arranged, parse this date
		try {
			fileDate = filenameDateFormat.parse(filename);
		} catch (ParseException e) {
			// ignore this, handling by retuning null
		}
		return fileDate;
    }
	
	public void changeExifMetadata(final File jpegImageFile, final File dst, final Trackpoint imageLoc) throws IOException, ImageReadException, ImageWriteException {
		OutputStream os = null;
        try {
            TiffOutputSet outputSet = null;

            // note that metadata might be null if no metadata is found.
            final IImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata) {
                // note that exif might be null if no Exif metadata is found.
                final TiffImageMetadata exif = jpegMetadata.getExif();

                if (null != exif) {
                    // TiffImageMetadata class is immutable (read-only).
                    // TiffOutputSet class represents the Exif data to write.
                    //
                    // Usually, we want to update existing Exif metadata by
                    // changing
                    // the values of a few fields, or adding a field.
                    // In these cases, it is easiest to use getOutputSet() to
                    // start with a "copy" of the fields read from the image.
                    outputSet = exif.getOutputSet();
                }
            }

            // if file does not contain any exif metadata, we create an empty
            // set of exif metadata. Otherwise, we keep all of the other
            // existing tags.
            if (null == outputSet) {
                outputSet = new TiffOutputSet();
            }

            try {
            	// set our GPS co-ordinates
            	outputSet.setGPSInDegrees(imageLoc.getLongitude(), imageLoc.getLatitude());
            }
            catch (Exception e) {
            	e.printStackTrace();
            	throw e;
            }
            
            os = new FileOutputStream(dst);
            os = new BufferedOutputStream(os);
            try {
            	// update the image file with the new location data
            	new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, outputSet);
            }
            catch (ExifOverflowException e) {
            	// the segment is 'too large' shrink it?
            	final TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
                // make sure to remove old value if present (this method will
                // not fail if the tag does not exist).
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_AFCP_IPTC);
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_ANNOTATIONS); 
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_APERTURE_VALUE); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_APPLICATION_NOTES); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_BACKGROUND_COLOR_INDICATOR); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_BACKGROUND_COLOR_VALUE);
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_BITS_PER_EXTENDED_RUN_LENGTH); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_BITS_PER_RUN_LENGTH); 
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_BRIGHTNESS); 
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_BRIGHTNESS_VALUE); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_CFAPATTERN );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_COLOR_CHARACTERIZATION); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_COLOR_SEQUENCE );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_COLOR_TABLE );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_COMPONENTS_CONFIGURATION); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_COMPRESSED_BITS_PER_PIXEL );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_CONTRAST_1 );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_CONTRAST_2 );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_CONVERTER );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_CUSTOM_RENDERED );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATA_TYPE );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DEVICE_SETTING_DESCRIPTION );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DIGITAL_ZOOM_RATIO );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_EXIF_IMAGE_LENGTH );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_EXIF_IMAGE_WIDTH );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_EXIF_OFFSET );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_EXIF_VERSION );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_EXPOSURE );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_EXPOSURE_COMPENSATION); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_EXPOSURE_INDEX_EXIF_IFD );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_EXPOSURE_MODE );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_EXPOSURE_PROGRAM); 
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_EXPOSURE_TIME );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_FILE_SOURCE );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_FLASH );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_FLASH_ENERGY_EXIF_IFD); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_FLASHPIX_VERSION );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_FNUMBER );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_FOCAL_LENGTH); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_FOCAL_LENGTH_IN_35MM_FORMAT); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_FOCAL_PLANE_RESOLUTION_UNIT_EXIF_IFD); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_FOCAL_PLANE_XRESOLUTION_EXIF_IFD );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_FOCAL_PLANE_YRESOLUTION_EXIF_IFD );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_GAIN_CONTROL );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_GAMMA );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_GPSINFO );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_HCUSAGE); 
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_IMAGE_COLOR_INDICATOR); 
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_IMAGE_COLOR_VALUE );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_IMAGE_DEPTH );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_IMAGE_HISTORY );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_IMAGE_NUMBER );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_IMAGE_UNIQUE_ID); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_INTERGRAPH_FLAG_REGISTERS); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_INTERGRAPH_PACKET_DATA );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_INTEROP_OFFSET );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_INTEROPERABILITY_INDEX); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_INTEROPERABILITY_VERSION); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_IPTC_NAA );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_ISO );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_IT8HEADER); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_JPG_FROM_RAW_LENGTH_IFD2); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_JPG_FROM_RAW_LENGTH_SUB_IFD); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_JPG_FROM_RAW_LENGTH_SUB_IFD2 );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_JPG_FROM_RAW_START_IFD2 );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_JPG_FROM_RAW_START_SUB_IFD); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_JPG_FROM_RAW_START_SUB_IFD2 );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_LEAF_DATA );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_LEAF_SUB_IFD); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_LENS );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_LIGHT_SOURCE); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_MAKER_NOTE );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_MATTEING );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_MAX_APERTURE_VALUE); 
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_METERING_MODE );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_MODEL_2 );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_MOIRE_FILTER); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_NOISE_2 );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_OFFSET_SCHEMA); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_OPTO_ELECTRIC_CONV_FACTOR); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_OTHER_IMAGE_LENGTH );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_OTHER_IMAGE_START );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_OWNER_NAME );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_PHOTOSHOP_SETTINGS); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_PIXEL_INTENSITY_RANGE); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_PREVIEW_IMAGE_LENGTH_IFD0); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_PREVIEW_IMAGE_LENGTH_MAKER_NOTES); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_PREVIEW_IMAGE_LENGTH_SUB_IFD1 );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_PREVIEW_IMAGE_START_IFD0 );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_PREVIEW_IMAGE_START_MAKER_NOTES); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_PREVIEW_IMAGE_START_SUB_IFD1 );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_PRINT_IM );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_PROCESSING_SOFTWARE); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_RASTER_PADDING );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_RAW_FILE );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_RELATED_SOUND_FILE); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SATURATION_1 );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SATURATION_2 );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SCENE_CAPTURE_TYPE); 
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SCENE_TYPE );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SECURITY_CLASSIFICATION); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SEMINFO );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SENSING_METHOD_EXIF_IFD); 
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SERIAL_NUMBER );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SHADOWS );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SHARPNESS_1); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SHARPNESS_2 );
				//exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SITE );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SMOOTHNESS); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SOFTWARE );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SPATIAL_FREQUENCY_RESPONSE_2); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SPECTRAL_SENSITIVITY );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_STO_NITS );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SUB_SEC_TIME); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SUB_SEC_TIME_DIGITIZED); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SUB_SEC_TIME_ORIGINAL );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SUBJECT_DISTANCE); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SUBJECT_DISTANCE_RANGE );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_TIFF_EPSTANDARD_ID_2); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_TILE_DEPTH );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_TRANSPARENCY_INDICATOR); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_USER_COMMENT );
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_WHITE_BALANCE_1); 
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_WHITE_BALANCE_2 );
            	outputSet.setGPSInDegrees(imageLoc.getLongitude(), imageLoc.getLatitude());
            	// create the new stream
            	os = new FileOutputStream(dst);
                os = new BufferedOutputStream(os);
            	new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, outputSet);
            }
        } finally {
        	if (null != os) {
        		os.close();
        	}
            //IoUtils.closeQuietly(canThrow, os);
        }
    }

	/** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = Main.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Holiday File Thingiser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new Main());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE); 
                createAndShowGUI();
            }
        });
    }
}

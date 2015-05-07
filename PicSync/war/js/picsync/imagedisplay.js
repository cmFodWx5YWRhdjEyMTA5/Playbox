//declare the module
PicSync.Display = (function () {

	var containerWidth = 100;

	var fileProcessingIndex = 0;
	var filesProcessing = null;
	var imageObjectsToFix = null;
	
	var public = {};
	
	public.showMainImage = function(imageObject) {
		// show the main image file for this object
		if (imageObject) {
			public.showMainImageFile(imageObject.file);
		}
		else {
			public.showMainImageFile(null);
		}
	}
	
	public.showMainImageFile = function(imageFile) {
		// clear the current image
		var imagePanel = document.getElementById('image_panel');
		while (imagePanel.firstChild) {
			imagePanel.removeChild(imagePanel.firstChild);
		}
		if (imageFile) {
			loadImage(
				imageFile,
			    function (img) {
					if (!img || img.type == "error") {
						// create the error image
						img = document.createElement('img');
						img.setAttribute("src", "/images/thumbnail.png");
					}
					img.className = "mainImage";
					img.setAttribute("title", escape(imageFile.name));
					imagePanel.appendChild(img);
					// and create a nice information title
					var titleDiv = document.createElement('div');
					titleDiv.className = "mainImageTitle";
					titleDiv.textContent = escape(imageFile.name);
					imagePanel.appendChild(titleDiv);
			    },
			    {
			        maxHeight: imagePanel.offsetHeight,
			        minHeight: imagePanel.offsetHeight / 2,
			        contain: true,
			        orientation: true,
			        canvas: true
			    }
			);
		}
	}
	
	public.showHelpImage = function() {
		// clear the current image
		var imagePanel = document.getElementById('image_panel');
		while (imagePanel.firstChild) {
			imagePanel.removeChild(imagePanel.firstChild);
		}
		var img = document.createElement('img');
		img.setAttribute("src", "/images/instructions.png");
		img.className = "mainImage";
		imagePanel.appendChild(img);
	}
	
	public.showImagesLoaded = function() {
		// go through the list of images and ensure they are all shown as thumbnails
		// so create a list of files to send to the function
		var files = [];
		var images_loaded = getImagesLoaded();
		for (var i = 0; i < images_loaded.length; ++i) {
			var imageObject = images_loaded[i];
			if (null != imageObject) {
				files.push(imageObject.file);
			}
		}
		// show the thumbnails for all of these
		public.showFileThumbnails(files, null);
	}

	public.showFileThumbnails = function(files, cameraObject) {
		if (null == files || files.length == 0) {
			return;
		}
		// start the display of progress of this operation
		PicSync.Progress.startProgress();
		// Loop through the FileList and render image files as thumbnails.
		fileProcessingIndex = -1;
		filesProcessing = files;
		imageObjectsToFix = [];
		// process the first file, the call-back function will process the next, and so on...
		processFileRecursive(cameraObject);
	}

	processFileRecursive = function(cameraObject) {
		if (++fileProcessingIndex < filesProcessing.length) {
			// process this file
			var f = filesProcessing[fileProcessingIndex];
			var progressText = "";
			if (null != f) {
				progressText = f.name;
			}
			PicSync.Progress.updateProgress(fileProcessingIndex + 1, filesProcessing.length, "Importing " + progressText);
			if (false == getIsImageLoaded(f)) {
				// the image is not loaded, process this
				if (null != cameraObject) {
					// we know the camera to which this applies, create this
					addTimelineImageFileForCamera(f, cameraObject, cameraObject.id, f.lastModifiedDate, "./images/thumbnail.png");
				}
				else if (null != f && (f.type.match('image.*'))) {
					// the file is an image does not exist in the list yet, load it into a thumbnail, first we have to			
					// read the EXIF data here to get the actual size, so can calculate the width and position of the image
					loadImage.parseMetaData(
					    f,
					    function (data) {
					        if (!data.imageHead) {
					        	// no exif data, add to the fix list
					        	var fixingObject = addFixImageFile(f);
								// remember this to try to fix it once all the images are loaded
								imageObjectsToFix.push(fixingObject);
					        }
					        else {
						        addTimelineImageFile(f, data);
					        }
							// and call the function recursively
							processFileRecursive(cameraObject);
					    },
					    {
					        maxMetaDataSize: 262144,
					        disableImageHead: false
					    }
					);
				}
				else {
					// add to the list of images to fix
					var fixingObject = addFixImageFile(f);
					// remember this to try to fix it once all the images are loaded
					imageObjectsToFix.push(fixingObject);
					// and move on
					processFileRecursive(cameraObject);
				}
			}
			else {
				// the image is already loaded, just move onto the next one
				processFileRecursive(cameraObject);
			}
		}
		else {
			// this is the end of the processing of files, here we can take a look at all the
			// files we want to fix and see if any match the data of files in the image list.
			// these are RAW partner files to image files and can just be handled alongside
			// the image to which they refer...
			autoFixFiles();
			// and clear our data to release the lists
			filesProcessing = null;
			imageObjectsToFix = null;
		}
	}
	
	addTimelineImageFile = function(f, data) {
		// get all the data for this image file and create a nice representation
		var orientation = PicSync.TimeSync.getExifOrientation(data.exif);
        var thumbnail = PicSync.TimeSync.getExifThumbnail(data.exif);
        // get the date for this image, and the camera ID
        var imageDate = PicSync.TimeSync.getExifImageDate(data.exif, f);
        var cameraId = PicSync.TimeSync.getExifCameraId(data.exif);
		// find the camera object
		var cameraObject = PicSync.TimeSync.getExifCamera(cameraId);
		// and add the thumbnail and object representation for this
		return addTimelineImageFileForCamera(f, cameraObject, cameraId, imageDate, thumbnail);
	}
	
	addTimelineImageFileForCamera = function(f, cameraObject, cameraId, imageDate, thumbnail) {
		// get the data we need and construct the object and thumbnail representation div
		var imageDateIncOffset = PicSync.TimeSync.offsetImageDate(imageDate, cameraId);
		var color = 'grey';
		if (null != cameraObject) {
			color = cameraObject.color;
		}
		var titleText = PicSync.TimeSync.getExifFilename(imageDateIncOffset);
        // create the representation
		var reps = createImageRepresentation(f, "thumbnailImagePanel", titleText, color, thumbnail);
		var imageObject = reps[0];
		var thumb = reps[1];
		// set the date data on this object
		imageObject["cameraId"] = cameraId;
		imageObject["imageDate"] = imageDate;
		imageObject["imageDateOffset"] = imageDateIncOffset;
		// and push this object to the list
		PicSync.Images.addImage(imageObject);
		// store this new item locally
		//storeNewImageLoaded(imageObject);
		// put this in the list
		insertThumbDivAtCorrectLocation(thumb, null);
		// and update the width of the container to include this thumbnail image
		containerWidth = containerWidth + thumb.offsetWidth + 10; // adding 10 for margins
		$(".container-inner").css("width", containerWidth);
		return imageObject;
	} 
	
	autoFixFiles = function() {
		// go through the list of files to fix and see if any partner other files
		// if so they can just go with those files...
		var imageObjects = PicSync.Images.getImagesLoaded();
		for (var i = 0; i < imageObjectsToFix.length; ++i) {
			var imageObjectToFix = imageObjectsToFix[i];
			var fileToFix = imageObjectToFix.file;
			// for each one, try to find it's partner in the list we just processed
			for (var j = 0; j < imageObjects.length; ++j) {
				var imageObject = imageObjects[j];
				var imageFile = imageObject.file;
				if (!imageObject.cameraId) {
					// don't check this, there is no camera ID so it is not a valid thing in the image list (one to fix)...
				}
				else if (compareNameWithoutExtension(imageFile, fileToFix)) {
					// same name, but are they made togehter too?
					var timeDifference = Math.abs(imageFile.lastModifiedDate - fileToFix.lastModifiedDate);
					if (timeDifference < 10000) {
						// the names are the same and were made at a similar time, partner this
						imageObjects[j].partners.push(fileToFix);
						// ensure there is an overlay on this thumb
						addMultiFileOverlay(imageObject);
						// removing from our list in the store
						PicSync.Images.removeImage(imageObjectToFix);
						// and remove this from the list to fix we are showing...
						var thumb = document.getElementById(imageObjectToFix.thumbId);
						thumb.parentNode.removeChild(thumb);
						break;
					}
				}
			}
		}
		// show / hide the fix panel accordingly
		public.showFixPanel();
	}
	
	public.performImageSynchronisation = function(cameraObject) {
		// now we need to re-sort the list
		if (!PicSync.Images) {
			return;
		}
		var images_loaded = PicSync.Images.getImagesLoaded();
		// go through all the images for this camera and update the date for each
		var previousThumbId = "";
		for (var i = 0; i < images_loaded.length; ++i) {
			// update the representation
			var imageObject = images_loaded[i];
			if (imageObject.cameraId == cameraObject.id) {
				// this is for the camera object that changed, update the offset time
				var imageDate = imageObject.imageDate;
				imageObject.imageDateOffset = PicSync.TimeSync.offsetImageDate(imageDate, imageObject.cameraId);	
			}
		}
		// now all the dates are corrected, sort the data
		PicSync.Images.sortImages();
		// now update the thumbs to be in the correct position (go through backwards to make it easier to remember the previous one)
		for (var i = images_loaded.length - 1; i >= 0; --i) {
			// update the representation
			var imageObject = images_loaded[i];
			if (imageObject.cameraId == cameraObject.id) {
				// this is for the camera object that changed, so we can update the thumbnail here
				// now ensure this is in the correct position, get the div for the image
				var thumb = document.getElementById(imageObject.thumbId);
				if (thumb) {
					thumb.style.borderColor = cameraObject.color;
					// get the title div
					var title = document.getElementById(imageObject.thumbId + "_title");
				    if (title) {
				    	title.textContent = PicSync.TimeSync.getExifFilename(imageObject.imageDateOffset);
				    }
				    // and re-position this thumbnail to where it should be
					thumb.parentNode.removeChild(thumb);
					insertThumbDivAtCorrectLocation(thumb, null);
				}
			}
		}
	}
	
	addMultiFileOverlay = function(imageObject) {
		if (imageObject.partners.length == 1) {
			// this is the first addition, OK to add the overlay, find the thumb
			var thumb = document.getElementById(imageObject.thumbId);
			// create the overlay
			var image = document.createElement('img');
			image.className = "thumbnailOverlay";
			image.setAttribute("src", "/images/filesOverlay.png");
			thumb.appendChild(image);
		}
	}
	
	compareNameWithoutExtension = function(fileOne, fileTwo) {
		var nameOne = fileOne.name;
		var dotIndex = nameOne.lastIndexOf('.');
		if (dotIndex) {
			nameOne = nameOne.substring(0, dotIndex);
		}
		var nameTwo = fileTwo.name;
		dotIndex = nameTwo.lastIndexOf('.');
		if (dotIndex) {
			nameTwo = nameTwo.substring(0, dotIndex);
		}
		return nameOne == nameTwo;
	}
	
	public.associateImageObjectWithCamera = function(imageObject, cameraObject) {
		// set the missing data on the image object
		imageObject["cameraId"] = cameraObject.id;
		var imageDateIncOffset = PicSync.TimeSync.offsetImageDate(imageObject.imageDate, cameraObject.id);
		imageObject["imageDateOffset"] = imageDateIncOffset;
		// remove the thumb from it's parent, the fix list
		var thumb = document.getElementById(imageObject.thumbId);
		thumb.parentNode.removeChild(thumb);
		thumb.style.borderColor = cameraObject.color;
		// change the style
		thumb.className = "thumbnailImagePanel";
		// now we have updated the data, re-sort the list
		PicSync.Images.sortImages();
		// put this in the list
		insertThumbDivAtCorrectLocation(thumb, null);
		// and update the width of the container to include this thumbnail image
		containerWidth = containerWidth + thumb.offsetWidth + 10; // adding 10 for margins
		$(".container-inner").css("width", containerWidth);
		// show / hide the fix panel accordingly
		public.showFixPanel();
	}
	
	addFixImageFile = function(f, data) {
		var reps = createImageRepresentation(f, "thumbnailFixImagePanel", f.name, 'grey', "./images/thumbnail.png");
		var imageObject = reps[0];
		var thumb = reps[1];
		// set the date data on this object
		imageObject["cameraId"] = null;
		imageObject["imageDate"] = f.lastModifiedDate;
		imageObject["imageDateOffset"] = f.lastModifiedDate;
		// and push this object to the list
		PicSync.Images.addImage(imageObject);
		// store this new item locally
		//storeNewImageLoaded(imageObject);
		// put this in the panel
		var fixImagePanel = document.getElementById('fix_image_panel');
		fixImagePanel.appendChild(thumb, fixImagePanel.firstChild);
		// return the created object
		return imageObject;
	}
	
	createImageRepresentation = function(f, thumbClass, titleText, color, imageSource) {
		// with a nice new unique ID, create a thumbnail representation of the file
		var uniqueImageId = PicSync.Images.getNextUniqueImageId();
		// create the thumbnail for this image
        var thumb = document.createElement('div');
        thumb.className = thumbClass;
        thumb.id = "image_thumb_" + uniqueImageId;
        thumb.style.borderColor = color;
        // create the iamge
		var img = document.createElement("img");
		img.src = imageSource;
		img.id = thumb.id + "_img";
		img.className = "thumbnailImage";
		img.alt = f.name;
	    img.height = 75;
	    // add the image to the thumbnail to show it
	    thumb.appendChild(img);
	    // also we want to add the date label (including offset) to this thumbnail image
	    var title = document.createElement("div");
	    title.className = "thumbnailTitle";
	    title.id = thumb.id + "_title";
	    title.textContent = titleText;
	    thumb.appendChild(title);
	    
        // and remember what we have loaded
		var imageObject = new Object();
		imageObject["id"] = uniqueImageId;
		imageObject["file"] = f;
		imageObject["cameraColor"] = color;
		imageObject["thumbId"] = thumb.id;
		imageObject["partners"] = [];
		// process the click events on thumb and button
		thumb.addEventListener("click", function(){
			public.showMainImage(imageObject);
		});
		// setup the image drag start
	    img.addEventListener('dragstart', function (event) {
	    	// set the list of files to be the file, not done as just an image dragging
	    	event.dataTransfer.setData('thumbIds', thumb.id);
	    	event.dataTransfer.setData('imageObjectIds', imageObject.id);
	    });
		// and return
		return [imageObject, thumb];
	}

	insertThumbDivAtCorrectLocation = function(thumb, beforeId) {
		// find the item after this one in the list of images loaded
		var parentList = document.getElementById('file_loaded_list');
		if (beforeId == null) {
			// not sure where this goes, look for a place
			var images_loaded = PicSync.Images.getImagesLoaded();
			for (var i = images_loaded.length - 1; i >= 0; --i) {
				// loop through all the files we have
				if (thumb.id == images_loaded[i].thumbId ) {
					// this is our thumb in the list, find the first after this who is in the image list
					for (var j = i + 1; j < images_loaded.length; ++j) {
						// while there are items after this one, find one who's camera
						// id is set (ie in the sync image list) that we can insert ourselves after
						if (!images_loaded[j].cameraId) {
							// there is no cameraID, ignore this one, in the fix list...
						}
						else {
							// use this one as the one we want to insert ourselves before
							beforeId = images_loaded[j].thumbId;
							break;
						}
					}
					// else we are just at the end, use NULL as the before value
					break;
				}
			}
		}
		if (beforeId) {
			// put the thumbnail in before the one after it in the list
			parentList.insertBefore(thumb, document.getElementById(beforeId));
		}
		else {
			// put the thumbnail in the end of the list
			document.getElementById('file_loaded_list').insertBefore(thumb, null);
		}
	}

	updateImageRepresentation = function(imageObject) {
		var imageThumbId = "image_thumb_" + imageObject.id;
		// set the correct border color in case it is different
		document.getElementById(imageThumbId).style.borderColor = imageObject.cameraColor;
		// update the alt text of the image
		document.getElementById(imageThumbId + "_img").alt = "Taken at " + imageObject.imageDate + " but will offset to " + imageObject.imageDateOffset;
		// and the title
		document.getElementById(imageThumbId + "_title").textContent = PicSync.TimeSync.getExifFilename(imageObject.imageDateOffset);
	}

	public.abortRead = function() {
		// clear the list of files to process
		filesProcessing = [];
		PicSync.Progress.updateProgress(fileProcessingIndex, fileProcessingIndex, "");
	}
	
	public.showFixPanel = function() {
		var fixPanel = document.getElementById('fix_image_panel');
		if (fixPanel.childNodes.length > 1) {
			// more than just the label
			$('#fix_image_panel').show();
		}
		else {
			// just the label
			$('#fix_image_panel').hide();
		}
	}
	
	init = function() {
		// initialise this module here
		public.showFixPanel();
		// setup dragging of the fix panel title to the correct camera
		var fixTitle = document.getElementById('fix_image_panel_titlebar');
		fixTitle.addEventListener('dragstart', function (event) {
	    	// set the list of files to be the file, not done as just an image dragging
			var thumbIds = "";
			var imageObjectIds = "";
			var fixPanel = document.getElementById('fix_image_panel');
			for (var i = 0; i < fixPanel.childNodes.length; ++i) {
				var child = fixPanel.childNodes[i];
				if (child && child.id) {
					// have a child, get the thumb id...
					var imageObject = PicSync.Images.getImageObjectLoaded(child.id);
					if (imageObject) {
						// this is a good thumbnail, add the data to the string
						thumbIds += child.id + ",";
						imageObjectIds += imageObject.id + ",";
					}
				}
			}
			// set this data in the drag operation
	    	event.dataTransfer.setData('thumbIds', thumbIds);
	    	event.dataTransfer.setData('imageObjectIds', imageObjectIds);
	    });
	}();
	
	return public;
})();
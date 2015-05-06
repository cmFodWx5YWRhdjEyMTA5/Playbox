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
					img.className = "mainImage";
					img.setAttribute("title", escape(imageFile.name));
					imagePanel.appendChild(img);
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
		showFileThumbnails(files);
	}

	showFileThumbnails = function(files) {
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
		processFileRecursive();
	}

	processFileRecursive = function() {
		if (++fileProcessingIndex < filesProcessing.length) {
			// process this file
			var f = filesProcessing[fileProcessingIndex];
			var progressText = "";
			if (null != f) {
				progressText = f.name;
			}
			PicSync.Progress.updateProgress(fileProcessingIndex + 1, filesProcessing.length, "Importing " + progressText);
			if (null != f 
					&& (f.type.match('image.*'))// || f.type.match('video.*')) 
					&& false == getIsImageLoaded(f)) {
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
						processFileRecursive();
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
				processFileRecursive();
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
        
        var imageDate = PicSync.TimeSync.getExifImageDate(data.exif, f);
        var cameraId = PicSync.TimeSync.getExifCameraId(data.exif);
        
		var imageDateIncOffset = PicSync.TimeSync.offsetImageDate(imageDate, cameraId);
		
		var cameraObject = PicSync.TimeSync.getExifCamera(cameraId);
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
		// process the thumbnail click
		thumb.addEventListener("click", function(){
			public.showMainImage(imageObject);
		});
		// and update the width of the container to include this thumbnail image
		containerWidth = containerWidth + thumb.offsetWidth;
		$(".container-inner").css("width", containerWidth);
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
		showFixPanel();
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
		// change the style
		thumb.className = "thumbnailImagePanel";
		// now we have updated the data, re-sort the list
		PicSync.Images.sortImages();
		// put this in the list
		insertThumbDivAtCorrectLocation(thumb, null);
		// and update the width of the container to include this thumbnail image
		containerWidth = containerWidth + thumb.offsetWidth;
		$(".container-inner").css("width", containerWidth);
		// show / hide the fix panel accordingly
		showFixPanel();
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
	    img.addEventListener('dragstart', function (event) {
	    	// set the list of files to be the file, not done as just an image dragging
	    	var thumbId = thumb.id;
	    	event.dataTransfer.setData('thumbId', thumbId);
	    });
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
	
	showFixPanel = function() {
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
		showFixPanel();
	}();
	
	return public;
})();
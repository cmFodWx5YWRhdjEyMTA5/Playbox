//declare the module
PicSync.Display = (function () {

	var containerWidth = 100;

	var fileProcessingIndex = 0;
	var filesProcessing = null;
	
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
				            return;
				        }
				        var orientation = PicSync.TimeSync.getExifOrientation(data.exif);
				        var thumbnail = PicSync.TimeSync.getExifThumbnail(data.exif);
				        
				        var imageDate = PicSync.TimeSync.getExifImageDate(data.exif, f);
				        var cameraId = PicSync.TimeSync.getExifCameraId(data.exif);
				        
						var imageDateIncOffset = PicSync.TimeSync.getExifImageDateIncOffset(data.exif, f, cameraId);
						
						var cameraObject = PicSync.TimeSync.getExifCamera(cameraId);
						var uniqueImageId = PicSync.Images.getNextUniqueImageId();
				        // create the thumbnail for this image
				        var thumb = document.createElement('div');
				        thumb.className = "thumbnailImagePanel";
				        thumb.id = "image_thumb_" + uniqueImageId;

						var img = document.createElement("img");
						img.src = thumbnail;
						img.id = thumb.id + "_img";
						img.className = "thumbnailImage";
						img.alt = "Taken at " + imageDate + " but will offset to " + imageDateIncOffset;
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
					    title.textContent = PicSync.TimeSync.getExifFilename(imageDateIncOffset);
					    thumb.appendChild(title);
				        // and remember what we have loaded
						var imageObject = new Object();
						imageObject["id"] = uniqueImageId;
						imageObject["file"] = f;
						imageObject["cameraId"] = cameraId;
						imageObject["imageDate"] = imageDate;
						imageObject["imageDateOffset"] = imageDateIncOffset;
						if (null != cameraObject) {
							imageObject["cameraColor"] = cameraObject.color;
						}
						else {
							imageObject["cameraColor"] = 'grey';
						}
				        thumb.style.borderColor = imageObject.cameraColor;
						imageObject["thumbId"] = thumb.id;
						// and push this object to the list
						PicSync.Images.addImage(imageObject);
						// store this new item locally
						//storeNewImageLoaded(imageObject);
						insertThumbDivAtCorrectLocation(thumb, null);
						// process the thumbnail click
						thumb.addEventListener("click", function(){
							public.showMainImage(imageObject);
						});
						// and update the width of the container to include this thumbnail image
						containerWidth = containerWidth + thumb.offsetWidth;
						$(".container-inner").css("width", containerWidth);
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
				// not an image, just move on
				processFileRecursive();
			}
		}
	}

	insertThumbDivAtCorrectLocation = function(thumb, beforeId) {
		// find the item after this one in the list of images loaded
		if (beforeId == null) {
			// not sure where this goes, look for a place
			var images_loaded = PicSync.Images.getImagesLoaded();
			for (var j = images_loaded.length - 1; j >= 0; --j) {
				// loop through all the files we have
				if (thumb.id == images_loaded[j].thumbId ) {
					// this is the one in the list we want to add
					if (j < images_loaded.length - 1) {
						beforeId = images_loaded[j + 1].thumbId;
					}
					// else we are just at the end, use NULL as the before value
					break;
				}
			}
		}
		if (beforeId) {
			// put the thumbnail in before the one after it in the list
			document.getElementById('file_loaded_list').insertBefore(thumb, document.getElementById(beforeId));
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
	
	init = function() {
		// initialise this module here
	}();
	
	return public;
})();
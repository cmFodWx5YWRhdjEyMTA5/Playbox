var progress = document.querySelector('.percent');

var images_loaded = [];

var uniqueImageId = 1;

var containerWidth = 100;

function handleFileSelect(evt) {
	var files = evt.target.files;
	// FileList object
	showFileThumbnails(files);
}

function handleFileDrop(evt) {
	evt.stopPropagation();
	evt.preventDefault();

	var files = evt.dataTransfer.files;
	// FileList object
	showFileThumbnails(files);
}

// sort files in the array by the calculated offset date
function fileComparator(a, b) {
  return a.imageDateOffset - b.imageDateOffset;

}

function handleFileDragOver(evt) {
	evt.stopPropagation();
	evt.preventDefault();
	evt.dataTransfer.dropEffect = 'copy'; // Explicitly show this is a copy.
}

function showFiles(files) {
	// files is a FileList of File objects. List some properties.
	var output = [];
	// files is a FileList object
	for (var i = 0, f; f = files[i]; i++) {
		output.push('<li><strong>', escape(f.name), '</strong> (', f.type
				|| 'n/a', ') - ', f.size, ' bytes, last modified: ',
				f.lastModifiedDate ? f.lastModifiedDate.toLocaleDateString() : 'n/a', '</li>');
	}
	document.getElementById('file_loaded_list').innerHTML = '<ul>' + output.join('') + '</ul>';
}

function getImageLoaded(thumbId) {
	var foundFile = null;
	for (var j = 0; j < images_loaded.length; ++j) {
		// loop through all the files we have
		if (thumbId == images_loaded[j].thumbId) {
			foundFile = images_loaded[j].file;
			break;
		}
	}
	return foundFile;
}

function getIsImageLoaded(file) {
	var foundFile = false;
	for (var j = 0; j < images_loaded.length; ++j) {
		// loop through all the files we have
		var existingFile = images_loaded[j].file;
		if (existingFile.name == file.name && 
				existingFile.lastModifiedDate - file.lastModifiedDate == 0 &&
				existingFile.size - file.size == 0) {
			// checking the name, size, and modified time to see if the same loaded already
			foundFile = true;
			break;
		}
	}
	return foundFile;
}

var fileProcessingIndex = 0;
var filesProcessing = null;

function showFileThumbnails(files) {
	if (null == files || files.length == 0) {
		return;
	}
	// Reset progress indicator on new file selection.
	document.getElementById('progress_bar').className = 'loading';
	progress.style.width = '0%';
	progress.textContent = '0%';
	// Loop through the FileList and render image files as thumbnails.
	fileProcessingIndex = -1;
	filesProcessing = files;
	// process the first file, the call-back function will process the next, and so on...
	processFileRecursive();
}

function processFileRecursive() {
	if (++fileProcessingIndex < filesProcessing.length) {
		// process this file
		var f = filesProcessing[fileProcessingIndex];
		var progressText = "";
		if (null != f) {
			progressText = f.name;
		}
		updateProgress(fileProcessingIndex + 1, filesProcessing.length, "" + fileProcessingIndex + " " + progressText);
		if (null != f && f.type.match('image.*') && false == getIsImageLoaded(f)) {
			// the file is an image does not exist in the list yet, load it into a thumbnail, first we have to			
			// read the EXIF data here to get the actual size, so can calculate the width and position of the image
			EXIF.getData(f, function() {
				// get the relevant EXIF data
				var imageDate = getExifImageDate(this);
				var cameraId = createExifCameraId(this);
				var imageDateIncOffset = getExifImageDateIncOffset(this);
		        // create the thumbnail for this image
		        var thumb = document.createElement('div');
		        thumb.className = "thumbnailImagePanel";
		        thumb.id = "image_thumb_" + uniqueImageId;

				var img = document.createElement("img");
				img.src = "/images/thumbnail.png";
				img.id = thumb.id + "_img";
				img.className = "thumbnailImage";
				img.alt = "Taken at " + imageDate + " but will offset to " + imageDateIncOffset;
			    img.height = 75;
			    // add the image to the thumbnail to show it
			    thumb.appendChild(img);
			    // also we want to add the date label (including offset) to this thumbnail image
			    var title = document.createElement("div");
			    title.className = "thumbnailTitle";
			    title.id = thumb.id + "_title";
			    title.textContent = getExifFilename(imageDateIncOffset);
			    thumb.appendChild(title);
		        // and remember what we have loaded
				var imageObject = new Object();
				imageObject["id"] = uniqueImageId;
				imageObject["file"] = this;
				imageObject["cameraId"] = cameraId;
				imageObject["imageDate"] = imageDate;
				imageObject["imageDateOffset"] = imageDateIncOffset;
				imageObject["thumbId"] = thumb.id;
				// and push this object to the list
				images_loaded.push(imageObject);
				images_loaded.sort(fileComparator);
				// store this new item locally
				//storeNewImageLoaded(imageObject);
				insertThumbDivAtCorrectLocation(thumb, null);
				// process the thumbnail click
				thumb.addEventListener("click", function(){
				    showMainImage(imageObject);
				});
				var originalWidth = thumb.offsetWidth + 10;	//adding the 2*5 margin widths
				// and update the width of the container to include this thumbnail image
				containerWidth = containerWidth + originalWidth;
				$(".container-inner").css("width", containerWidth);
				var that = this;
				// and handle the mouse over operation
			    var mouseOverFunction = function() {
			    	// load the image here as a nice thumbnail on hover over it
			    	img.removeEventListener("mouseover", mouseOverFunction);
					img.src = URL.createObjectURL(that);
				    img.height = 75;
				    img.onload = function() {
				    	// release the URL
				        URL.revokeObjectURL(this.src);
				        // and update the width of the container to include the width of this (minus the original width it was)
				        containerWidth = containerWidth + thumb.offsetWidth - originalWidth;
						$(".container-inner").css("width", containerWidth);
				    }
			    } 
			    img.addEventListener("mouseover", mouseOverFunction);
				// this ID is used now, increment the counter
				++uniqueImageId;
				// and call the function recursively
				processFileRecursive();
		    });
		}
		else {
			// not an image, just move on
			processFileRecursive();
		}
	}
}

function insertThumbDivAtCorrectLocation(thumb, beforeId) {
	// find the item after this one in the list of images loaded
	if (beforeId == null) {
		// not sure where this goes, look for a place
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

function updateImageRepresentation(imageObject) {
	var uniqueImageId = imageObject.id;
	var imageThumbId = "image_thumb_" + uniqueImageId;
	// update the alt text of the image
	document.getElementById(imageThumbId + "_img").alt = "Taken at " + imageObject.imageDate + " but will offset to " + imageObject.imageDateOffset;
	// and the title
	document.getElementById(imageThumbId + "_title").textContent = getExifFilename(imageObject.imageDateOffset);
}

function abortRead() {
	// clear the list of files to process
	filesProcessing = [];
	updateProgress(fileProcessingIndex, fileProcessingIndex, "");
}

function performImageSynchronisation(cameraObject) {
	// now we need to re-sort the list
	images_loaded.sort(fileComparator);
	// go through all our images and synchronise their position, change their name and
	// put them in the correct place in the list
	var previousThumbId = "";
	for (var i = images_loaded.length - 1; i >= 0; --i) {
		// update the representation
		var imageObject = images_loaded[i];
		if (imageObject.cameraId == cameraObject.id) {
			// ensure this is in the correct position, get the div for the image
			var thumb = document.getElementById(imageObject.thumbId);
			thumb.parentNode.removeChild(thumb);
			insertThumbDivAtCorrectLocation(thumb, previousThumbId);
			// this image applies to the camera that changed
			EXIF.getData(imageObject.file, function() {
				// get the relevant EXIF data
				var imageDate = getExifImageDate(this);
				var imageDateIncOffset = getExifImageDateIncOffset(this);
				// set this data in the image object
				imageObject["imageDate"] = imageDate;
				imageObject["imageDateOffset"] = imageDateIncOffset;
				// and update the representation
				updateImageRepresentation(imageObject);
			});
		}
		// remember the previous ID in order to put the thumbnail at the correct location
		previousThumbId = imageObject.thumbId;
	}
}
/*
 * UNABLE TO STORE IMAGE FILE OBJECTS TO USE LATER - WHATEVER (o;
function storeNewImageLoaded(imageObject) {
	// store the image object under it's unique ID
	var imageStoreId = "image_";
	imageStoreId = imageStoreId + imageObject.id;
	localStorage.setItem(imageStoreId, JSON.stringify(imageObject));
	localStorage.setItem(imageStoreId + "_file", imageObject.fileURL);
	// remember all the IDs in a place we know, just need the last one as it is sequential (o;
	localStorage.setItem("imageObjectUniqueId", uniqueImageId);
}

function retrieveImagesLoaded() {
	uniqueImageId = 1;
	while (false) {
		// while there are images with the sequential ID, get each one
		var imageStoreId = "image_";
		imageStoreId = imageStoreId + uniqueImageId;
		var imageObjectString = localStorage.getItem(imageStoreId);
		var imageObject = JSON.parse(imageObjectString);
		if (null != imageObject) {
			// set the file, as are nested objects will be empty... stringify is not recursive
			imageObject.file = JSON.parse(localStorage.getItem(imageStoreId + "_file"));
			images_loaded.push(imageObject);
			// increment the counter to the next one
			++uniqueImageId;
		}
		else {
			// there is no image at this index, uniqueImageId is set too, stop here
			break;
		}
	}
	// make sure this is nicely sorted now
	images_loaded.sort(fileComparator);
}
*/
function showMainImage(imageObject) {
	// show the main image file for this object
	showMainImageFile(imageObject.file);
}

function showMainImageFile(imageFile) {
	// clear the current image
	var imagePanel = document.getElementById('image_panel');
	while (imagePanel.firstChild) {
		imagePanel.removeChild(imagePanel.firstChild);
	}
	var reader = new FileReader();
	reader.onload = (function(theFile) {
		return function(e) {
			// create the image, an image can natively be dragged so helpful functionality
			var image = document.createElement('img');
			image.className = "mainImage";
			image.setAttribute("src", e.target.result);
			image.setAttribute("title", escape(theFile.name));
			imagePanel.appendChild(image);
		};
	})(imageFile);
	// Read in the image file as a data URL.
	reader.readAsDataURL(imageFile);
}

function showImagesLoaded() {
	// go through the list of images and ensure they are all shown as thumbnails
	// so create a list of files to send to the function
	var files = [];
	for (var i = 0; i < images_loaded.length; ++i) {
		var imageObject = images_loaded[i];
		if (null != imageObject) {
			files.push(imageObject.file);
		}
	}
	// show the thumbnails for all of these
	showFileThumbnails(files);
}

function triggerFileSelect() {
	$('#file_browse_button').trigger('click');
}

function updateProgress(loaded, total, description) {
	var percentLoaded = Math.round((loaded / total) * 100);
	// Increase the progress bar length.
	if (percentLoaded < 100) {
		progress.style.width = percentLoaded + '%';
		progress.textContent = percentLoaded + "% " + description;
	}
	else {
		// Ensure that the progress bar displays 100% at the end for a couple seconds
		progress.style.width = '100%';
		progress.textContent = '100%';
		setTimeout("document.getElementById('progress_bar').className='';", 2000);
	}
}

$(document).ready(function() {

	//setup the file browsing button
	document.getElementById('file_browse_button').addEventListener('change', handleFileSelect, false);
	//Setup the dnd listeners to receive a file
	var dropZone = document.getElementById('file_drop_zone');
	dropZone.addEventListener('dragover', handleFileDragOver, false);
	dropZone.addEventListener('drop', handleFileDrop, false);
	
	// load all our data here
	//retrieveImagesLoaded();
	// and show them
	//showImagesLoaded();
});


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

// sort files in the array by modified date
function fileComparator(a, b) {
  return a.file.lastModifiedDate - b.file.lastModifiedDate;

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

function getImageLoaded(spanId) {
	var foundFile = null;
	for (var j = 0; j < images_loaded.length; ++j) {
		// loop through all the files we have
		if (spanId == images_loaded[j].span.id) {
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

function showFileThumbnails(files) {
	if (null == files || files.length == 0) {
		return;
	}
	// Reset progress indicator on new file selection.
	document.getElementById('progress_bar').className = 'loading';
	progress.style.width = '0%';
	progress.textContent = '0%';
	// Loop through the FileList and render image files as thumbnails.
	var imagesProcessed = 0;
	var imagesToProcess = files.length;
	for (var i = 0, f; f = files[i]; i++) {
		// Only process image files.
		if (!f.type.match('image.*')) {
			updateProgress(++imagesProcessed, imagesToProcess, files[i].name);
			continue;
		}
		// check we have this file or not
		var fileExists = getIsImageLoaded(f);
		if (!fileExists) {
			// the file does not exist in the list yet, load it into a thumbnail, first we have to			
			// read the EXIF data here to get the actual size, so can calculate the width and position of the image
			EXIF.getData(f, function() {
				// get the relevant EXIF data
				var imageDate = getExifImageDate(this);
				var cameraId = createExifCameraId(this);
		        // create the thumbnail for this image
		        var span = document.createElement('span');
				span.id = "image_thumb_" + uniqueImageId;

				var img = document.createElement("img");
				img.src = URL.createObjectURL(this);
			    img.height = 75;
			    img.onload = function() {
			    	// release the URL
			        URL.revokeObjectURL(this.src);
			        // and update the width of the container to include the width of this
			        containerWidth = containerWidth + span.offsetWidth;
					$(".container-inner").css("width", containerWidth);
			    }
			    // add the image to the span to show it
				span.appendChild(img);
		        
				// and remember what we have loaded
				var imageObject = new Object();
				imageObject["id"] = uniqueImageId;
				imageObject["file"] = this;
				imageObject["span"] = span;
				imageObject["fileURL"] = img.src;
				// and push this object to the list
				images_loaded.push(imageObject);
				images_loaded.sort(fileComparator);
				// store this new item locally
				storeNewImageLoaded(imageObject);
				// find the item after this one in the list of images loaded
				var beforeId = "";
				for (var j = 0; j < images_loaded.length; ++j) {
					// loop through all the files we have
					if (span.id == images_loaded[j].span.id && j < images_loaded.length - 1) {
						// this is the one in the list we want to add
						beforeId = images_loaded[j + 1].span.id;
						break;
					}
				}
				if (beforeId) {
					// put the span in before the one after it in the list
					document.getElementById('file_loaded_list').insertBefore(span, document.getElementById(beforeId));
				}
				else {
					// put the span in the end of the list
					document.getElementById('file_loaded_list').insertBefore(span, null);
				}
				// process the span click
				span.addEventListener("click", function(){
				    showMainImage(imageObject);
				});
				// this ID is used now, increment the counter
				++uniqueImageId;
				// and update the progress of this
				updateProgress(++imagesProcessed, imagesToProcess, this.name);
		    });
		}
		else {
			// file is already processed, update progress
			updateProgress(++imagesProcessed, imagesToProcess, files[i].name);
		}
	}
}

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
			// set the file and span, as are nested objects will be empty... stringify is not recursive
			imageObject.file = JSON.parse(localStorage.getItem(imageStoreId + "_file"));
			imageObject.span = JSON.parse(localStorage.getItem(imageStoreId + "_span"));
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
	retrieveImagesLoaded();
	// and show them
	showImagesLoaded();
});


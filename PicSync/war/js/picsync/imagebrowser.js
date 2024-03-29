PicSync.Images = (function () {
	// declare private variables
	var images_loaded = [];
	var uniqueImageId = 1;
	
	var public = {};
	
	public.getNextUniqueImageId = function() {
		return uniqueImageId++;
	}
	
	public.getImagesLoaded = function() {
		return images_loaded;
	}
	
	fileComparator = function (a, b) {
	  return a.imageDateOffset - b.imageDateOffset;
	}
	
	public.sortImages = function() {
		//sort files in the array by the calculated offset date
		images_loaded.sort(fileComparator);
	}
	
	public.addImage = function(imageObject) {
		images_loaded.push(imageObject);
		public.sortImages();
	}
	
	public.removeImage = function(imageObject) {
		var removeIndex = images_loaded.indexOf(imageObject);
		if (removeIndex > -1) {
			images_loaded.splice(removeIndex, 1);
		}
	}
	
	public.removeImageId = function(imageObjectId) {
		for (var i = 0; i < images_loaded.length; ++i) {
			if (images_loaded[i].id == imageObjectId) {
				public.removeImage(images_loaded[i]);
				break;
			}
		}
	}
	
	function handleFileSelect(evt) {
		var files = evt.target.files;
		// FileList object
		PicSync.Display.showFileThumbnails(files, null);
	}

	function handleFileDrop (evt) {
		evt.stopPropagation();
		evt.preventDefault();

		/*var dropContents = evt.dataTransfer.files;
		// create a list of the files, traverse the folders here
		var files = [];
		getFiles(dropContents, files);
		// FileList object
		PicSync.Display.showFileThumbnails(files, null);
		*/
		PicSync.Display.showFileThumbnails(evt.dataTransfer.files, null);
	}

	function handleDeleteDrop (evt) {
		evt.stopPropagation();
		evt.preventDefault();
		
		// find the thumb dropped
		var thumbsString = evt.dataTransfer.getData('thumbIds');
		var imageObjectIdsString = evt.dataTransfer.getData('imageObjectIds');
    	var cameraObjectIdsString = evt.dataTransfer.getData('cameraObjectIds');
		if (thumbsString) {
			var thumbs = thumbsString.split(",");
			var imageObjectIds = null;
			if (imageObjectIdsString) {
				imageObjectIds = imageObjectIdsString.split(",");
			}
			var cameraObjectIds = null;
			if (cameraObjectIdsString) {
				cameraObjectIds = cameraObjectIdsString.split(",");
			}
			for (var i = 0; i < thumbs.length; ++i) {
				var thumb = document.getElementById(thumbs[i]);
				if (thumb) {
					// remove the thumb representation
			    	thumb.parentNode.removeChild(thumb);
					// for each thumb, get the image and camera id associated, and remove
					if (imageObjectIds && i < imageObjectIds.length && imageObjectIds[i]) {
						// remove the image data for this thumb
						PicSync.Images.removeImageId(imageObjectIds[i]);
					}
					if (cameraObjectIds && i < cameraObjectIds.length && cameraObjectIds[i]) {
						// remove the camera data for this thumb
						PicSync.TimeSync.removeCameraId(cameraObjectIds[i]);
					}
				}
			}
	    	// might need to hide the fix panel, if it deleted from there
	    	PicSync.Display.showFixPanel();
		}
	}
	
	//TODO test and complete (webkitGetAsEntry() only works on Chrome 21+ though)
	getFiles = function (toSearch, toPopulate) {
		for (var i = 0; i < toSearch.length; ++i) {
			var file = toSearch[i];
			var entry = file.webkitGetAsEntry();
			if (entry) {
				// do something with the file
				if (file.isDirectory) {
					// get the directory contents to push to the list
		
				}
				else {
					// push the file to the list
					toPopulate.push(file);
				}
			}
			else {
				// push the file to the list
				toPopulate.push(file);
			}
		}
	}

	function handleFileDragOver(evt) {
		evt.stopPropagation();
		evt.preventDefault();
		evt.dataTransfer.dropEffect = 'copy'; // Explicitly show this is a copy.
	}

	function handleDeleteDragOver(evt) {
		evt.stopPropagation();
		evt.preventDefault();
		evt.dataTransfer.dropEffect = 'copy'; // Explicitly show this is a move - to the bin
	}
	
	public.getImageObjectLoaded = function(thumbId) {
		var foundFile = null;
		for (var j = 0; j < images_loaded.length; ++j) {
			// loop through all the files we have
			if (thumbId == images_loaded[j].thumbId) {
				foundFile = images_loaded[j];
				break;
			}
		}
		return foundFile;
	}

	getIsImageLoaded = function(file) {
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

	triggerFileSelect = function() {
		$('#file_browse_button').trigger('click');
	}
	
	init = function() {
		//setup the file browsing button
		document.getElementById('file_browse_button').addEventListener('change', handleFileSelect, false);
		//Setup the dnd listeners to receive a file
		var dropZone = document.getElementById('file_drop_zone');
		dropZone.addEventListener('dragover', handleFileDragOver, false);
		dropZone.addEventListener('drop', handleFileDrop, false);
		// and delete things
		var dropZone = document.getElementById('delete_drop_zone');
		dropZone.addEventListener('dragover', handleDeleteDragOver, false);
		dropZone.addEventListener('drop', handleDeleteDrop, false);
	}();
	
	return public;
})();




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
	public.sortImages();
}
*/


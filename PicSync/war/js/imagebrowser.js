var progress = document.querySelector('.percent');

var images_loaded = [];

var uniqueImageId = 1;

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

function handleSyncDrop(evt) {
	evt.stopPropagation();
	evt.preventDefault();

	var files = evt.dataTransfer.files;
	// FileList object
	performFileSync(files);
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

function handleSyncDragOver(evt) {
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

function showFileThumbnails(files) {
	// Reset progress indicator on new file selection.
	document.getElementById('progress_bar').className = 'loading';
	progress.style.width = '0%';
	progress.textContent = '0%';
	// Loop through the FileList and render image files as thumbnails.
	for (var i = 0, f; f = files[i]; i++) {
		updateProgress(i, files.length);
		// Only process image files.
		if (!f.type.match('image.*')) {
			continue;
		}
		// check we have this file or not
		var fileExists = false;
		for (var j = 0; j < images_loaded.length; ++j) {
			// loop through all the files we have
			var existingFile = images_loaded[j].file;
			if (existingFile.name == f.name && 
					existingFile.lastModifiedDate - f.lastModifiedDate == 0 &&
					existingFile.size - f.size == 0) {
				// checking the name, size, and modified time to see if the same loaded already
				fileExists = true;
				break;
			}
		}
		if (!fileExists) {
			// the file does not exist in the list yet, load it into a thumbnail
			var reader = new FileReader();
			// Closure to capture the file information.
			reader.onload = (function(theFile) {
				return function(e) {
					// Render thumbnail as a new span element.
					var span = document.createElement('span');
					span.id = "image_thumb_" + uniqueImageId++;
					span.innerHTML = ['<img class="thumb" src="', e.target.result, '" title="', escape(theFile.name), '"/>'].join('');
					// and remember what we have loaded
					var imageObject = new Object();
					imageObject["file"] = theFile;
					imageObject["span"] = span;
					images_loaded.push(imageObject);
					images_loaded.sort(fileComparator);
					// find the item after this one in the list of images loaded
					var beforeId = "";
					var containerWidth = 100;
					for (var j = 0; j < images_loaded.length; ++j) {
						// loop through all the files we have
						if (span.id == images_loaded[j].span.id && j < images_loaded.length - 1) {
							// this is the one in the list we want to add
							beforeId = images_loaded[j + 1].span.id;
						}
						else if (images_loaded[j].thumbwidth) {
							containerWidth = containerWidth + images_loaded[j].thumbwidth;
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
					// remember the width of the span we just added in order to measure next time
					imageObject["thumbwidth"] = span.offsetWidth;
					containerWidth = containerWidth + span.offsetWidth;
					$(".container-inner").css("width", containerWidth);
				};
			})(f);
			// Read in the image file as a data URL.
			reader.readAsDataURL(f);
		}
	}
	// Ensure that the progress bar displays 100% at the end for a couple seconds
	progress.style.width = '100%';
	progress.textContent = '100%';
	setTimeout("document.getElementById('progress_bar').className='';", 2000);
}

function performFileSync(files) {
	// Reset progress indicator on new file selection.
	document.getElementById('progress_bar').className = 'loading';
	progress.style.width = '0%';
	progress.textContent = '0%';
	// Loop through the FileList and sync to the QR code in the image
	for (var i = 0, f; f = files[i]; i++) {
		updateProgress(i, files.length);
		// Only process image files.
		if (!f.type.match('image.*')) {
			continue;
		}
		
	}
	// Ensure that the progress bar displays 100% at the end for a couple seconds
	progress.style.width = '100%';
	progress.textContent = '100%';
	setTimeout("document.getElementById('progress_bar').className='';", 2000);
}

function triggerFileSelect() {
	$('#file_browse_button').trigger('click');
}

function updateProgress(loaded, total) {
	var percentLoaded = Math.round((loaded / total) * 100);
	// Increase the progress bar length.
	if (percentLoaded < 100) {
		progress.style.width = percentLoaded + '%';
		progress.textContent = percentLoaded + '%';
	}
}

//setup the file browsing button
document.getElementById('file_browse_button').addEventListener('change', handleFileSelect, false);
//Setup the dnd listeners to receive a file
var dropZone = document.getElementById('file_drop_zone');
dropZone.addEventListener('dragover', handleFileDragOver, false);
dropZone.addEventListener('drop', handleFileDrop, false);
//Setup the dnd listeners to sync an image
var dropZone = document.getElementById('sync_drop_zone');
dropZone.addEventListener('dragover', handleSyncDragOver, false);
dropZone.addEventListener('drop', handleSyncDrop, false);


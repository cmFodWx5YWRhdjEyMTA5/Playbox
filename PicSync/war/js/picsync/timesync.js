PicSync.TimeSync = (function () {
	
	var public = {};

	var displayQrCode = new QRCode(document.getElementById("time_sync_qr"), {
	    text: "not-initialised",
	    width: 240,
	    height: 240,
	    colorDark : "#000000",
	    colorLight : "#ffffff",
	    correctLevel : QRCode.CorrectLevel.H
	});

	var camera_time_offsets = [];
	var camera_colors = ['orange', 'blue', 'yellow', 'red', 'green'];

	var slideInnerDiv;
	
	makeCode = function(content) {
		// if the left margin of the slide inner bit is zero then create an image to show
		// as we are showing the code
		if (parseInt(slideInnerDiv.css('marginLeft'), 10) <= 30) {
			displayQrCode.makeCode(content); // make another code.
		}
	};
	
	function handleSyncDrop(evt) {
		evt.stopPropagation();
		evt.preventDefault();
	
		var files = evt.dataTransfer.files;
		if (null == files || files.length == 0) {
			// try instead the source node
			var thumbId = evt.dataTransfer.getData('thumbId');
			if (thumbId != null) {
				files = [PicSync.Images.getImageObjectLoaded(thumbId).file];
			}
		}
		// FileList object
		performFileSync(files);
	};
	
	function handleSyncDragOver(evt) {
		evt.stopPropagation();
		evt.preventDefault();
		evt.dataTransfer.dropEffect = 'copy'; // Explicitly show this is a copy.
	};
	
	performFileSync = function(files) {
		if (null == files || files.length == 0) {
			return;
		}
		// Reset progress indicator on new file selection.
		PicSync.Progress.startProgress();
		// Loop through the FileList to find and subsequently sync to the QR code in the image
		var filesProcessed = 0;
		var filesToProcess = files.length;
		for (var i = 0, f; f = files[i]; i++) {
			// Only process image files.
			if (!f.type.match('image.*')) {
				PicSync.Progress.updateProgress(++filesProcessed, filesToProcess, files[i].name);
				continue;
			}
			var reader = new FileReader();
			reader.onload = (function(theFile) {
				return function(e) {
					var processedFile = theFile;
					qrcode.callback = (function(dataResult) {
						PicSync.Progress.updateProgress(++filesProcessed, filesToProcess, theFile);
						processImageQrCode(processedFile, dataResult);
					});
					qrcode.decode(e.target.result);
				};
			})(f);
	
			// Read in the image file as a data URL.
			reader.readAsDataURL(f);
		}
		/*
		// Ensure that the progress bar displays 100% at the end for a couple seconds
		progress.style.width = '100%';
		progress.textContent = '100%';
		setTimeout("document.getElementById('progress_bar').className='';", 2000);
		*/
	};
	
	processImageQrCode = function(sourceFile, dataResult) {
		var isNumber =  /^\d+$/.test(dataResult);
		PicSync.Display.showMainImageFile(sourceFile);
		if (!isNumber) {
			dataResult = prompt("Please enter the number code on the image", dataResult);
			isNumber =  /^\d+$/.test(dataResult);
		}
		if (isNumber && sourceFile != null) {
			// this is a QR code of our time, get this data and show it
			var reggie = /(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})/;
			var dateArray = reggie.exec(dataResult); 
			var qrDate = new Date(
			    (+dateArray[1]),
			    (+dateArray[2])-1, // Careful, month starts at 0!
			    (+dateArray[3]),
			    (+dateArray[4]),
			    (+dateArray[5]),
			    (+dateArray[6])
			);
			// so we have the date the QR code says, get the date the file says
			if (qrDate != null) {
				storeImageOffsetData(sourceFile, qrDate);
			}
			else {
				alert("Sorry the image time could not be decoded, try another...");
			}
		}
		else {
			alert("Sorry no time image found, try another...");
		}
	};
	
	public.getExifOrientation = function(exifData) {
		return exifData.get('Orientation');
	}
	
	public.getExifThumbnail = function(exifData) {
		return exifData.get('Thumbnail');
	}
    
    public.getExifImageDate = function(exifData, sourceFile) {
		var time = exifData.get("DateTimeOriginal");
	    var imageDate = sourceFile.lastModifiedDate;
	    if (time != null) {
	        // get the time from this
	        var reggie = /(\d{4}):(\d{2}):(\d{2}) (\d{2}):(\d{2}):(\d{2})/;
			var dateArray = reggie.exec(time); 
			imageDate = new Date(
			    (+dateArray[1]),
			    (+dateArray[2])-1, // Careful, month starts at 0!
			    (+dateArray[3]),
			    (+dateArray[4]),
			    (+dateArray[5]),
			    (+dateArray[6])
			);
	    } 
	    return imageDate;
	}
	
	public.offsetImageDate = function(imageDate, cameraId) {
		// find if there is an offset to this
	    var offset = 0;
	    for (var i = 0; i < camera_time_offsets.length; ++i) {
	    	var cameraObject = camera_time_offsets[i];
	    	if (cameraObject != null && cameraObject.id == cameraId) {
	    		// use this offset for the camera
	    		offset = camera_time_offsets[i].difftime;
	    		offset -= camera_time_offsets[i].holidayOffset * 3600000;
	    		break;
	    	}
	    }
	    return new Date(imageDate.getTime() - offset);
	}
	
	twoDigit = function(number) {
		if (number >= 10) {
			return number;
		}
		else {
			return "0" + number;
		}
	}
	
	public.getExifFilename = function(fileDate) {
		// we are constructing the time by hand as year-month-day hour-min-sec
		var fileString = fileDate.getFullYear()
			+ "-" + twoDigit(fileDate.getMonth() + 1)
			+ "-" + twoDigit(fileDate.getDate())
			+ " " + twoDigit(fileDate.getHours())
			+ twoDigit(fileDate.getMinutes())
			+ twoDigit(fileDate.getSeconds());
		return fileString;
	}
	
	public.getExifCameraId = function(exifData) {
	    var make = exifData.get("Make"),
	    	model = exifData.get("Model");
		var cameraId = (make + " " + model).replace(/\s+/g, '_').toLowerCase();
		return cameraId;
	}
	
	public.getExifCamera = function(cameraId) {
		var cameraObject = null;
		for (var i = 0; i < camera_time_offsets.length; ++i) {
	    	var extantCamera = camera_time_offsets[i];
	    	if (extantCamera != null && extantCamera.id == cameraId) {
	    		// set this
	    		cameraObject = extantCamera;
	    		break;
	    	}
	    }
		return cameraObject;
	}
	
	storeImageOffsetData = function(sourceFile, qrDate) {
		loadImage.parseMetaData(
			sourceFile,
		    function (data) {
		        if (!data.imageHead) {
		            return;
		        }
		        // get the data for this then
		        var imageDate = public.getExifImageDate(data.exif, sourceFile);
				// compare the two
				var diffTime = imageDate.getTime() - qrDate.getTime();
		        // store this data in the list
		        var cameraObject = new Object();
		        cameraObject["id"] = public.getExifCameraId(data.exif);
		        cameraObject["make"] = data.exif.get("Make");
		        cameraObject["model"] = data.exif.get("Model");
		        cameraObject["holidayOffset"] = 0;
		        cameraObject["difftime"] = diffTime;
		        // put this in the array
		        var isAddNeeded = true;
		        for (var i = 0; i < camera_time_offsets.length; ++i) {
		        	var extantCamera = camera_time_offsets[i];
		        	if (extantCamera != null && extantCamera.id == cameraObject.id) {
		        		// set this
		        		camera_time_offsets[i] = cameraObject;
		        		isAddNeeded = false;
		        		break;
		        	}
		        }
		        var cameraObjectIndex = i;
		        if (isAddNeeded) {
		        	// this is new, add to the list, remembering the index
		        	cameraObjectIndex = camera_time_offsets.length;
		        	// and add to the list
		        	camera_time_offsets.push(cameraObject);
		        }
		        // set the color of this camera object now please
		        if (cameraObjectIndex >= camera_colors) {
		    		// just use black
		    		cameraObject["color"] = 'black';
		    	}
		    	else {
		    		// use one from the array
		    		cameraObject["color"] = camera_colors[cameraObjectIndex];
		    	}
		        // store the entire list in memory now
		        storeCameraObjects();
		        // show all the camera representations now at least one is different
		        showCameraRepesentations();
		        // and refresh the images we have to synchronise to this camera
		        //performImageSynchronisation();
		    },
		    {
		        maxMetaDataSize: 262144,
		        disableImageHead: false
		    }
		);
	}
	
	storeCameraObjects = function() {
		var cameraIds = "";
		for (var i = 0; i < camera_time_offsets.length; ++i) {
			var cameraObject = camera_time_offsets[i];
			if (cameraObject != null) {
				// Put the object into storage
				localStorage.setItem(cameraObject.id, JSON.stringify(cameraObject));
				// remember the ID to retrieve them all
				cameraIds = cameraIds + cameraObject.id;
				cameraIds = cameraIds + ',';
			}
		}
		// remember all the camera IDs in a place we know
		localStorage.setItem("cameraObjectIds", cameraIds);
	}
	
	retrieveCameraObjects = function() {
		camera_time_offsets = [];
		var cameraObjectIds = localStorage.getItem("cameraObjectIds");
		if (cameraObjectIds != null) {
			// get all the camera ids
			var cameraIds = cameraObjectIds.split(',');
			for (var i = 0; i < cameraIds.length; ++i) {
				var cameraId = cameraIds[i];
				var cameraObjectString = localStorage.getItem(cameraId);
				var cameraObject = JSON.parse(cameraObjectString);
				if (null != cameraObject) {
					camera_time_offsets.push(cameraObject);
				}
			}
		}
	}
	
	showCameraRepesentations = function() {
		var cameraListDiv = document.getElementById('camera_list');
		while (cameraListDiv.firstChild) {
			cameraListDiv.removeChild(cameraListDiv.firstChild);
		}
		for (var i = 0; i < camera_time_offsets.length; ++i) {
			var cameraObject = camera_time_offsets[i];
			if (cameraObject != null) {
				addCameraRepresentation(cameraObject);
			}
		}
	}
	
	addCameraRepresentation = function(cameraObject) {
		// Render thumbnail as a new span element.
		var div = document.createElement('div');
		div.className = "cameraOffsetItem";
		// create the image, an image can natively be dragged so helpful functionality
		var image = document.createElement('img');
		image.className = "camera_thumb";
		image.setAttribute("src", "/icons/Camera_icon.gif");
		image.setAttribute("title", cameraObject.make + " " + cameraObject.model);
		div.appendChild(image);
		// create an area for the details of this
		var cameraDetailsDiv = document.createElement('div');
		cameraDetailsDiv.className = "cameraOffsetDetails";
		// create the label
		var span = document.createElement('div');
		span.className = "cameraTitle";
		span.textContent = cameraObject.make + " " + cameraObject.model;
		span.style.color = cameraObject.color;
		cameraDetailsDiv.appendChild(span);
		// create the offset text
		span = document.createElement('span');
		span.className = "cameraOffsetTitle";
		span.textContent = "Offset by " + (cameraObject.difftime / 1000) + "s";
		cameraDetailsDiv.appendChild(span);
		// create the offset spinner
		span = document.createElement('span');
		span.className = "cameraOffsetSpin";
		cameraDetailsDiv.appendChild(span);
		var spinboxId = cameraObject.id + "_spinner";
		var spinbox = new SpinBox(span, {'minimum' : -24, 'maximum' : 24});
		spinbox.setValue(cameraObject.holidayOffset);
		spinbox.input.addEventListener('input', function() {
			// listens to the user typing in the edit box
			updateHolidayOffset(cameraObject, spinbox.getValue())
		});
		spinbox.input.addEventListener('change', function() {
			// listens to the user changing the contents from the spin box
			updateHolidayOffset(cameraObject, spinbox.getValue())
		});
		// and the spin text
		span = document.createElement('span');
		span.className = "cameraOffsetSpinLabel";
		span.textContent = "hrs";
		cameraDetailsDiv.appendChild(span);
		// append details to the div
		div.appendChild(cameraDetailsDiv);
		// setup dropping things to this camera representation
		div.addEventListener('dragover', handleSyncDragOver, false);
		div.addEventListener('drop', function(evt) {
			evt.stopPropagation();
			evt.preventDefault();
			// get the thumb ID that has been dropped on this camera ID representation
			var thumbId = evt.dataTransfer.getData('thumbId');
			if (thumbId != null) {
				var imageObject = PicSync.Images.getImageObjectLoaded(thumbId);
				if (imageObject != null) {
					// have the image object, associate this with the camera...
					PicSync.Display.associateImageObjectWithCamera(imageObject, cameraObject)
				}
			}
		}, false);
		// put the span in to the div
		document.getElementById('camera_list').insertBefore(div, null);
	    // and refresh the images we have to synchronise to this new camera object
	    //performImageSynchronisation(cameraObject);
	}
	
	updateHolidayOffset = function(cameraObject, newValue) {
		cameraObject.holidayOffset = newValue;
		//performImageSynchronisation(cameraObject);
	    // store the entire list in memory now
	    storeCameraObjects();
	}

	init = function () {
		slideInnerDiv = $('#middle div.inner');
		slideInnerDiv.css({
			marginLeft: (slideInnerDiv.outerWidth() + 30) +  'px',
			display: 'block'
		});

		$('#sync_button').click(function() {
			var isSlidIn = parseInt(slideInnerDiv.css('marginLeft'), 10) == 25;
			slideInnerDiv.animate({
				marginLeft: isSlidIn ? slideInnerDiv.outerWidth() + 30 : 25
			});
			if (isSlidIn) {
				$('#camera_list_outer').show();
				this.textContent = 'Show Synchronisation Panel';
				
			}
			else {
				$('#camera_list_outer').hide();
				this.textContent = 'Hide Synchronisation Panel';
			}
		});
		
		//Setup the dnd listeners to sync an image
		var dropZone = document.getElementById('sync_drop_zone');
		dropZone.addEventListener('dragover', handleSyncDragOver, false);
		dropZone.addEventListener('drop', handleSyncDrop, false);
		
		// load all our data here
		retrieveCameraObjects();
		// and show them
		showCameraRepesentations();
	}();
	
	return public;
})();
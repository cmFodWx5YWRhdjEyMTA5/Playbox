var displayQrCode = new QRCode(document.getElementById("time_sync_qr"), {
    text: "not-initialised",
    width: 240,
    height: 240,
    colorDark : "#000000",
    colorLight : "#ffffff",
    correctLevel : QRCode.CorrectLevel.H
});

var camera_time_offsets = [];

function makeCode (content) {
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
		var sourceNode = evt.dataTransfer.mozSourceNode;
		if (null != sourceNode) {
			var spanId = sourceNode.getAttribute("spanId");
			if (spanId != null) {
				var file = getImageLoaded(spanId);
				if (file != null) {
					files = [file];
				}
			}
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

function performFileSync(files) {
	if (null == files || files.length == 0) {
		return;
	}
	// Reset progress indicator on new file selection.
	document.getElementById('progress_bar').className = 'loading';
	progress.style.width = '0%';
	progress.textContent = '0%';
	// Loop through the FileList to find and subsequently sync to the QR code in the image
	var filesProcessed = 0;
	var filesToProcess = files.length;
	for (var i = 0, f; f = files[i]; i++) {
		// Only process image files.
		if (!f.type.match('image.*')) {
			updateProgress(++filesProcessed, filesToProcess, files[i].name);
			continue;
		}
		var reader = new FileReader();
		reader.onload = (function(theFile) {
			return function(e) {
				var processedFile = theFile;
				qrcode.callback = (function(dataResult) {
					updateProgress(++filesProcessed, filesToProcess, theFile);
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

function processImageQrCode(sourceFile, dataResult) {
	var isNumber =  /^\d+$/.test(dataResult);
	showMainImageFile(sourceFile);
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

function storeImageOffsetData(sourceFile, qrDate) {
	EXIF.getData(sourceFile, function() {
        var make = EXIF.getTag(this, "Make"),
            model = EXIF.getTag(this, "Model");
        var time = EXIF.getTag(this, "DateTimeOriginal");
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
		// compare the two
		var diffTime = Math.abs(imageDate.getTime() - qrDate.getTime());
        // store this data in the list
        var cameraObject = new Object();
        cameraObject["id"] = (make + " " + model).replace(/\s+/g, '_').toLowerCase();;
        cameraObject["make"] = make;
        cameraObject["model"] = model;
        cameraObject["difftime"] = diffTime;
        cameraObject["exif"] = this;
        camera_time_offsets.push(cameraObject);
        // add the offset for this camera data to the div
        addCameraRepresentation(cameraObject);
    });
}

function addCameraRepresentation(cameraObject) {
	// Render thumbnail as a new span element.
	var div = document.createElement('div');
	// create the image, an image can natively be dragged so helpful functionality
	var image = document.createElement('img');
	image.className = "camera_thumb";
	image.setAttribute("src", "/icons/Camera_icon.gif");
	image.setAttribute("title", cameraObject.make + " " + cameraObject.model);
	div.appendChild(image);
	// create the label
	var span = document.createElement('span');
	span.textContent = cameraObject.make + " " + cameraObject.model + " offset is " + (cameraObject.difftime / 1000) + "s";
	div.appendChild(span);
	// create the offset spinner
	span = document.createElement('span');
	div.appendChild(span);
	var spinboxId = cameraObject.id + "_spinner";
	var spinbox = new SpinBox(span, {'minimum' : -24, 'maximum' : 24});
	// and the spin text
	span = document.createElement('span');
	span.textContent = "hrs";
	div.appendChild(span);
	// put the span in to the div
	document.getElementById('camera_list').insertBefore(div, null);
}

var slideInnerDiv;

$(document).ready(function() {

	slideInnerDiv = $('#middle div.inner');
	slideInnerDiv.css({
		marginLeft: (slideInnerDiv.outerWidth() + 30) +  'px',
		display: 'block'
	});

	$('#middle button').click(function() {
		var isSlidIn = parseInt(slideInnerDiv.css('marginLeft'), 10) == 25;
		slideInnerDiv.animate({
			marginLeft: isSlidIn ? slideInnerDiv.outerWidth() + 30 : 25
		});
		if (isSlidIn) {
			$('#camera_list_outer').show();
		}
		else {
			$('#camera_list_outer').hide();
		}
	});
});  

function readQrFile(a)
{
	alert(a);
}

//Setup the dnd listeners to sync an image
var dropZone = document.getElementById('sync_drop_zone');
dropZone.addEventListener('dragover', handleSyncDragOver, false);
dropZone.addEventListener('drop', handleSyncDrop, false);
var qrcode = new QRCode(document.getElementById("time_sync_qr"), {
    text: "not-initialised",
    width: 240,
    height: 240,
    colorDark : "#000000",
    colorLight : "#ffffff",
    correctLevel : QRCode.CorrectLevel.H
});

function makeCode (content) {
	// if the left margin of the slide inner bit is zero then create an image to show
	// as we are showing the code
	if (parseInt(slideInnerDiv.css('marginLeft'), 10) == 0) {
		qrcode.makeCode(content); // make another code.
	}
}

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
}

function handleSyncDragOver(evt) {
	evt.stopPropagation();
	evt.preventDefault();
	evt.dataTransfer.dropEffect = 'copy'; // Explicitly show this is a copy.
}

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
}

function processImageQrCode(sourceFile, dataResult) {
	var isNumber =  /^\d+$/.test(dataResult);
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
		var fileDate = sourceFile.lastModifiedDate;
		if (fileDate != null && qrDate != null) {
			// compare the two
			var diffTime = Math.abs(fileDate.getTime() - qrDate.getTime());
			alert(diffTime);
		}
		else {
			alert("Sorry the image has no modified time, try another...");
		}
	}
	else {
		alert("Sorry no time image found, try another...");
	}
}

var slideInnerDiv;

$(document).ready(function() {

	slideInnerDiv = $('#middle div.inner');
	slideInnerDiv.css({
		marginLeft: (slideInnerDiv.outerWidth() + 30) +  'px',
		display: 'block'
	});

	$('#middle button').click(function() {
		slideInnerDiv.animate({
			marginLeft: parseInt(slideInnerDiv.css('marginLeft'), 10) == 0 ? slideInnerDiv.outerWidth() + 30 : 0
		});
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

qrcode.callback = readQrFile;
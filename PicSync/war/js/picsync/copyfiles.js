var zip_files;
var zip_targetNames;
var zip_targets;
var zipFileIndex;
var zipFilename;
var zipFileCount;

function copyImageFiles() {
	var destFolder = "~/Desktop/destination";
	var filenameNumber = -1;
	var isNumberingFiles = false;
	
	zip_files = [];
	zip_targetNames = [];
	
	for (var i = 0; i < images_loaded.length; i++) {
		var imageObject = images_loaded[i];
		var newFilename = getExifFilename(imageObject.imageDateOffset);
		// find the extension
		var name = imageObject.file.name;
		var dotIndex = name.lastIndexOf(".");
		var extension = name.substring(dotIndex + 1, name.length);
		// but check the one after this...
		if (i + 1 < images_loaded.length) {
			// there is one after this, will this clash?
			var nextFilename = getExifFilename(images_loaded[i + 1].imageDateOffset);
			if (nextFilename == newFilename) {
				// this next one clashes with this one, number this file
				if (false == isNumberingFiles) {
					// start numbering these files
					filenameNumber = 0;
					isNumberingFiles = true;
				}
			}
			else if (isNumberingFiles){
				// this filename is different, don't number the next set
				isNumberingFiles = false;
			}
		}
		if (filenameNumber >= 0) {
			// increment the filename number and add to the name
			newFilename += " (" + threeDigit(++filenameNumber) + ")";
		}
		// don't forget the extension
		newFilename += "." + extension;
		// push the data to the list to process
		zip_files.push(imageObject.file);
		zip_targetNames.push(newFilename);
		
		//download(imageObject.file, newFilename, imageObject.file.type);
		
		if (false == isNumberingFiles) {
	    	// we don't want to number the next one
	    	filenameNumber = -1;
	    }
	}  
	
	// create a nice filename
	var zipFileDate = new Date();
	zipFilename = zipFileDate.getFullYear() + "_" + twoDigit(zipFileDate.getMonth() + 1) + " images";
	// Reset progress indicator on new file selection.
	document.getElementById('progress_bar').className = 'loading';
	progress.style.width = '0%';
	progress.textContent = '0%';
	// recursively create a zip file
	zipFileCount = 0;
	zipFileIndex = -1;
	zip_targets = [];
	zip_targets.push(new JSZip());
	recursiveZip();
}

function recursiveZip() {
	if (++zipFileIndex < zip_files.length) {
		// process this file
		var file = zip_files[zipFileIndex];
		var newFilename = zip_targetNames[zipFileIndex];
		var progressText = "";
		if (null != file) {
			progressText = file.name;
		}
		updateProgress(zipFileIndex + 1, zip_files.length, "Exporting " + progressText);
		if (null != file) {
			// load this data into the zip
			var reader = new FileReader();
			reader.onload = function(e) {
				// add this data to the zip file
				zip_targets[zip_targets.length - 1].file(newFilename, reader.result, {base64: true});
				if (++zipFileCount >= 50) {
					// this is enough files in the zip, close this one and start another
					dumpZipFileContent();
					// reset the new zip file
					zipFileCount = 0;
					zip_targets.push(new JSZip());
				}
				// and call the function recursively
				recursiveZip();
			};
			reader.readAsArrayBuffer(file);
		}
		else {
			// not an file, just move on
			recursiveZip();
		}
	}
	else if (zipFileCount > 0) {
		// zipping all over, save it now		
		dumpZipFileContent();
	}
}

function dumpZipFileContent(zip) {
	var zipFilePostfix = zip_targets.length - 1;
	var zip = zip_targets[zipFilePostfix];
	var content = zip.generate({type:"blob"});
	download(content, zipFilename + " (" + twoDigit(zipFilePostfix + 1) + ").zip", "application/zip");
	zip_targets[zipFilePostfix] = null;
}

function threeDigit(number) {
	if (number >= 100) {
		return number;
	}
	else if (number >= 10) {
		return "0" + number;
	}
	else {
		return "00" + number;
	}
}
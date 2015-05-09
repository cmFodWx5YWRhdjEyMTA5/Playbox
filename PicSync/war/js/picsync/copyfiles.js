//declare the module
PicSync.Copy = (function () {
	var zip_files;
	var zip_targetNames;
	var zip_targets;
	var zipFileIndex;
	var zipFilename;
	var zipFileCount;
	var zipSplitValue = 50;
	var zipDirectDownload = false;
	
	var copyConfirmation = new CopyDialog();
	var saveasConfirmation = new SafariSaveasDialog();
	var safariDownloadifyId = 0;
	
	var public = {};
	
	getFileExtension = function(file) {
		var name = file.name;
		var dotIndex = name.lastIndexOf(".");
		var extension = "";
		if (dotIndex >= 0) {
			extension = name.substring(dotIndex + 1, name.length);
		}
		return extension;
	}
	
	public.copyImageFiles = function() {
		copyConfirmation.render(PicSync.Images.getImagesLoaded());
	}
	
	public.performZipCopy = function() {
		// close the dialog
		public.cancelDialog();
		
		// and do our copying here
		var destFolder = "~/Desktop/destination";
		var filenameNumber = -1;
		var isNumberingFiles = false;
		
		zip_files = [];
		zip_targetNames = [];
		
		var images_loaded = PicSync.Images.getImagesLoaded();
		for (var i = 0; i < images_loaded.length; i++) {
			var imageObject = images_loaded[i];
			var newFilename = PicSync.TimeSync.getExifFilename(imageObject.imageDateOffset);
			// find the extension
			var extension = getFileExtension(imageObject.file);
			// but check the one after this...
			if (i + 1 < images_loaded.length) {
				// there is one after this, will this clash?
				var nextFilename = PicSync.TimeSync.getExifFilename(images_loaded[i + 1].imageDateOffset);
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
			// push the data to the list to process
			zip_files.push(imageObject.file);
			// remember the filename, including the extension
			zip_targetNames.push(newFilename + "." + extension);
			
			// also we want to do the partner files here
			for (var j = 0; j < imageObject.partners.length; ++j) {
				// for each partner file, get the new filename
				var partnerFile = imageObject.partners[j];
				extension = getFileExtension(partnerFile);
				// and put this partner in the list to add to the zip
				zip_files.push(partnerFile);
				// remember the filename, including the extension
				zip_targetNames.push(newFilename + "." + extension);
			}
			
			if (false == isNumberingFiles) {
		    	// we don't want to number the next one
		    	filenameNumber = -1;
		    }
		}  
		
		// create a nice filename
		var zipFileDate = new Date();
		zipFilename = zipFileDate.getFullYear() + "_" + twoDigit(zipFileDate.getMonth() + 1) + " images";
		// Reset progress indicator on new file selection.
		PicSync.Progress.startProgress();
		// recursively create a zip file
		zipFileCount = 0;
		zipFileIndex = -1;
		zip_targets = [];
		zip_targets.push(new JSZip());
		recursiveZip();
	}
	
	recursiveZip = function() {
		if (++zipFileIndex < zip_files.length) {
			// process this file
			var file = zip_files[zipFileIndex];
			var newFilename = zip_targetNames[zipFileIndex];
			var progressText = "";
			if (null != file) {
				progressText = file.name;
			}
			PicSync.Progress.updateProgress(zipFileIndex + 1, zip_files.length, "Exporting " + progressText);
			if (null != file) {
				if (zipDirectDownload) {
					// just straight download
					downloadFileContent(file, newFilename, file.type);
					// and do the next one
					recursiveZip();
				}
				else {
					// load this data into the zip
					var reader = new FileReader();
					reader.onload = function(e) {
						// add this data to the zip file
						zip_targets[zip_targets.length - 1].file(newFilename, reader.result, {base64: true});
						if (++zipFileCount >= zipSplitValue) {
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
	
	dumpZipFileContent = function(zip) {
		var zipFilePostfix = zip_targets.length - 1;
		var zip = zip_targets[zipFilePostfix];
		
		if (PicSync.isSafari()) {
			// here we have a little problem in that Safari only downloads files in an onclick
			// function, so we have to show a little dialog for the user to click OK, grr...
			// but OK, show the dialog then
			//load(file, newFilename);
			saveasConfirmation.render(zip, zipFilename + " (" + twoDigit(zipFilePostfix + 1) + ").zip");
		}
		else {
			var content = zip.generate({type:"blob"});
			downloadFileContent(content, zipFilename + " (" + twoDigit(zipFilePostfix + 1) + ").zip", "application/zip");
			zip_targets[zipFilePostfix] = null;
		}
	}
	
	downloadFileContent = function(file, newFilename, fileType) {
		//download(file, newFilename, fileType);
		saveAs(file, newFilename);
		//saveAs(file, newFilename);
	}
	
	threeDigit = function(number) {
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
	
	public.cancelDialog = function() {
		copyConfirmation.ok();
	}
	
	function CopyDialog(){
		var noZipRadio;
		var copyPackageCheck;
		var copyPackageNumber;
		var downloadFilesWarning;
	    this.render = function(images_loaded){
	        var winW = window.innerWidth;
	        var winH = window.innerHeight;
	        var dialogoverlay = document.getElementById('dialogoverlay');
	        var dialogbox = document.getElementById('copydialogbox');
	        dialogoverlay.style.display = "block";
	        dialogoverlay.style.height = winH+"px";
	        dialogbox.style.left = (winW/2) - (550 * .5)+"px";
	        dialogbox.style.top = "100px";
	        dialogbox.style.display = "block";
	        document.getElementById('copydialogboxhead').innerHTML = "Select how to download the collected " + images_loaded.length + " images...";
	        
	        // setup input change listeners
	        noZipRadio = document.getElementById("copy_input_nozip");
	        copyPackageCheck = document.getElementById("copy_package_check");
	        copyPackageNumber = document.getElementById("copy_package_number");
	        downloadFilesWarning = document.getElementById("download_nocheck");
	        
	        // process for Safari - just allow zip files...
	        if (PicSync.isSafari()) {
	        	$("#copy_input_nozip").hide();
	        	copyPackageCheck.checked = true;
	        	zipSplitValue = 5;
		        copyPackageNumber.value = zipSplitValue;
	        }
	        else {
	        	$("#safari_desciption").hide();
	        	zipSplitValue = 50;
		        copyPackageNumber.value = zipSplitValue;
	        }
	        
	        // add change listeners
	        noZipRadio.addEventListener('change', function() {
				// listens to the user changing the contents
	        	copyConfirmation.updateContents();
			});
	        copyPackageCheck.addEventListener('change', function() {
				// listens to the user changing the contents
	        	copyConfirmation.updateContents();
			});
	        document.getElementById("copy_input_zip").addEventListener('change', function() {
				// listens to the user changing the contents
	        	copyConfirmation.updateContents();
			});
	        document.getElementById("copy_input_zip").addEventListener('change', function() {
				// listens to the user changing the contents
	        	copyConfirmation.updateContents();
			});
	        // and listen for the copy button to do our business
	        copyPackageNumber.addEventListener('change', function() {
				// listens to the user changing the contents
	        	copyConfirmation.updateContents();
			});
	        // update contents to set data
	        copyConfirmation.updateContents();
	    }
	    this.updateContents = function() {
	    	if (noZipRadio.checked) {
	    		// not zipping
	    		copyPackageCheck.checked = false;
	    		copyPackageCheck.disabled = true;
	    		copyPackageNumber.disabled = true;
	    		$("#download_nocheck").show();
	    	}
	    	else {
	    		// zipping
	    		copyPackageCheck.disabled = false;
	    		copyPackageNumber.disabled = false;
	    		if (copyPackageCheck.checked) {
		    		copyPackageNumber.disabled = false;
		    	}
		    	else {
		    		copyPackageNumber.disabled = true;
		    	}
	    		$("#download_nocheck").hide();
	    	}
	    	// set all the member data
	    	zipSplitValue = copyPackageNumber.value;
        	if (!copyPackageCheck.checked) {
        		// do not split anything, well lets keep it under 1000
        		zipSplitValue = 1000;
        	}
        	zipDirectDownload = noZipRadio.checked;
	    }
		this.ok = function(){
			document.getElementById('copydialogbox').style.display = "none";
			document.getElementById('dialogoverlay').style.display = "none";
		}
	}
	
	function SafariSaveasDialog(){
	    this.render = function(saveFile, saveFilename){
	        var winW = window.innerWidth;
	        var winH = window.innerHeight;
	        var dialogoverlay = document.getElementById('dialogoverlay');
	        var dialogbox = document.getElementById('saveasdialogbox');
	        var dialogBody = document.getElementById('saveasdialogboxbody');
	        dialogoverlay.style.display = "block";
	        dialogoverlay.style.height = winH+"px";
	        dialogbox.style.left = (winW/2) - (550 * .5)+"px";
	        dialogbox.style.top = "100px";
	        dialogbox.style.display = "block";
	        document.getElementById('saveasdialogboxhead').innerHTML = "Download the packaged file(s) in Safari...";
	        // create the downloadify handler to process this zip file download, first create a div in which to put
	        // the downloadify button to use
	        var titleDiv = document.createElement('div');
			titleDiv.className = "saveas_title";
			titleDiv.textContent = saveFilename;
			titleDiv.id = "downloadify_" + ++safariDownloadifyId;
	        dialogBody.appendChild(titleDiv);
	        // and add the save as button from downloadify
	        Downloadify.create(titleDiv.id, {
				filename: saveFilename,
				data: function(){
					return saveFile.generate({type:"base64"});
				},
				dataType: 'base64',
				onComplete: function() { 
					// remove the save option from the dialog
					dialogBody.removeChild(titleDiv);
				},
				onCancel: function(){ alert('You have cancelled the saving of this file.'); },
				onError: function(){ alert('You must put something in the File Contents or there will be nothing to save!'); },
				swf: 'media/downloadify.swf',
				downloadImage: 'images/download.png',
				width: 100,
				height: 30,
				transparent: true,
				append: true
			});
	        document.getElementById("saveas_cancelbutton").addEventListener("click", function() {
	        	// close the dialog
	        	saveasConfirmation.closeDialog();
	        });
	    }
		this.closeDialog = function(){
			document.getElementById('saveasdialogbox').style.display = "none";
			document.getElementById('dialogoverlay').style.display = "none";
		}
	}
	
	init = function() {
		// initialise this module here
		var isFileSaverSupported = false;
		try {
		    isFileSaverSupported = !!new Blob;
		} catch (e) {}
		if (!isFileSaverSupported) {
			alert("Sorry... this browser does not support the features you need to copy files, please use another - tested on Chrome so try that?...");
		}
	}();
	
	return public;
})();
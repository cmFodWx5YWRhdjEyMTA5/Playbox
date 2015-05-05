//declare the module
PicSync.Sync = (function () {
	
	var public = {};

	public.performImageSynchronisation = function(cameraObject) {
		// now we need to re-sort the list
		PicSync.Images.sortImages();
		var images_loaded = PicSync.Images.getImagesLoaded();
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
				loadImage.parseMetaData(
					sourceFile,
				    function (data) {
				        if (!data.imageHead) {
				            return;
				        }
				        // get the data for this then
				        var imageDate = public.getExifImageDate(data.exif, sourceFile);
				        var imageDateIncOffset = PicSync.TimeSync.getExifImageDateIncOffset(data.exif, sourceFile, cameraObject.id);
						// set this data in the image object
						imageObject["imageDate"] = imageDate;
						imageObject["imageDateOffset"] = imageDateIncOffset;
						imageObject["cameraColor"] = cameraObject.color;
						// and update the representation
						updateImageRepresentation(imageObject);
				    },
				    {
				        maxMetaDataSize: 262144,
				        disableImageHead: false
				    }
				);
			}
			// remember the previous ID in order to put the thumbnail at the correct location
			previousThumbId = imageObject.thumbId;
		}
	}
	
	init = function() {
		// initialise this module here
	}();

	return public;
})();
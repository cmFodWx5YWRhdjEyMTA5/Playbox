//declare the global namespace, and helpful function
var PicSync = (function () {
	// code
	var public = {};
	
	public.appendScript = function(jsFilePath) {
		var js = document.createElement("script");
		js.type = "text/javascript";
		js.src = jsFilePath;
		document.body.appendChild(js);
	};
	
	init = function() {
		// append all the scripts we require
		public.appendScript("./js/picsync/progress.js");
		public.appendScript("./js/picsync/imagebrowser.js");
		public.appendScript("./js/picsync/imagedisplay.js");
		public.appendScript("./js/picsync/imagesync.js");
		public.appendScript("./js/picsync/timesync.js");
		public.appendScript("./js/picsync/copyfiles.js");

		window.onbeforeunload = function() {
		    return "If you leave this page, you will lose any unsaved changes.";
		}
	}();
	
	return public;
})();
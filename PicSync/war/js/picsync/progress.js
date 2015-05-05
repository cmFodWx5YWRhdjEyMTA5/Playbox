// declare the module
PicSync.Progress = (function () {
	// code
	var public = {};
	
	var progressElement = document.querySelector('.percent');
	
	public.startProgress = function () {
		// Reset progress indicator to show the bar starting
		document.getElementById('progress_bar').className = 'loading';
		progressElement.style.width = '0%';
		progressElement.textContent = '0%';
    };
    
    public.updateProgress = function(loaded, total, description) {
    	var percentLoaded = Math.round((loaded / total) * 100);
    	// Increase the progress bar length.
    	if (percentLoaded < 100) {
    		progressElement.style.width = percentLoaded + '%';
    		progressElement.textContent = percentLoaded + "% " + description;
    	}
    	else {
    		// Ensure that the progress bar displays 100% at the end for a couple seconds
    		progressElement.style.width = '100%';
    		progressElement.textContent = '100%';
    		setTimeout("document.getElementById('progress_bar').className='';", 2000);
    	}
    };
	
	init = function() {
		// initialise this module here
	}();
	
	return public;
})();
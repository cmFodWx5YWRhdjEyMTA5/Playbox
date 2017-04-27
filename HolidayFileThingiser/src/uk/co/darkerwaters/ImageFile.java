package uk.co.darkerwaters;

import java.io.File;

class ImageFile {
	File file;
	Boolean isLocationSet = null;
	
	public ImageFile(File file) {
		this.file = file;
	}

	@Override
	public String toString() {
		return file.getName() + 
				(this.isLocationSet == null ? "" : (this.isLocationSet ? " located" : " not located")); 
	}

	public String getName() {
		return this.file.getName();
	}
}
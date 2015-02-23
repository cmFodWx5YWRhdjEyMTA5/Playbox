package uk.co.darkerwaters.client;

public interface EmoTrackListener {
	void handleError(Throwable error);
	void loadingComplete();
}

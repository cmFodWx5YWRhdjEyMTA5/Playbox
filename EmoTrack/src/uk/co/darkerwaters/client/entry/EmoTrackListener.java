package uk.co.darkerwaters.client.entry;

public interface EmoTrackListener {
	void handleError(Throwable error);
	void loadingComplete();
}

package uk.co.darkerwaters.emotrack;

import java.net.CookieManager;
import java.util.logging.Logger;

import com.gdevelop.gwt.syncrpc.ProxySettings;
import com.gdevelop.gwt.syncrpc.SyncProxy;
import com.google.gwt.user.client.rpc.AsyncCallback;

import uk.co.darkerwaters.client.tracks.TrackPointData;
import uk.co.darkerwaters.client.tracks.TrackPointServiceAsync;
import android.os.AsyncTask;
import android.text.Html;
import android.widget.TextView;

public class RpcTrackPointAction extends AsyncTask<MainActivity, Void, MainActivity>{

	TrackPointServiceAsync trackService;
	CookieManager cookieManager;
	
	public RpcTrackPointAction(CookieManager cm) {
		this.cookieManager = cm;
	}
	
	@Override
	protected MainActivity doInBackground(MainActivity... params) {
		// create the service
		// Use 10.0.2.2 for Hosted Emulator Loopback interface
		SyncProxy.setBaseURL(MainActivity.BASE_URL + "/spawebtest/");
		this.trackService = SyncProxy.createProxy(TrackPointServiceAsync.class,
				new ProxySettings().setCookieManager(this.cookieManager));
		return params[0];
	}
	
	@Override
	protected void onPostExecute(final MainActivity result) {
		this.trackService.getTrackPoints(
			new AsyncCallback<TrackPointData[]>() {
				@Override
				public void onFailure(Throwable caught) {
					throw new RuntimeException(caught);
				}
				@Override
				public void onSuccess(final TrackPointData[] data) {
					result.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							System.out.println("there are " + data.length + " items");
							/*String resultString = "there are " + data.length + " items";
							
							final TextView tv = (TextView) result.findViewById(R.id.result);
							tv.setText(Html.fromHtml(resultString));*/
						}
					});
				}
			});
		super.onPostExecute(result);
	}

}

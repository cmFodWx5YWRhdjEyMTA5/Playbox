package uk.co.darkerwaters.staveinvaders.actvities.handlers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.games.Game;
import uk.co.darkerwaters.staveinvaders.games.GameScore;
import uk.co.darkerwaters.staveinvaders.games.PlayNothing;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Clef;
import uk.co.darkerwaters.staveinvaders.notes.Note;
import uk.co.darkerwaters.staveinvaders.views.MusicView;

public class AttributionRecyclerAdapter extends RecyclerView.Adapter<AttributionRecyclerAdapter.MyViewHolder> {

    private final Application application;
    private final Context context;

    public static class Attribution {
        final String imageName;
        final String attribution;

        Attribution(String imageName, String attribution) {
            this.imageName = imageName;
            this.attribution = attribution;
        }
    }

    private List<Attribution> dataList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        final public ImageView imageView;
        final public WebView attributionView;

        public MyViewHolder(View view) {
            super(view);
            this.imageView = view.findViewById(R.id.attributionImage);
            this.attributionView = view.findViewById(R.id.attributionText);
        }
    }


    public AttributionRecyclerAdapter(Application application, Context context) {
        this.context = context;
        this.application = application;
        this.dataList = new ArrayList<Attribution>();

        // load in the files and create attributions from them
        try {
            String[] fileList = context.getAssets().list("attributions");
            for (String filename : fileList) {
                // open each file
                InputStream is = context.getAssets().open("attributions/" + filename);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                // create the object here
                String imageName = filename.toLowerCase().replace(".html", "");
                Attribution attribution = new Attribution(imageName, new String(buffer, "UTF-8"));
                this.dataList.add(attribution);
            }
        } catch (IOException e) {
            Log.error("Filed to find attribution files", e);
        }
    }

    public boolean isEmpty() {
        return this.dataList.isEmpty();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_attribution_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Attribution attribution = dataList.get(position);

        int id = context.getResources().getIdentifier(attribution.imageName, "drawable", context.getPackageName());
        holder.imageView.setImageResource(id);
        holder.attributionView.loadData(attribution.attribution, "text/html", "UTF-8");
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}

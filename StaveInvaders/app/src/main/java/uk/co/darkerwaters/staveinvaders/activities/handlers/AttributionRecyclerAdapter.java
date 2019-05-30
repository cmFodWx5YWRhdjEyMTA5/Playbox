package uk.co.darkerwaters.staveinvaders.activities.handlers;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.activities.fragments.GameParentCardHolder;
import uk.co.darkerwaters.staveinvaders.application.Log;

public class AttributionRecyclerAdapter extends RecyclerView.Adapter<AttributionRecyclerAdapter.MyViewHolder> {

    private final Application application;
    private final Context context;

    static class Attribution {
        final String imageName;
        final String attribution;

        Attribution(String imageName, String attribution) {
            this.imageName = imageName;
            this.attribution = attribution;
        }
    }

    private final List<Attribution> dataList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final WebView attributionView;
        final TextView titleView;

        MyViewHolder(View view) {
            super(view);
            this.titleView = view.findViewById(R.id.attributionTitle);
            this.imageView = view.findViewById(R.id.attributionImage);
            this.attributionView = view.findViewById(R.id.attributionText);
        }
    }


    public AttributionRecyclerAdapter(Application application, Context context) {
        this.context = context;
        this.application = application;
        this.dataList = new ArrayList<>();

        // load in the files and create attributions from them
        try {
            String[] fileList = context.getAssets().list("attributions");
            if (null != fileList) {
                for (String filename : fileList) {
                    // open each file
                    InputStream is = context.getAssets().open("attributions/" + filename);
                    int size = is.available();
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    is.close();
                    // create the object here
                    String imageName = filename.toLowerCase().replace(".html", "");
                    Attribution attribution = new Attribution(imageName, new String(buffer, StandardCharsets.UTF_8));
                    this.dataList.add(attribution);
                }
            }
            else {
                Log.error("No files in attributions found");
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

        holder.titleView.setText(attribution.imageName);
        int id = context.getResources().getIdentifier(attribution.imageName, "drawable", context.getPackageName());
        if (0 == id) {
            // this is not a drawable, is this an asset?
            Bitmap bitmap = GameParentCardHolder.getBitmapFromAssets("games/" + attribution.imageName + ".jpg", context);
            if (null != bitmap) {
                // this was loaded ok, use this
                holder.imageView.setImageBitmap(bitmap);
            }
            else {
                // try an icon?
            }
        }
        else {
            holder.imageView.setImageResource(id);
        }
        holder.attributionView.loadData(attribution.attribution, "text/html", "UTF-8");
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}

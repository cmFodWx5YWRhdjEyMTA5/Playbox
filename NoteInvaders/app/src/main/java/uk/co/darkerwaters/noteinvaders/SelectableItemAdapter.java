package uk.co.darkerwaters.noteinvaders;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.List;

public class SelectableItemAdapter extends RecyclerView.Adapter<SelectableItemAdapter.MyViewHolder> {

    private SelectableItemActivity mContext;
    private List<? extends SelectableItem> itemList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, count, progressText;
        public ProgressBar progressBar;
        public ImageView thumbnail;
        SelectableItem item;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            count = (TextView) view.findViewById(R.id.count);
            progressBar = (ProgressBar) view.findViewById(R.id.score_progress);
            progressText = (TextView) view.findViewById(R.id.progress_text);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        }
    }


    public SelectableItemAdapter(SelectableItemActivity mContext, List<? extends SelectableItem> itemList) {
        this.mContext = mContext;
        this.itemList = itemList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int itemList) {
        // create the view
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_instrument, parent, false);
        // and return the holder for the view
        return new MyViewHolder(itemView);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull MyViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        // refresh the contents, might have changed
        refreshContent(holder);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        // setup the holder
        holder.item = this.itemList.get(position);
        // and refresh the content
        refreshContent(holder);
        // setup the click listener
        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // called when the user click the view
                SelectableItemAdapter.this.mContext.onSelectableItemClicked(holder.item);
            }
        });
    }

    private void refreshContent(MyViewHolder holder) {
        holder.title.setText(holder.item.getTitle(mContext));
        holder.count.setText(holder.item.getSubtitle(mContext));
        int progress = holder.item.getProgress(mContext);
        if (progress < 0) {
            holder.progressBar.setVisibility(View.GONE);
            holder.progressText.setVisibility(View.GONE);
        }
        else {
            holder.progressBar.setProgress(progress);
            holder.progressText.setText(Integer.toString(progress) + "%");
        }
        // and have the card add it's things as it wants
        holder.item.onItemRefreshed(mContext, holder);
    }

    @Override
    public void onViewRecycled(MyViewHolder holder) {
        super.onViewRecycled(holder);
        for (SelectableItem item : this.itemList) {
            item.onDestroy(holder);
        }
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }
}

package uk.co.darkerwaters.noteinvaders;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

public class SelectableItemAdapter extends RecyclerView.Adapter<SelectableItemAdapter.MyViewHolder> {

    private SelectableItemActivity mContext;
    private List<? extends SelectableItem> itemList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, count;
        public ImageView thumbnail;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            count = (TextView) view.findViewById(R.id.count);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        }
    }


    public SelectableItemAdapter(SelectableItemActivity mContext, List<? extends SelectableItem> itemList) {
        this.mContext = mContext;
        this.itemList = itemList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create the view
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_instrument, parent, false);
        // and return the holder for the view
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final SelectableItem item = this.itemList.get(position);
        holder.title.setText(item.getName());
        holder.count.setText(item.getSubtitle());

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // called when the user click the view
                SelectableItemAdapter.this.mContext.onSelectableItemClicked(item);
            }
        });

        // and have the card add it's things as it wants
        item.onBindViewHolder(mContext, holder);
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

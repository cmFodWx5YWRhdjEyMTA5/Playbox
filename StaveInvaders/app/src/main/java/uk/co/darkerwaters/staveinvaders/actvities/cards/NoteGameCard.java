package uk.co.darkerwaters.staveinvaders.actvities.cards;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.actvities.handlers.MainRecyclerAdapter;

public class NoteGameCard extends MainRecyclerAdapter.Card {

    private TextView itemTitle;
    private ImageView itemImage;
    private TextView itemDetail;

    public NoteGameCard() {

    }

    @Override
    public void onCardCreated(View v) {
        super.onCardCreated(v);
        // card is created, find all our children views and stuff here
        this.itemImage = (ImageView)v.findViewById(R.id.item_image);
        this.itemTitle = (TextView)v.findViewById(R.id.item_title);
        this.itemDetail = (TextView)v.findViewById(R.id.item_detail);
    }

    @Override
    public int getLayoutId() {
        return R.layout.card_notes;
    }

    @Override
    public void initialiseCard(MainRecyclerAdapter.ViewHolder viewHolder) {
        this.itemTitle.setText("hello");
        this.itemDetail.setText("this is nice isn't it");
        this.itemImage.setImageResource(R.drawable.piano);

    }
}

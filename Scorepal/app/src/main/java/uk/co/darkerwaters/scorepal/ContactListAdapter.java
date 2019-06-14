package uk.co.darkerwaters.scorepal;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactListAdapter extends CursorAdapter implements Filterable {

    private ContentResolver mContent;

    public static final String[] PEOPLE_PROJECTION = new String[] {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
            ContactsContract.CommonDataKinds.Email.DATA
    };

    public ContactListAdapter(Context context) {
        super(context, createCursor(context), true);

        this.mContent = context.getContentResolver();
    }

    private static Cursor createCursor(Context context) {
        return context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                ContactListAdapter.PEOPLE_PROJECTION, null, null, null);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View contactCard = inflater.inflate(R.layout.card_contact, parent, false);
        // set the data on this new card
        setCardData(contactCard, cursor);
        // and return it
        return contactCard;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        setCardData(view, cursor);
    }

    private void setCardData(View contactCard, Cursor cursor) {

        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        String image = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
        String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

        // do the name
        TextView textView = contactCard.findViewById(R.id.ccontName);
        textView.setText(name);
        // and the email
        textView = contactCard.findViewById(R.id.ccontNo);
        textView.setText(email);

        if (null != image && !image.isEmpty()) {
            // there is an image URI - use the image for niceness
            ImageView imageView = contactCard.findViewById(R.id.ccontImage);
            if (null != imageView) {
                // have a view, set the image
                imageView.setImageURI(Uri.parse(image));
            }
        }
    }

    @Override
    public String convertToString(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (getFilterQueryProvider() != null) {
            return getFilterQueryProvider().runQuery(constraint);
        }

        StringBuilder buffer = null;
        String[] args = null;
        if (constraint != null) {
            buffer = new StringBuilder();
            buffer.append("UPPER(");
            buffer.append(ContactsContract.Contacts.DISPLAY_NAME);
            buffer.append(") GLOB ?");
            args = new String[] { constraint.toString().toUpperCase() + "*" };
        }

        return mContent.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PEOPLE_PROJECTION,
                buffer == null ? null : buffer.toString(), args,
                null);
    }
}

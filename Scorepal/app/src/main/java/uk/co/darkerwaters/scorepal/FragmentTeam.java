package uk.co.darkerwaters.scorepal;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class FragmentTeam extends Fragment {

    private TextView title;
    private AutoCompleteTextView playerName;
    private AutoCompleteTextView partnerName;
    private CursorAdapter adapter;

    public interface OnFragmentInteractionListener {
        // the listener to set the data from this fragment
        void onAttachFragment(FragmentTeam fragmentTeam);
    }
    
    private OnFragmentInteractionListener listener;

    private int teamNumber = 0;

    public FragmentTeam() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parent = inflater.inflate(R.layout.fragment_fragment_team, container, false);

        this.title = parent.findViewById(R.id.titleText);
        this.playerName = parent.findViewById(R.id.playerAutoTextView);
        this.partnerName = parent.findViewById(R.id.playerPartnerAutoTextView);

        // set our labels
        setLabels(this.teamNumber);
        // setup our adapters here
        setupAdapters();
        return parent;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
            // and inform this listener of our attachment
            listener.onAttachFragment(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void setAutoCompleteAdapter(CursorAdapter adapter) {
        this.adapter = adapter;
        // setup our adapters here
        setupAdapters();
    }

    private void setupAdapters() {
        if (null != this.playerName) {
            this.playerName.setAdapter(this.adapter);
        }
        if (null != this.partnerName) {
            this.partnerName.setAdapter(this.adapter);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void setLabels(int teamNumber) {
        this.teamNumber = teamNumber;

        if (null != this.title) {
            switch (this.teamNumber) {
                case 1:
                    this.title.setText(R.string.team_one_title);
                    this.playerName.setHint(R.string.default_playerOneName);
                    this.partnerName.setHint(R.string.default_playerOnePartnerName);
                    break;
                case 2:
                    this.title.setText(R.string.team_two_title);
                    this.playerName.setHint(R.string.default_playerTwoName);
                    this.partnerName.setHint(R.string.default_playerTwoPartnerName);
                    break;
            }
        }
    }
}

package uk.co.darkerwaters.staveinvaders.actvities.handlers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.games.Game;
import uk.co.darkerwaters.staveinvaders.games.GameScore;
import uk.co.darkerwaters.staveinvaders.games.PlayNothing;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Clef;
import uk.co.darkerwaters.staveinvaders.notes.Note;
import uk.co.darkerwaters.staveinvaders.views.MusicView;

public class MissedTargetRecyclerAdapter extends RecyclerView.Adapter<MissedTargetRecyclerAdapter.MyViewHolder> {

    private final Application application;
    private final Context context;

    public static class MissedTarget {
        final Chord target;
        int missCount = 0;
        final Clef clef;
        GameScore.Miss misfires = null;

        MissedTarget(GameScore.Miss miss) {
            this.clef = miss.clef;
            this.target = miss.target;
            this.misfires = miss;
        }

        MissedTarget(GameScore.Hit hit) {
            this.clef = hit.clef;
            this.target = hit.target;
            this.missCount = hit.count;
        }
    }

    private List<MissedTarget> dataList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView numberMisses;
        public TextView numberMisfires;
        public TextView explanation;
        public MusicView noteView;

        public MyViewHolder(View view) {
            super(view);
            explanation = view.findViewById(R.id.textViewExplanation);
            noteView = view.findViewById(R.id.noteMusicView);
            numberMisses = view.findViewById(R.id.textViewNumberMisses);
            numberMisfires = view.findViewById(R.id.textViewNumberMissfires);
        }
    }


    public MissedTargetRecyclerAdapter(Application application, Context context, Clef clef) {
        this.context = context;
        this.application = application;
        this.dataList = new ArrayList<MissedTarget>();

        // get the last score
        GameScore score = GameScore.GetLastInstance(false);
        // and get all the misses and misfires for this clef
        for (GameScore.Hit miss : score.getMisses()) {
            // seperate and combine
            if (miss.clef == clef) {
                putHitInList(miss);
            }
        }
        // go through the list and seperate and combine
        for (GameScore.Miss miss : score.getMisfires()) {
            if (miss.clef == clef) {
                // this is ours
                putMissInList(miss);
            }
        }
    }

    public boolean isEmpty() {
        return this.dataList.isEmpty();
    }

    private void putHitInList(GameScore.Hit hit) {
        boolean isCombined = false;
        for (int i = 0; i < dataList.size(); ++i) {
            MissedTarget existing = dataList.get(i);
            if (existing.target.equals(hit.target)) {
                // this is the same target, probably for a different tempo, combine
                existing.missCount += hit.count;
                isCombined = true;
                break;
            }
        }
        if (false == isCombined) {
            // just add to the list
            this.dataList.add(new MissedTarget(hit));
        }
    }

    private void putMissInList(GameScore.Miss miss) {
        boolean isCombined = false;
        for (int i = 0; i < dataList.size(); ++i) {
            MissedTarget existing = dataList.get(i);
            if (existing.target.equals(miss.target)) {
                // this is the same target, probably for a different tempo, combine
                if (existing.misfires == null) {
                    // there are no misfires, just use this one
                    existing.misfires = miss;
                }
                else {
                    // combine this miss with the one already encountered
                    for (GameScore.Hit hit : miss.actuals) {
                        existing.misfires.addActual(hit.clef, hit.target, hit.count);
                    }
                }
                isCombined = true;
                break;
            }
        }
        if (false == isCombined) {
            // just add to the list
            this.dataList.add(new MissedTarget(miss));
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.missed_target_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        MissedTarget miss = dataList.get(position);
        //holder.title.setText(miss.target.getTitle());

        // show the single note, create the game that doesn't play
        Game game = new Game(this.application, PlayNothing.class.getName(), miss.clef, new Chord[] {miss.target});
        // set this to the correct clef
        holder.noteView.setPermittedClefs(new Clef[] {miss.clef});
        // and the game to show the note for
        holder.noteView.setIsScaleView(false);
        holder.noteView.setIsShowTempo(false);
        holder.noteView.setActiveGame(game);

        holder.numberMisses.setText(Integer.toString(miss.missCount));
        if (miss.misfires == null) {
            holder.numberMisfires.setText("0");
            holder.explanation.setText("");
        }
        else {
            holder.numberMisfires.setText(Integer.toString(miss.misfires.actuals.size()));
            // create the explanation too, everything we missed
            StringBuilder builder = new StringBuilder();
            Collections.sort(miss.misfires.actuals, new Comparator<GameScore.Hit>() {
                @Override
                public int compare(GameScore.Hit hit, GameScore.Hit t1) {
                    return hit.target.root().getNotePrimative() - t1.target.root().getNotePrimative();
                }
            });

            boolean firstRun = true;
            for (GameScore.Hit actual : miss.misfires.actuals) {
                if (firstRun) {
                    // add the line break
                    firstRun = false;
                } else {
                    // add the separation between notes
                    builder.append(", ");
                }
                Note note = actual.target.root();
                builder.append(note.getName());
                builder.append(" ");
                switch (actual.count) {
                    case 1:
                        builder.append(context.getResources().getString(R.string.once));
                        break;
                    case 2:
                        builder.append(context.getResources().getString(R.string.twice));
                        break;
                    default:
                        builder.append(actual.count);
                        builder.append(" ");
                        builder.append(context.getResources().getString(R.string.times));
                        break;
                }
            }
            holder.explanation.setText(builder.toString().trim());
            holder.explanation.setMovementMethod(new ScrollingMovementMethod());
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}

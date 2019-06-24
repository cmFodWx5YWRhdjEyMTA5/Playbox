package uk.co.darkerwaters.scorepal.activities.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.Point;
import uk.co.darkerwaters.scorepal.score.TennisMatch;
import uk.co.darkerwaters.scorepal.score.TennisScore;

public class LayoutTennisSummary extends LayoutScoreSummary {

    private static final int K_TITLES = 0;
    private static final int K_TEAM1 = 1;
    private static final int K_TIE = 2;
    private static final int K_TEAM2 = 3;

    private static final int K_ROWS = 4;
    private static final int K_COLS = 6;

    private View parent;

    private TextView teamOneTitle;
    private TextView teamTwoTitle;
    
    private TextView[][] textViews;

    private final TextView[] totalPoints = new TextView[2];
    private final TextView[] breakPoints = new TextView[2];

    private ViewGroup moreLessLayout;
    private Button moreLessButton;
    private Button deleteButton;
    private Button shareButton;

    private boolean isMoreShown;

    public LayoutTennisSummary() {
    }


    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        // create the layout on the parent view
        this.parent = inflater.inflate(R.layout.layout_tennis_summary, container, false);

        this.teamOneTitle = this.parent.findViewById(R.id.textViewTeamOne);
        this.teamTwoTitle = this.parent.findViewById(R.id.textViewTeamTwo);

        // create the array - to prevent to many members
        this.textViews = new TextView[K_ROWS][K_COLS];

        // find all the titles
        this.textViews[K_TITLES][0] = this.parent.findViewById(R.id.textViewPointsTitle);
        this.textViews[K_TITLES][1] = this.parent.findViewById(R.id.textViewSet1Title);
        this.textViews[K_TITLES][2] = this.parent.findViewById(R.id.textViewSet2Title);
        this.textViews[K_TITLES][3] = this.parent.findViewById(R.id.textViewSet3Title);
        this.textViews[K_TITLES][4] = this.parent.findViewById(R.id.textViewSet4Title);
        this.textViews[K_TITLES][5] = this.parent.findViewById(R.id.textViewSet5Title);

        // find all the text textViews here for team one
        this.textViews[K_TEAM1][0] = this.parent.findViewById(R.id.teamOne_Points);
        this.textViews[K_TEAM1][1] = this.parent.findViewById(R.id.teamOne_setOne);
        this.textViews[K_TEAM1][2] = this.parent.findViewById(R.id.teamOne_setTwo);
        this.textViews[K_TEAM1][3] = this.parent.findViewById(R.id.teamOne_setThree);
        this.textViews[K_TEAM1][4] = this.parent.findViewById(R.id.teamOne_setFour);
        this.textViews[K_TEAM1][5] = this.parent.findViewById(R.id.teamOne_setFive);

        // and the text views for the tie-break results
        this.textViews[K_TIE][0] = null;
        this.textViews[K_TIE][1] = this.parent.findViewById(R.id.tieBreak_setOne);
        this.textViews[K_TIE][2] = this.parent.findViewById(R.id.tieBreak_setTwo);
        this.textViews[K_TIE][3] = this.parent.findViewById(R.id.tieBreak_setThree);
        this.textViews[K_TIE][4] = this.parent.findViewById(R.id.tieBreak_setFour);
        this.textViews[K_TIE][5] = this.parent.findViewById(R.id.tieBreak_setFive);

        // and team two
        this.textViews[K_TEAM2][0] = this.parent.findViewById(R.id.teamTwo_Points);
        this.textViews[K_TEAM2][1] = this.parent.findViewById(R.id.teamTwo_setOne);
        this.textViews[K_TEAM2][2] = this.parent.findViewById(R.id.teamTwo_setTwo);
        this.textViews[K_TEAM2][3] = this.parent.findViewById(R.id.teamTwo_setThree);
        this.textViews[K_TEAM2][4] = this.parent.findViewById(R.id.teamTwo_setFour);
        this.textViews[K_TEAM2][5] = this.parent.findViewById(R.id.teamTwo_setFive);

        // set the colour for team one
        int color = parent.getContext().getColor(R.color.teamOneColor);
        this.teamOneTitle.setTextColor(color);
        setTextColor(K_TEAM1, color);

        // and team two
        color = parent.getContext().getColor(R.color.teamTwoColor);
        this.teamTwoTitle.setTextColor(color);
        setTextColor(K_TEAM2, color);

        // get the more / less controls and button
        this.moreLessButton = this.parent.findViewById(R.id.moreLessButton);
        this.moreLessLayout = this.parent.findViewById(R.id.moreLessLayout);
        this.deleteButton = this.parent.findViewById(R.id.deleteButton);
        this.shareButton = this.parent.findViewById(R.id.shareButton);

        // currently more is shown
        this.isMoreShown = true;

        this.totalPoints[0] = this.parent.findViewById(R.id.totalPointsText_teamOne);
        this.totalPoints[1] = this.parent.findViewById(R.id.totalPointsText_teamTwo);
        this.breakPoints[0] = this.parent.findViewById(R.id.breakPointsText_teamOne);
        this.breakPoints[1] = this.parent.findViewById(R.id.breakPointsText_teamTwo);

        // return the main view created
        return this.parent;
    }

    public void setDataFromMatch(Match match, CardHolderMatch source) {
        if (match instanceof TennisMatch) {
            // this is cool, do this
            setMatchData((TennisMatch)match, source);
        }
        else {
            // this isn't cool
            Log.error("The match is not a tennis match in LayoutTennisSummary");
        }
    }

    private void setTextColor(int row, int color) {
        for (int i = 0; i < K_COLS; ++i) {
            this.textViews[row][i].setText("0");
            this.textViews[row][i].setTextColor(color);
        }
    }

    private void setMatchData(TennisMatch match, final CardHolderMatch source) {
        Context context = parent.getContext();
        // set all the data from this match on this view
        TennisScore score = match.getScore();
        Team teamOne = match.getTeamOne();
        Team teamTwo = match.getTeamTwo();

        // who won
        Team matchWinner = match.getMatchWinner();

        // set the titles
        this.teamOneTitle.setText(teamOne.getTeamName());
        this.teamTwoTitle.setText(teamTwo.getTeamName());

        if (matchWinner == teamOne) {
            // make team one's name bold
            BaseActivity.SetTextViewBold(this.teamOneTitle);
        }
        else if (matchWinner == teamTwo) {
            // make team one's name bold
            BaseActivity.SetTextViewBold(this.teamTwoTitle);
        }

        // scroll these names
        this.teamOneTitle.setSelected(true);
        this.teamTwoTitle.setSelected(true);

        // set the points
        Point teamOnePoint = score.getDisplayPoint(teamOne);
        if (teamOnePoint.val() > 0) {
            this.textViews[K_TEAM1][0].setText(teamOnePoint.displayString(context));
        }

        // set the points
        Point teamTwoPoint = score.getDisplayPoint(teamTwo);
        if (teamTwoPoint.val() > 0) {
            this.textViews[K_TEAM2][0].setText(teamTwoPoint.displayString(context));
        }

        if (teamOnePoint.val() > teamTwoPoint.val()) {
            setTextViewBold(this.textViews[K_TEAM1][0]);
        }
        else if (teamTwoPoint.val() > teamOnePoint.val()) {
            setTextViewBold(this.textViews[K_TEAM2][0]);
        }

        if (score.isMatchOver()) {
            // match is over, get rid of the points boxes
            setColumnVisibility(0, View.INVISIBLE);
            // and change the title from points to sets
            this.textViews[K_TITLES][0].setText(R.string.sets);
            this.textViews[K_TITLES][0].setVisibility(View.VISIBLE);
        }

        // and all the previous sets
        int setsPlayed = score.getPlayedSets();
        int colIndex;
        for (int i = 0; i < K_COLS - 1; ++i) {
            // the set index is from 0 to 5, the column index will be 1-6
            colIndex = i + 1;
            int playerOneGames = score.getGames(teamOne, i);
            int playerTwoGames = score.getGames(teamTwo, i);
            if (i  > setsPlayed || (playerOneGames == 0 && playerTwoGames == 0)) {
                // this set wasn't played, need to hide this column (don't delete them all
                // as this makes things massive!)
                setColumnVisibility(colIndex, View.INVISIBLE);
            }
            else {
                // set the text to be the number of games
                this.textViews[K_TEAM1][colIndex].setText(Integer.toString(playerOneGames));
                this.textViews[K_TEAM2][colIndex].setText(Integer.toString(playerTwoGames));
                if (playerOneGames > playerTwoGames) {
                    // player one is winning
                    setTextViewBold(this.textViews[K_TEAM1][colIndex]);
                }
                else if (playerTwoGames > playerOneGames) {
                    // player two is winning
                    setTextViewBold(this.textViews[K_TEAM2][colIndex]);
                }

                if (score.isSetTieBreak(i)) {
                    // this set is / was a tie, show the score of this in brackets
                    int[] tiePoints = score.getPoints(teamOne, i, playerOneGames + playerTwoGames - 1);
                    String tieResult = "(" + tiePoints[0] + "-" + tiePoints[1] + ")";
                    this.textViews[K_TIE][colIndex].setText(tieResult);
                }
                else {
                    this.textViews[K_TIE][colIndex].setVisibility(View.INVISIBLE);
                }
            }
        }


        this.moreLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreLess();
            }
        });
        this.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                source.deleteMatchFile();
            }
        });
        this.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                source.shareMatchFile();
            }
        });

        // set the total points counters
        this.totalPoints[0].setText(Integer.toString(match.getPointsTotal(0, 0)));
        this.totalPoints[1].setText(Integer.toString(match.getPointsTotal(0, 1)));

        // and the break point counters
        this.breakPoints[0].setText(
                score.getBreakPointsConverted(0)
                        + " / "
                        + score.getBreakPoints(0));
        this.breakPoints[1].setText(
                score.getBreakPointsConverted(1)
                        + " / "
                        + score.getBreakPoints(1));

        // hide the more controls
        showMoreLess();
    }

    private void setColumnVisibility(int colIndex, int visibility) {
        for (int i = 0; i < K_ROWS; ++i) {
            TextView view = this.textViews[i][colIndex];
            if (null != view) {
                view.setVisibility(visibility);
            }
        }
    }

    private void showMoreLess() {
        this.isMoreShown = !this.isMoreShown;
        // hide show everything except the button
        for (int i = 0; i < this.moreLessLayout.getChildCount(); ++i) {
            View child = this.moreLessLayout.getChildAt(i);
            if (child == this.moreLessButton) {
                // ignore
            }
            else {
                child.setVisibility(this.isMoreShown ? View.VISIBLE : View.GONE);
            }
        }
        if (this.isMoreShown) {
            // more is shown
            this.moreLessButton.setText(R.string.btn_less);
            this.moreLessButton.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.ic_baseline_keyboard_arrow_left_24px, 0);
        }
        else {
            // less is shown
            this.moreLessButton.setText(R.string.btn_more);
            this.moreLessButton.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.ic_baseline_keyboard_arrow_right_24px, 0);
        }
        // set the icon buttons to be white
        BaseActivity.SetIconTint(this.moreLessButton, Color.WHITE);
    }

}

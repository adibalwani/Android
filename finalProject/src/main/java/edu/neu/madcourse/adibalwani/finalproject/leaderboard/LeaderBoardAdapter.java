package edu.neu.madcourse.adibalwani.finalproject.leaderboard;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import edu.neu.madcourse.adibalwani.finalproject.R;

/**
 * @author rachit on 26-03-2016.
 */
public class LeaderBoardAdapter extends ArrayAdapter<User> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<User> data;

    private static class ViewHolder {
        TextView name;
        TextView score;
        TextView rank;
    }

    public LeaderBoardAdapter(Context context, int layoutResourceId, ArrayList<User> list) {
        super(context, R.layout.two_player_leaderboard_list);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.data = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        if (holder == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView)row.findViewById(R.id.two_player_listview_name);
            holder.score = (TextView)row.findViewById(R.id.two_player_listview_score);
            holder.rank = (TextView)row.findViewById(R.id.two_player_rank);
            row.setTag(holder);
        } else {
            holder = (ViewHolder)row.getTag();
        }
        User user = data.get(position);
        holder.name.setText(user.getName());
        holder.score.setText(String.valueOf(user.getScore()));
        holder.rank.setText(String.valueOf(position+1));
        if (position % 2 == 0) {
            row.setBackgroundColor(getContext().getResources().getColor(R.color.leaderboardbackground));
        }
        return row;
    }
}

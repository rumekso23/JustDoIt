package com.kinocode.justdoit.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kinocode.justdoit.R;
import com.kinocode.justdoit.model.Notes;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter {

    private Context context;
    private ArrayList<Notes> myNote;

    public CustomAdapter(Context context, int textViewResourceId, ArrayList objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        myNote = objects;
    }

    private class ViewHolder{
        TextView note_title;
        TextView note_deadline;
        TextView note_entrydate;
        TextView note_priority;
        TextView note_category;
    }

    public View getView(int position, View convertView, ViewGroup parent){

        ViewHolder holder = null;
        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.note_row, null);

            holder = new ViewHolder();
            holder.note_title = (TextView) convertView.findViewById(R.id.tv_noteTitle);
            holder.note_entrydate = (TextView) convertView.findViewById(R.id.tv_noteEntryDate);
            holder.note_deadline = (TextView) convertView.findViewById(R.id.tv_noteDeadline);
            holder.note_priority = (TextView) convertView.findViewById(R.id.tv_notePriority);
            holder.note_category = (TextView) convertView.findViewById(R.id.tv_noteCategory);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        Notes note_incoming = myNote.get(position);

        holder.note_title.setText("Title: " + note_incoming.getTitle() + "");
        holder.note_entrydate.setText("Updated Date: " + note_incoming.getWriting_date() + "");
        holder.note_priority.setText("Priority: " + note_incoming.getPriority() + "");
        holder.note_deadline.setText("Deadline Date: " + note_incoming.getDeadline_date() + "");
        holder.note_category.setText("Category: " + note_incoming.getCategory() + "");

        return convertView;
    }
}

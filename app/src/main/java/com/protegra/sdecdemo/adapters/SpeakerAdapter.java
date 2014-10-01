package com.protegra.sdecdemo.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.protegra.sdecdemo.R;
import com.protegra.sdecdemo.data.Speaker;

import java.util.List;

public class SpeakerAdapter extends ArrayAdapter<Speaker> {

    private final Context mContext;

    public SpeakerAdapter(Context mContext, int layoutResourceId, List<Speaker> speakers) {
        super(mContext, layoutResourceId, speakers);
        this.mContext = mContext;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        SpeakerViewHolder viewHolder;
        View row = convertView;
        final Speaker currentItem = getItem(position);

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(R.layout.row_speaker, parent, false);

            viewHolder = new SpeakerViewHolder();
            viewHolder.name = (TextView) row.findViewById(R.id.name);

            row.setTag(viewHolder);
        } else {
            viewHolder = (SpeakerViewHolder)row.getTag();
        }

        if (currentItem != null) {
            viewHolder.name.setText(currentItem.name);
        }

        return row;
    }

    static class SpeakerViewHolder {
        TextView name;
    }
}

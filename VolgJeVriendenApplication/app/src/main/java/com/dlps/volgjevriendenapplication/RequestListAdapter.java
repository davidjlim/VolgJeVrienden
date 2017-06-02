package com.dlps.volgjevriendenapplication;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by pim on 1-6-17.
 */

public class RequestListAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<String> list = new ArrayList<String>();
    private Context context;

    public RequestListAdapter(ArrayList<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.request_item, null);
        }

        //Handle TextView and display string from your list
        TextView listItemText = (TextView)view.findViewById(R.id.list_item_string);
        listItemText.setText(list.get(position));

        //Handle buttons and add onClickListeners
        Button acceptBtn = (Button)view.findViewById(R.id.accept_btn);

        acceptBtn.setOnClickListener(new View.OnClickListener(){
            Boolean first = true;
            @Override
            public void onClick(View v) {
                if(!first)
                    return;
                first = false;
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        String url = DataHolder.getInstance().getContext().getString(R.string.ip_address) +
                                DataHolder.getInstance().getContext().getString(R.string.add_friend_url);
                        JSONObject json = new JSONObject();
                        try{
                            json.put("pid",DataHolder.getInstance().getPhonenumber());
                            json.put("password", DataHolder.getInstance().getPassword());
                            json.put("pid2", list.get(position));
                            list.remove(position);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ServerConnector.postRequest(url,json);
                        return null;
                    }
                }.execute();
                notifyDataSetChanged();
            }
        });

        return view;
    }
}
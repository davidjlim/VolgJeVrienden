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
 * For correctly displaying the items in list
 * Created by s1511432 on 02/06/17.
 */

public class FriendListAdapter extends BaseAdapter implements ListAdapter {
    /**
     * List of entries
     */
    private ArrayList<String> list = new ArrayList<String>();
    /**
     * The context of the FriendsActivity
     */
    private Context context;

    /**
     * Constructor
     * @param list the list of friends
     * @param context the context of the friendsactivity
     */
    public FriendListAdapter(ArrayList<String> list, Context context) {
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
            view = inflater.inflate(R.layout.friend_item, null);
        }

        //Handle TextView and display string from your list
        TextView listItemText = (TextView)view.findViewById(R.id.list_item_string);
        listItemText.setText(list.get(position));

        //Handle buttons and add onClickListeners
        Button removeBtn = (Button)view.findViewById(R.id.remove_btn);

        removeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        String url = DataHolder.getInstance().getContext().getString(R.string.ip_address) +
                                DataHolder.getInstance().getContext().getString(R.string.remove_friend_url);
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

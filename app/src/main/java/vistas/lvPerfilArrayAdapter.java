package vistas;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.villasoftgps.ebndsrrep.R;

import java.util.ArrayList;

import controles.AutoResizeTextView;

import static com.villasoftgps.ebndsrrep.R.id.html;

public class lvPerfilArrayAdapter extends BaseAdapter {
    private Context c;
    private ArrayList<lvPerfilItems> data;

    public lvPerfilArrayAdapter (Context c, ArrayList<lvPerfilItems> data){
        this.c = c;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder viewHolder;

        if (row == null){
            LayoutInflater inflater = ((Activity)c).getLayoutInflater();

            row = inflater.inflate(R.layout.lvperfilitems,parent,false);

            viewHolder = new ViewHolder();
            viewHolder.header = (TextView) row.findViewById(R.id.lblHeader);
            viewHolder.title = (TextView) row.findViewById(R.id.lblTitle);
            viewHolder.body = (AutoResizeTextView) row.findViewById(R.id.lblBody);
            viewHolder.footer = (AutoResizeTextView) row.findViewById(R.id.lblFooter);
            row.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder)row.getTag();
        }

        lvPerfilItems item = data.get(position);

        viewHolder.header.setText(item.getHeader());
        viewHolder.header.setVisibility(item.getHeader().equals("") ? View.GONE : View.VISIBLE);

        viewHolder.title.setText(item.getTitle());
        viewHolder.title.setVisibility(item.getTitle().equals("") ? View.GONE : View.VISIBLE);

        viewHolder.body.setText(item.getBody());
        viewHolder.body.setVisibility(item.getBody().equals("") ? View.GONE : View.VISIBLE);

        viewHolder.footer.setText(Html.fromHtml(item.getFooter()), TextView.BufferType.SPANNABLE);
        viewHolder.footer.setVisibility(item.getFooter().equals("") ? View.GONE : View.VISIBLE);

        return row;
    }

    private class ViewHolder{
        TextView header;
        TextView title;
        AutoResizeTextView body;
        AutoResizeTextView footer;
    }
}

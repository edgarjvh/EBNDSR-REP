package vistas;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.villasoftgps.ebndsrrep.R;

import java.util.ArrayList;
import java.util.zip.Inflater;

import controles.AutoResizeTextView;

public class lvMenuArrayAdapter extends BaseAdapter {
    private Context c;
    private ArrayList<lvMenuItems> items;
    private LayoutInflater inflater;

    public lvMenuArrayAdapter(Context c, ArrayList<lvMenuItems> items){
        this.c = c;
        this.items = items;
        this.inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        lvMenuItems item = (lvMenuItems) getItem(position);

        ImageView imgMenuItem;
        AutoResizeTextView lblMenuItem;

        if (convertView == null){
            convertView = inflater.inflate(R.layout.lvmenuitems,null);
        }

        imgMenuItem = (ImageView) convertView.findViewById(R.id.imgMenuItem);
        lblMenuItem = (AutoResizeTextView) convertView.findViewById(R.id.lblMenuItem);

        imgMenuItem.setImageResource(item.getImg());
        lblMenuItem.setText(item.getText());

        return convertView;
    }
}

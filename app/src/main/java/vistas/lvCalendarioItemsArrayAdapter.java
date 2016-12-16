package vistas;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.villasoftgps.ebndsrrep.R;

public class lvCalendarioItemsArrayAdapter extends ArrayAdapter<lvCalendarioItems> {

    private Context context;
    private int layoutResourceId;
    private lvCalendarioItems[] data = null;

    public lvCalendarioItemsArrayAdapter(Context context, int layoutResourceId, lvCalendarioItems[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        WeatherHolder holder;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new WeatherHolder();
            holder.iconoItem = (ImageView)row.findViewById(R.id.iconoItem);
            holder.tituloItem = (TextView)row.findViewById(R.id.tituloItem);
            holder.fechaItem = (TextView)row.findViewById(R.id.fechaItem);
            holder.header = (TextView)row.findViewById(R.id.lblHeader);
            holder.relContent = row.findViewById(R.id.relContent);
            holder.descripcion = (TextView)row.findViewById(R.id.lblDescripcion);
            row.setTag(holder);
        }
        else
        {
            holder = (WeatherHolder)row.getTag();
        }

        lvCalendarioItems weather = data[position];

        holder.tituloItem.setText(weather.titulo);
        holder.fechaItem.setText(weather.fecha);
        holder.descripcion.setText(weather.descripcion);
        holder.iconoItem.setImageResource(weather.icono == 1 ? R.drawable.bulb_on : R.drawable.bulb_off);

        if (!weather.header.equals("")){
            holder.header.setText(weather.header);
            holder.header.setVisibility(View.VISIBLE);
        }else{
            holder.header.setVisibility(View.GONE);
        }

        switch (weather.antiguedad){
            case 0:
                holder.relContent.setBackgroundColor(Color.parseColor("#60F5A9A9"));
                break;
            case 1:
                holder.relContent.setBackgroundColor(Color.parseColor("#6058ACFA"));
                break;
            case 2:
                holder.relContent.setBackgroundColor(0x00000000);
                break;
        }
        return row;
    }

    private static class WeatherHolder
    {
        ImageView iconoItem;
        TextView header;
        TextView tituloItem;
        TextView fechaItem;
        TextView descripcion;
        View relContent;
    }
}

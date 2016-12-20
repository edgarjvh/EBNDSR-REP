package vistas;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.villasoftgps.ebndsrrep.R;
import java.util.ArrayList;
import java.util.Locale;

public class SpinnerItemsArrayAdapter extends BaseAdapter {

    Context c;
    ArrayList<SpinnerItems> data;

    public SpinnerItemsArrayAdapter(Context c, ArrayList<SpinnerItems> data){
        this.c=c;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        SpinnerItems docente = (SpinnerItems)getItem(pos);

        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.spinnerlayout,null);
        }

        ImageView imgPerfil = (ImageView)convertView.findViewById(R.id.imgPerfil);
        TextView lblId = (TextView)convertView.findViewById(R.id.lblIdDocente);
        TextView lblApellidos = (TextView)convertView.findViewById(R.id.lblApellidos);
        TextView lblNombres = (TextView)convertView.findViewById(R.id.lblNombres);

        imgPerfil.setImageResource(R.drawable.teacher_icon);
        lblId.setText(String.format(new Locale("es","ES"),"%1$d",docente.getIdDocente()));
        lblApellidos.setText(docente.getApellidos());
        lblNombres.setText(docente.getNombres());

        if(docente.getRegistrado() == 0){
            lblApellidos.setTextColor(Color.parseColor("#8b0101"));
            lblNombres.setTextColor(Color.parseColor("#8b0101"));
        }else{
            lblApellidos.setTextColor(Color.parseColor("#d6c400"));
            lblNombres.setTextColor(Color.parseColor("#d6c400"));
        }
        return convertView;
    }
}

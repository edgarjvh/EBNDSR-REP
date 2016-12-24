package vistas;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
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
        SpinnerItems alumno = (SpinnerItems)getItem(pos);

        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.spinnerlayout,null);
        }

        ImageView imgPerfil = (ImageView)convertView.findViewById(R.id.imgPerfil);
        TextView lblIdDocente = (TextView)convertView.findViewById(R.id.lblIdDocente);
        TextView lblIdAlumno = (TextView)convertView.findViewById(R.id.lblIdAlumno);
        TextView lblRegistrado = (TextView)convertView.findViewById(R.id.lblRegistrado);
        TextView lblDocente = (TextView)convertView.findViewById(R.id.lblDocente);
        TextView lblAlumno = (TextView)convertView.findViewById(R.id.lblAlumno);

        if (alumno.getImagen().equals("")){
            imgPerfil.setImageResource(R.drawable.teacher_icon);
        }else{
            byte[] decodedBytes = Base64.decode(alumno.getImagen(),0);
            imgPerfil.setImageBitmap(BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length));
        }

        lblIdDocente.setText(String.format(new Locale("es","ES"),"%1$d",alumno.getIdDocente()));
        lblIdAlumno.setText(String.format(new Locale("es","ES"),"%1$d",alumno.getIdAlumno()));
        lblRegistrado.setText(String.format(new Locale("es","ES"),"%1$d",alumno.getRegistrado()));
        lblDocente.setText(alumno.getApellidosDoc() + ", " + alumno.getNombresDoc());
        lblAlumno.setText(alumno.getApellidosAl() + ", " + alumno.getNombresAl());

        if(alumno.getRegistrado() == 0){
            lblDocente.setTextColor(Color.parseColor("#8b0101"));
        }else{
            lblDocente.setTextColor(Color.parseColor("#d6c400"));
        }
        return convertView;
    }
}

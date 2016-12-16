package vistas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.villasoftgps.ebndsrrep.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class lvMensajesItemsArrayAdapter extends BaseAdapter {

    private ArrayList<lvMensajesItems> data;
    private Context c;
    private static final int ROW_DOC = 0;
    private static final int ROW_REP = 1;
    private static final int STATUS_SENT = 0;
    private static final int STATUS_RECEIVED = 1;
    private static final int STATUS_READ = 2;
    private String lastHeader = "";
    private String tempHeader = "";

    public lvMensajesItemsArrayAdapter(Context c, ArrayList<lvMensajesItems> data){
        this.c = c;
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
        return i;
    }

    @Override
    public int getItemViewType(int position) {
        lvMensajesItems item = (lvMensajesItems)getItem(position);
        return item.getVia() == ROW_DOC ? ROW_DOC : ROW_REP;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        int type = getItemViewType(pos);
        lvMensajesItems msj = (lvMensajesItems)getItem(pos);

        TextView lblHeader;
        TextView lblFechaHora;
        TextView lblMensaje;
        ImageView imgStatus;

        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            switch (type){
                case ROW_DOC:
                    convertView = inflater.inflate(R.layout.lvmensajefromdocitem,null);
                    break;

                case ROW_REP:
                    convertView = inflater.inflate(R.layout.lvmensajefromrepitem,null);
                    break;

                default:
                    break;
            }
        }

        Calendar hoy = Calendar.getInstance(); // hoy
        hoy.add(Calendar.HOUR_OF_DAY, -4);

        Calendar ayer = Calendar.getInstance(); // hoy
        ayer.add(Calendar.HOUR_OF_DAY, -4);
        ayer.add(Calendar.DAY_OF_YEAR, -1);

        Calendar fecha = Calendar.getInstance(); // fecha
        fecha.setTime(new Date(msj.getFechaHora()));
        fecha.add(Calendar.HOUR_OF_DAY, -4);

        SimpleDateFormat df1 = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat df2 = new SimpleDateFormat("hh:mm:ss aaa");

        int visibility = View.GONE;

        if(df1.format(hoy.getTime()).equals(df1.format(fecha.getTime()))){
            tempHeader = "Hoy";
        }else if (df1.format(ayer.getTime()).equals(df1.format(fecha.getTime()))){
            tempHeader = "Ayer";
        }else{
            df1 = new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
            tempHeader = df1.format(fecha.getTime());
        }

        if (lastHeader.equals("")){
            lastHeader = tempHeader;
            visibility = View.VISIBLE;
        }else{
            if (lastHeader.equals(tempHeader)){
                visibility = View.GONE;
            }else{
                visibility = View.VISIBLE;
                lastHeader = tempHeader;
            }
        }

        switch (type){
            case ROW_DOC:
                lblHeader = (TextView) convertView.findViewById(R.id.lblHeader);
                lblFechaHora = (TextView)convertView.findViewById(R.id.lblFechaHora);
                lblMensaje = (TextView)convertView.findViewById(R.id.lblMensaje);

                lblHeader.setText(lastHeader);
                lblHeader.setVisibility(visibility);
                lblFechaHora.setText(df2.format(fecha.getTime()));
                lblMensaje.setText(msj.getMensaje());

                break;

            case ROW_REP:
                lblHeader = (TextView) convertView.findViewById(R.id.lblHeader);
                lblFechaHora = (TextView)convertView.findViewById(R.id.lblFechaHora);
                lblMensaje = (TextView)convertView.findViewById(R.id.lblMensaje);
                imgStatus = (ImageView)convertView.findViewById(R.id.imgStatus);

                lblHeader.setText(lastHeader);
                lblHeader.setVisibility(visibility);
                lblFechaHora.setText(df2.format(fecha.getTime()));
                lblMensaje.setText(msj.getMensaje());

                switch (msj.getStatus()){
                    case STATUS_SENT:
                        imgStatus.setImageResource(R.drawable.sent_status_icon);
                        break;
                    case STATUS_RECEIVED:
                        imgStatus.setImageResource(R.drawable.received_status_icon);
                        break;
                    case STATUS_READ:
                        imgStatus.setImageResource(R.drawable.read_status_icon);
                        break;
                    default:
                        break;
                }

                break;

            default:
                break;
        }

        return convertView;
    }


}

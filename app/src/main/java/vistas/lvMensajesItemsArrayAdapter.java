package vistas;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
        View row = convertView;
        MensajeHolder mensajeHolder;
        int type = getItemViewType(pos);

        if (row == null){
            LayoutInflater inflater = ((Activity)c).getLayoutInflater();

            if (type == ROW_DOC){
                row = inflater.inflate(R.layout.lvmensajefromdocitem,parent,false);
            }else{
                row = inflater.inflate(R.layout.lvmensajefromrepitem,parent,false);
            }

            mensajeHolder = new MensajeHolder();
            mensajeHolder.lblHeader = (TextView)row.findViewById(R.id.lblHeader);
            mensajeHolder.lblFechaHora = (TextView)row.findViewById(R.id.lblFechaHora);
            mensajeHolder.lblMensaje = (TextView)row.findViewById(R.id.lblMensaje);
            mensajeHolder.imgStatus = (ImageView)row.findViewById(R.id.imgStatus);
            mensajeHolder.pbarStatus = (ProgressBar)row.findViewById(R.id.pbarStatus);
            row.setTag(mensajeHolder);

        }else{
            mensajeHolder = (MensajeHolder)row.getTag();
        }

        lvMensajesItems mensaje = data.get(pos);

        if (pos == 0){
            Calendar fecha = Calendar.getInstance(); // fecha
            fecha.setTime(new Date(mensaje.getFechaHora()));
            tempHeader = getHeader(fecha);

            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss aaa",new Locale("es", "ES"));

            mensajeHolder.lblHeader.setText(tempHeader);
            mensajeHolder.lblHeader.setVisibility(View.VISIBLE);
            mensajeHolder.lblFechaHora.setText(df.format(fecha.getTime()));
            mensajeHolder.lblMensaje.setText(mensaje.getMensaje());

            if (type == ROW_REP){
                switch (mensaje.getStatus()){
                    case STATUS_SENT:
                        mensajeHolder.imgStatus.setImageResource(R.drawable.sent_status_icon);
                        mensajeHolder.imgStatus.setVisibility(View.VISIBLE);
                        mensajeHolder.pbarStatus.setVisibility(View.GONE);
                        break;
                    case STATUS_RECEIVED:
                        mensajeHolder.imgStatus.setImageResource(R.drawable.received_status_icon);
                        mensajeHolder.imgStatus.setVisibility(View.VISIBLE);
                        mensajeHolder.pbarStatus.setVisibility(View.GONE);
                        break;
                    case STATUS_READ:
                        mensajeHolder.imgStatus.setImageResource(R.drawable.read_status_icon);
                        mensajeHolder.imgStatus.setVisibility(View.VISIBLE);
                        mensajeHolder.pbarStatus.setVisibility(View.GONE);
                        break;
                    default:
                        mensajeHolder.imgStatus.setVisibility(View.GONE);
                        mensajeHolder.pbarStatus.setVisibility(View.VISIBLE);
                        break;
                }
            }else{
                mensajeHolder.imgStatus.setVisibility(View.GONE);
                mensajeHolder.pbarStatus.setVisibility(View.GONE);
            }

        }else{
            lvMensajesItems prevMsg = data.get(pos - 1);
            Calendar prevFecha = Calendar.getInstance(); // fecha
            prevFecha.setTime(new Date(prevMsg.getFechaHora()));
            String prevHeader = getHeader(prevFecha);

            Calendar curFecha = Calendar.getInstance(); // fecha
            curFecha.setTime(new Date(mensaje.getFechaHora()));
            String curHeader = getHeader(curFecha);

            SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss aaa",new Locale("es", "ES"));

            mensajeHolder.lblHeader.setText(curHeader);
            mensajeHolder.lblHeader.setVisibility(prevHeader.equals(curHeader) ? View.GONE : View.VISIBLE);
            mensajeHolder.lblFechaHora.setText(df.format(curFecha.getTime()));
            mensajeHolder.lblMensaje.setText(mensaje.getMensaje());

            if (type == ROW_REP){
                switch (mensaje.getStatus()){
                    case STATUS_SENT:
                        mensajeHolder.imgStatus.setImageResource(R.drawable.sent_status_icon);
                        mensajeHolder.imgStatus.setVisibility(View.VISIBLE);
                        mensajeHolder.pbarStatus.setVisibility(View.GONE);
                        break;
                    case STATUS_RECEIVED:
                        mensajeHolder.imgStatus.setImageResource(R.drawable.received_status_icon);
                        mensajeHolder.imgStatus.setVisibility(View.VISIBLE);
                        mensajeHolder.pbarStatus.setVisibility(View.GONE);
                        break;
                    case STATUS_READ:
                        mensajeHolder.imgStatus.setImageResource(R.drawable.read_status_icon);
                        mensajeHolder.imgStatus.setVisibility(View.VISIBLE);
                        mensajeHolder.pbarStatus.setVisibility(View.GONE);
                        break;
                    default:
                        mensajeHolder.imgStatus.setVisibility(View.GONE);
                        mensajeHolder.pbarStatus.setVisibility(View.VISIBLE);
                        break;
                }
            }else{
                mensajeHolder.imgStatus.setVisibility(View.GONE);
                mensajeHolder.pbarStatus.setVisibility(View.GONE);
            }
        }

        return row;
    }

    private static class MensajeHolder{
        TextView lblHeader;
        TextView lblFechaHora;
        TextView lblMensaje;
        ImageView imgStatus;
        ProgressBar pbarStatus;
    }

    private String getHeader(Calendar fecha){
        Calendar hoy = Calendar.getInstance(); // hoy
        Calendar ayer = Calendar.getInstance(); // hoy
        ayer.add(Calendar.DAY_OF_YEAR, -1);

        SimpleDateFormat df1 = new SimpleDateFormat("dd-MM-yyyy",new Locale("es", "ES"));
        SimpleDateFormat df2 = new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

        if(df1.format(hoy.getTime()).equals(df1.format(fecha.getTime()))){
            tempHeader = "Hoy";
        }else if (df1.format(ayer.getTime()).equals(df1.format(fecha.getTime()))){
            tempHeader = "Ayer";
        }else{
            tempHeader = df2.format(fecha.getTime());
        }

        return tempHeader;
    }
}

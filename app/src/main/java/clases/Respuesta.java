package clases;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import java.util.ArrayList;

public class Respuesta {
    public Object getData(ArrayList<Object> parametros){
        Object data;
        String namespace = "http://schooltool.org/";
        //String direccion = "http://154.42.65.212:9600/schooltool.asmx";
        String direccion = "http://cctv.zuprevencion.org:9800/schooltool.asmx";
        String metodo = parametros.get(parametros.size() - 1).toString();
        String soapAction = namespace + metodo;

        SoapObject request = new SoapObject(namespace, metodo);
        String property[];
        PropertyInfo pi;

        for (int i = 0; i < parametros.size() - 1; i++){
            property = parametros.get(i).toString().split("\\*");
            pi = new PropertyInfo();
            pi.setName(property[0]);
            pi.setValue(property[1]);
            pi.setType(property[1].getClass());
            request.addProperty(pi);
        }

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(direccion);

        try {
            httpTransport.call(soapAction, envelope);
            data = envelope.getResponse();
        } catch (Exception exception) {
            data = exception.toString();
        }

        return data;
    }
}
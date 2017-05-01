package net.gwerder.java.messagevortex.transport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by martin.gwerder on 03.04.2017.
 */
public class DummyTransport implements Transport {

    public static final Map<String,TransportListener> endpoints=new ConcurrentHashMap<>(  );

    public DummyTransport(String id,TransportListener blender) throws IOException{
        synchronized(endpoints) {
            if(endpoints.containsKey(id)) {
                throw new IOException("Duplicate transport endpoint identifier ("+id+")");
            }
            endpoints.put(id,blender);
        }
    }

    public DummyTransport(TransportListener blender) {
        synchronized(endpoints) {
            String id=null;
            while(id==null || endpoints.containsKey(id)) {
                id=RandomString.nextString( 5,"0123456789abcdef@example.com" );
            }
            endpoints.put(id,blender);
        }
    }

    public boolean sendMessage(final String address, InputStream is) throws IOException {
        if(endpoints.get(address)==null) {
            return false;
        }
        // convert is to byte array
        ByteArrayBuilder bab=new ByteArrayBuilder();
        int n;
        byte[] buffer = new byte[1024];
        while((n = is.read(buffer)) > -1) {
            bab.append(buffer, n);
        }

        // send byte array as input stream to target
        final InputStream iso=new ByteArrayInputStream(bab.toBytes());
        new Thread() {
            public void run() {
                endpoints.get(address).gotMessage( iso );
            }
        }.start();
        return true;
    }

}

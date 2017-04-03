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

    public static Map<String,TransportListener> endpoints=new ConcurrentHashMap<>(  );

    public DummyTransport(String id,TransportListener blender) {
        endpoints.put(id,blender);
    }

    public DummyTransport(TransportListener blender) {
        this("DummyID"+RandomString.nextString( 5,"0123456789abcdef@nowhere.com" ),blender);
    }

    public boolean sendMessage(String address, InputStream is) throws IOException {
        ByteArrayBuilder bab=new ByteArrayBuilder();
        int n;
        byte[] buffer = new byte[1024];
        while((n = is.read(buffer)) > -1) {
            bab.append(buffer, n);   // Don't allow any extra bytes to creep in, final write
        }
        InputStream iso=new ByteArrayInputStream(bab.toBytes());
        endpoints.get(address).gotMessage( iso );
        return true;
    }

}

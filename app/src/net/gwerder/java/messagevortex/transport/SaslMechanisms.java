package net.gwerder.java.messagevortex.transport;

/**
 * Created by martin.gwerder on 17.04.2018.
 */
public enum SaslMechanisms {

    PLAIN( "PLAIN" ),
    DIGEST_MD5( "DIGEST-MD5" ),
    CRAM_MD5( "CRAM-MD5" );

    String value;

    SaslMechanisms( String value ) {
        this.value = value;
    }

    @Override
    public String toString()  {
        return this.value;
    }
}

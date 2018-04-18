package net.gwerder.java.messagevortex.asn1;
// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.io.Serializable;

import static net.gwerder.java.messagevortex.asn1.BlendingParameter.BlendingParameterChoice.*;

/**
 * Blending Parameter Block representation.
 *
 * Created by martin.gwerder on 15.05.2017.
 */
public class BlendingParameter extends AbstractBlock implements Serializable {

    public static final long serialVersionUID = 100000000004L;

    public enum BlendingParameterChoice {
        OFFSET( 1 ),
        F5( 2 );

        final int id;

        BlendingParameterChoice( int i ) {
            id = i;
        }

        public static BlendingParameterChoice getById( int i ) {
            for( BlendingParameterChoice e : values() ) {
                if( e.id == i ) {
                    return e;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }

    int offset = -1;
    SymmetricKey symmetricKey = null;

    public BlendingParameter(ASN1Encodable e ) throws IOException {
        parse( e );
    }

    public BlendingParameter( BlendingParameterChoice choice ) throws IOException {
        if( choice==OFFSET ) {
            offset=0;
        } else {
            symmetricKey=new SymmetricKey();
        }
    }

    @Override
    protected void parse( ASN1Encodable to ) throws IOException {
        ASN1TaggedObject t = ASN1TaggedObject.getInstance( to );
        if( to == null || t == null ) {
            throw new IOException("unknown blending parameter choice detected (tagged object is null)");
        }
        BlendingParameterChoice bpc = BlendingParameterChoice.getById( t.getTagNo() );
        if( bpc == null ) {
            throw new IOException("unknown blending parameter choice detected ("+t.getTagNo()+")");
        }
        switch( bpc ) {
            case OFFSET:
                offset = ASN1Integer.getInstance( t.getObject() ).getValue().intValue();
                break;
            case F5:
                symmetricKey = new SymmetricKey( t.getObject().getEncoded() );
                break;
            default:
                throw new IOException( "unknown blending parameter choice detected (" + t.getTagNo() + ")" );
        }
    }

    public BlendingParameterChoice getChoice() {
        if(offset>-1) {
            return OFFSET;
        } else if( symmetricKey != null ) {
            return F5;
        }
        return null;
    }

    @Override
    public String dumpValueNotation(String prefix, DumpType dumptype) throws IOException {
        StringBuilder sb=new StringBuilder();
        switch( getChoice() ) {
            case OFFSET:
                sb.append( "offset " ).append( offset );
                break;
            case F5:
                sb.append( "symmetricKey " ).append( symmetricKey.dumpValueNotation( prefix, dumptype) );
                break;
            default:
                throw new IOException( "unable to dump " + getChoice() );
        }
        return sb.toString();
    }

    @Override
    public ASN1Object toASN1Object( DumpType dumpType ) throws IOException {
        switch ( getChoice() ) {
            case OFFSET:
                return new DERTaggedObject( getChoice().getId(), new ASN1Integer(offset) );
            case F5:
                return new DERTaggedObject( getChoice().getId(), symmetricKey.toASN1Object( dumpType ) );
            default:
                throw new IOException( "unable to convert to ASN.1 (" + getChoice() + ")" );
        }
    }

    @Override
    public boolean equals( Object t ) {
        if( t==null || t.getClass() != this.getClass() ) {
            return false;
        }
        BlendingParameter o = (BlendingParameter) t;
        try {
            return dumpValueNotation( "", DumpType.ALL ).equals( o.dumpValueNotation( "", DumpType.ALL ) );
        } catch( IOException ioe ) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        // this methode is required for code sanity
        try{
            return dumpValueNotation( "", DumpType.ALL ).hashCode();
        } catch( IOException ioe ) {
            return "FAILED".hashCode();
        }
    }

}

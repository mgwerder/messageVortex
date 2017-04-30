package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.routing.operation.GaloisFieldMathMode;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Represents a the Blending specification of the routing block.
 *
 */
public class AbstractRedundancyOperation extends Operation {

    public static final int INPUT_ID     = 16000;
    public static final int DATA_STRIPES = 16001;
    public static final int REDUNDANCY   = 16002;
    public static final int KEYS         = 16003;
    public static final int OUTPUT_ID    = 16004;
    public static final int GF_SIZE      = 16005;

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    int inputId;
    int dataStripes=1;
    int redundancy=1;
    ArrayList<SymmetricKey> keys;
    int outputId;
    int gfSize=4;

    /* constructor */
    public AbstractRedundancyOperation(int inputId, int dataStripes, int redundancy, List<SymmetricKey> keys, int newFirstId, int gfSize) {
        this.inputId = inputId;
        this.gfSize=gfSize;
        setDataStripes(dataStripes);
        setRedundancy(redundancy);
        setKeys(keys);
        this.outputId =newFirstId;
    }

    public AbstractRedundancyOperation(ASN1Encodable to) throws IOException {
        parse(to);
    }

    protected void parse(ASN1Encodable p) throws IOException {
        LOGGER.log( Level.FINER, "Executing parse()" );

        int i=0;
        ASN1Sequence s1 = ASN1Sequence.getInstance( p );

        inputId=parseIntval(ASN1TaggedObject.getInstance(s1.getObjectAt(i++)),INPUT_ID,"inputId");
        dataStripes=parseIntval(ASN1TaggedObject.getInstance(s1.getObjectAt(i++)),DATA_STRIPES,"dataStripes");
        redundancy=parseIntval(ASN1TaggedObject.getInstance(s1.getObjectAt(i++)),REDUNDANCY,"redundancy");

        // reading keys
        ASN1TaggedObject to=ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
        if(to.getTagNo()!=KEYS) {
            throw new IOException("got unknown tag id ("+to.getTagNo()+") when expecting keys" );
        }
        ASN1Sequence s=ASN1Sequence.getInstance(to.getObject());
        keys=new ArrayList<>();
        for(ASN1Encodable o:s) {
            keys.add(new SymmetricKey(o.toASN1Primitive().getEncoded()));
        }

        outputId=parseIntval(ASN1TaggedObject.getInstance(s1.getObjectAt(i++)),OUTPUT_ID,"outputId");
        gfSize=parseIntval(ASN1TaggedObject.getInstance(s1.getObjectAt(i++)),GF_SIZE,"gfSize");

        LOGGER.log( Level.FINER, "Finished parse()" );

    }

    private int parseIntval(ASN1TaggedObject obj,int id,String description) throws IOException {
        if(obj.getTagNo()!=id) {
            throw new IOException("got unknown tag id ("+id+") when expecting "+description );
        }
        return ASN1Integer.getInstance(obj.getObject()).getValue().intValue();
    }

    public ASN1Object toASN1Object() throws IOException{
        // Prepare encoding
        LOGGER.log( Level.FINER,"Executing toASN1Object()");

        ASN1EncodableVector v=new ASN1EncodableVector();

        LOGGER.log(Level.FINER,"  adding inputId");
        v.add(new DERTaggedObject( INPUT_ID,new ASN1Integer(inputId)));

        LOGGER.log(Level.FINER,"  adding dataStripes");
        v.add(new DERTaggedObject( DATA_STRIPES,new ASN1Integer(dataStripes)));

        LOGGER.log(Level.FINER,"  adding redundancy");
        v.add(new DERTaggedObject( REDUNDANCY,new ASN1Integer(redundancy)));

        ASN1EncodableVector v2=new ASN1EncodableVector();
        for(SymmetricKey k:keys) {
            v2.add(k.toASN1Object());
        }
        v.add(new DERTaggedObject( KEYS,new DERSequence(v2)));

        LOGGER.log(Level.FINER,"  adding outputId");
        v.add(new DERTaggedObject( OUTPUT_ID,new ASN1Integer(outputId)));

        LOGGER.log(Level.FINER,"  adding gfSize");
        v.add(new DERTaggedObject( GF_SIZE,new ASN1Integer(gfSize)));

        ASN1Sequence seq=new DERSequence(v);
        LOGGER.log(Level.FINER,"done toASN1Object()");
        return seq;
    }

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append(prefix).append("-- FIXME dumping of BlendingSpec object not yet supported").append(CRLF); //FIXME
        return sb.toString();
    }

    /***
     * Sets the id of the first input id of the payload.
     *
     * @param id the new first input id
     * @return the previously set first input id
     */
    public int setInputId(int id) {
        int old=this.inputId;
        this.inputId =id;
        return old;
    }

    public int getInputId() {
        return this.inputId;
    }

    /***
     * Sets the number of data stripes for this operation.
     *
     * @param stripes The number of data stripes to be used for the redundancy operation
     * @return the previously set number of stripes
     * @throws ArithmeticException if all stripes together are not accomodatable in the given GF field
     */
    public int setDataStripes(int stripes)  {
        if(stripes<1 || stripes+this.redundancy> GaloisFieldMathMode.lshift(gfSize,1,(byte)33)) {
            throw new ArithmeticException("too many stripes to be acomodated in given GF field");
        }
        int old=this.dataStripes;
        this.dataStripes=stripes;
        return old;
    }

    public int getDataStripes() {
        return this.dataStripes;
    }

    /***
     * sets the number of redundancy stripes.
     *
     * @param stripes the number of redundancy stripes to be set
     * @return the previous number of redundancy stripes
     * @throws ArithmeticException if the defined GF size is unable to accomodate all values
     */
    public int setRedundancy(int stripes)  {
        if(stripes<1 || stripes+this.dataStripes> GaloisFieldMathMode.lshift(gfSize,1,(byte)33)) {
            throw new ArithmeticException("too many stripes to be acomodated in given GF field");
        }
        int old=this.redundancy;
        this.redundancy=stripes;
        return old;
    }

    public int getRedundancy() {
        return this.redundancy;
    }

    /***
     * sets the keys to be used to encrypt all input respective output fields.
     *
     * @param keys a list of teys
     * @return the old list of keys
     * @throws ArithmeticException if the number of keys doees not match the number of stripes
     */
    public SymmetricKey[] setKeys(List<SymmetricKey> keys) {
        if(this.dataStripes+this.dataStripes!=keys.size()) {
            throw new ArithmeticException("illegal number of keys");
        }
        SymmetricKey[] old=new SymmetricKey[0];
        if(this.keys!=null) {
            old = this.keys.toArray(new SymmetricKey[this.keys.size()]);
        }
        this.keys=new ArrayList<>();
        this.keys.addAll(keys);
        return old;
    }

    /***
     * Gets the omega parameter of the Galoise field.
     *
     * @return the omega parameter of the GF.
     */
    public SymmetricKey[] getkeys() {
        return this.keys.toArray(new SymmetricKey[this.keys.size()]);
    }

    /***
     * Sets the omega parameter of the Galoise field.
     *
     * @param omega the omega of the new GF
     * @return the previous omega parameter of the GF.
     * @throws ArithmeticException if the number of all stripes in total (data and redundancy) exceeds the address space of the GF
     */
    public int setGFSize(int omega) {
        if(omega<2 || omega>16 || this.redundancy+this.dataStripes> GaloisFieldMathMode.lshift(omega,1,(byte)33)) {
            throw new ArithmeticException("too many stripes to be acomodated in given GF field");
        }
        int old=this.gfSize;
        this.gfSize=omega;
        return old;
    }

    public int getGFSize() {
        return this.gfSize;
    }

    /***
     * Sets the id of the first output block of the function.
     *
     * @param id the first id to ber ised
     * @return   old first value (before the write
     */
    public int setOutputId(int id) {
        int old=this.outputId;
        this.outputId =id;
        return old;
    }

    /***
     * gets the id of the first output payload block.
     *
     * @return id of the respective block
     */
    public int getOutputId() {
        return this.outputId;
    }

}

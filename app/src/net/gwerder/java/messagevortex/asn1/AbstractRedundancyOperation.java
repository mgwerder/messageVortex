package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.routing.operation.GaloisFieldMathMode;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a the Blending specification of the routing block.
 *
 */
public class AbstractRedundancyOperation extends Operation {

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

    public AbstractRedundancyOperation(ASN1Encodable to) {
        parse(to);
    }

    protected void parse(ASN1Encodable to) {
        throw new NotImplementedException();
    }

    public ASN1Object toASN1Object() throws IOException{
        throw new UnsupportedOperationException( "not yet implemented" ); //FIXME
    }

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append(prefix).append("-- FIXME dumping of BlendingSpec object not yet supported").append(CRLF); //FIXME
        return sb.toString();
    }

    public int setInputId(int id) {
        int old=this.inputId;
        this.inputId =id;
        return old;
    }

    public int getInputId() {
        return this.inputId;
    }

    public int setDataStripes(int stripes) throws ArithmeticException {
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

    public int setRedundancy(int stripes) throws ArithmeticException {
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

    public SymmetricKey[] setKeys(List<SymmetricKey> keys) throws ArithmeticException {
        if(this.dataStripes+this.dataStripes!=keys.size()) {
            throw new ArithmeticException("too many stripes to be acomodated in given GF field");
        }
        SymmetricKey[] old=new SymmetricKey[0];
        if(this.keys!=null) {
            old = this.keys.toArray(new SymmetricKey[this.keys.size()]);
        }
        this.keys=new ArrayList<>();
        this.keys.addAll(keys);
        return old;
    }

    public SymmetricKey[] getkeys() {
        return this.keys.toArray(new SymmetricKey[this.keys.size()]);
    }

    public int setGFSize(int size) throws ArithmeticException {
        if(gfSize<2 || gfSize>16 || this.redundancy+this.dataStripes> GaloisFieldMathMode.lshift(size,1,(byte)33)) {
            throw new ArithmeticException("too many stripes to be acomodated in given GF field");
        }
        int old=this.gfSize;
        this.gfSize=size;
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

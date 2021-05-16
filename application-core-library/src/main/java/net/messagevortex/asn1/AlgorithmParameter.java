package net.messagevortex.asn1;

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

import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.asn1.encryption.Parameter;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * <p>ASN1 parser block for algorithm parameters.</p>
 */
public class AlgorithmParameter extends AbstractBlock
       implements Serializable, Comparable<AlgorithmParameter>, Dumpable {

  public static final long serialVersionUID = 100000000001L;

  private final Map<Integer, String> parameter;

  public AlgorithmParameter() {
    parameter = new ConcurrentSkipListMap<>();
  }

  /***
   * <p>Create object from ASN.1 code.</p>
   *
   * @param ae the ASN.1 code
   * @throws IOException if parsing of ASN.1 code fails
   */
  public AlgorithmParameter(ASN1Encodable ae) throws IOException {
    this();
    if (ae != null) {
      parse(ae);
    }
  }

  /***
   * <p>Copy constructor.</p>
   *
   * @param p the ASN.1 code
   */
  public AlgorithmParameter(AlgorithmParameter p) {
    this();
    for (Map.Entry<Integer, String> e : p.parameter.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  public final String put(String id, String value) {
    return put(Parameter.getByString(id).getId(), value);
  }

  /***
   * <p>Puts a key/value pair into the list.</p>
   *
   * @param id the key (must be known to the
   *           subsystem @see Parameter)
   * @param value the value to be stored
   * @return the perviously set value if it had been set before
   */
  public final String put(int id, String value) {
    if (value == null) {
      String ret = get(id);
      parameter.remove(id);
      return ret;
    } else {
      return parameter.put(id, value);
    }
  }

  /***
   * <p>Puts a key/value pair into the list.</p>
   *
   * @param parameter the key
   * @param value the value to be stored
   * @return the perviously set value if it had been set before
   */
  public final String put(Parameter parameter, String value) {

    // this assertion catches rewritten keysizes (different values)
    assert parameter != Parameter.KEYSIZE || (get(parameter.getId()) == null
            || (get(parameter.getId()).equals(value)));

    return put(parameter.getId(), value);
  }

  /***
   * <p>Gets a value identified by a key from the list.</p>
   *
   * @param id the key
   * @return the value or null if not found
   */
  public final String get(String id) {
    Parameter p = Parameter.getByString(id);
    if (p == null) {
      // This should not happe if the parameter is working correctly
      throw new IllegalArgumentException("got unknown parameter id to map (" + id + ")");
    }
    return get(p.getId());
  }

  public String get(Parameter p) {
    return get(p.getId());
  }

  public String get(int id) {
    return parameter.get(id);
  }

  protected final void parse(ASN1Encodable ae) throws IOException {
    ASN1Sequence s1 = ASN1Sequence.getInstance(ae);
    for (ASN1Encodable o : s1) {
      ASN1TaggedObject to = ASN1TaggedObject.getInstance(o);
      Parameter p = Parameter.getById(to.getTagNo());
      if (p.isEncodable()) {
        parameter.put(to.getTagNo(), p.fromAsn1Object(to.getObject()));
      } else {
        throw new IOException("unknown der tagged object when parsing parameter ("
                + to.getTagNo() + ")");
      }
    }
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dumpType) {
    StringBuilder sb = new StringBuilder();
    sb.append('{').append(CRLF);
    int i = 0;
    for (Map.Entry<Integer, String> e : parameter.entrySet()) {
      Parameter p = Parameter.getById(e.getKey());
      if (p != null && p.isEncodable()) {
        if (i > 0) {
          sb.append(',').append(CRLF);
        }
        sb.append(prefix).append("  ").append(p).append(" \"").append(e.getValue()).append('\"');
        i++;
      }
    }
    sb.append(prefix).append(CRLF).append(prefix).append('}');
    return sb.toString();
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dt) {
    ASN1EncodableVector v = new ASN1EncodableVector();
    for (Map.Entry<Integer, String> e : parameter.entrySet()) {
      Parameter p = Parameter.getById(e.getKey());
      if (p != null && p.isEncodable() || dt == DumpType.INTERNAL) {
        v.add(new DERTaggedObject(p.getId(), p.toAsn1Object(e.getValue())));
      }
    }
    return new DERSequence(v);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for (Map.Entry<Integer, String> e : this.parameter.entrySet()) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(Parameter.getById(e.getKey())).append("=\"").append(e.getValue()).append('"');
      i++;
    }
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o.getClass() == this.getClass())) {
      return false;
    }
    return ((AlgorithmParameter) (o)).compareTo(this) == 0;
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public int compareTo(AlgorithmParameter o) {
    return toString().compareTo(o.toString());
  }
}

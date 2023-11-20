package net.messagevortex.visualization;

import net.messagevortex.asn1.*;
import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

public class RoutingBlockGenerator {
    /**
     * <p>Creates a new Routingcombo.</p>
     *
     * @param minTime       A long with a timestamp.
     * @param maxTime       A long with a timestamp.
     * @param rec           An ASN1Sequence containing the Blendingspec.
     * @param payloadOps    An ASN1TaggedObject containing all operations to be applied in workpsace.
     * @param prefix1       AN ASN1TaggedObject containing a sequence with prefixes.
     * @param routingTagged An ASN1TaggedObject containing a sequence with all future routing.
     * @param forwardSecret A long containing the forwardsecret.
     * @param replyTagged   An ASN1TaggedObject containing the reply blocks.
     * @return Returns a new ASN1Sequence with the Routingcombo.
     */
    public ASN1Sequence createRoutingCombo(long minTime, long maxTime, ASN1Sequence rec, ASN1TaggedObject payloadOps, ASN1TaggedObject prefix1, ASN1TaggedObject routingTagged, long forwardSecret, ASN1TaggedObject replyTagged) {
        return new DERSequence(new ASN1Encodable[]{rec, new ASN1Integer(minTime), new ASN1Integer(maxTime), prefix1, routingTagged, new ASN1Integer(forwardSecret), replyTagged, payloadOps});
    }

    /**
     * <p>Creates a new Blending Parameter.</p>
     *
     * @param offset    An Integer with the offset.
     * @return Returns a new ASN1TaggedObject with a Blending Parameter.
     */
    public ASN1TaggedObject createBlendingParameter(int offset) {
        return new DERTaggedObject(true, 1, new ASN1Integer(offset));
    }

    /**
     * <p>Creates a new blending specification.</p>
     *
     * @param media             A String with the media.
     * @param recipient         A String with the Recipient address.
     * @param blendingType      A String with the blending type.
     * @param blendingParameter A Treemap of ASN1TaggedObjects containing Blending Parameters.
     * @return A new Blending spec with as an ASN1Sequence.
     */
    public ASN1Sequence createBlendingSpec(String media, String recipient, String blendingType, TreeMap<Integer, ASN1TaggedObject> blendingParameter) throws IOException {
        ASN1EncodableVector blendingParams = new ASN1EncodableVector();

        blendingParameter.forEach((key, value) -> {
            blendingParams.add(value);
        });

        return new DERSequence(
                new ASN1Encodable[]{
                        new DERSequence(new ASN1Encodable[]{new DERUTF8String(media), new DERUTF8String(recipient)}),
                        new DERUTF8String(blendingType),
                        new DERSequence(blendingParams)
                }
        );
    }

    /**
     * <p>Creates a new Prefix</p>
     *
     * @param tag   An Integer with the tag.
     * @param data  An Array of ByteArrays containing the data.
     * @return Returns a new ASN1TaggedObject containing the prefix.
     */
    public ASN1TaggedObject createPrefix(int tag, byte[][] data) throws IOException {
        ASN1EncodableVector d = new ASN1EncodableVector();
        for(byte[] b : data) {
            d.add(ASN1Primitive.fromByteArray(b));
        }

        return new DERTaggedObject(true, tag, new DERSequence(d));
    }

    /**
     * <p>Creates a new Reply</p>
     *
     * @return An ASN1TaggedObject with a new reply.
     */
    public ASN1TaggedObject createReply() {
        return null;
    }
}

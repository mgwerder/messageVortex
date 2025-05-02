package net.messagevortex.generator;

import net.messagevortex.asn1.PrefixBlock;
import net.messagevortex.asn1.RoutingCombo;
import net.messagevortex.asn1.UsagePeriod;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.visualization.RoutingBlockGenerator;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

public class ASN1Parser {
    final RoutingBlockGenerator generator = new RoutingBlockGenerator();
    ASN1Sequence sequencePayloadOps = new DERSequence(new ASN1Encodable[]{});
    ASN1TaggedObject payloadOps = new DERTaggedObject(true, 132, sequencePayloadOps);

    /**
     * <p>Parses a Treemap of paths to a routingcombo.</p>
     *
     * @param paths A Treemap of paths, which together build a valid routing for a message.
     * @return      Returns a routingcombo Object with the parsed message.
     */
    public RoutingCombo parse(TreeMap<Integer, Path> paths) throws IOException {
        ArrayList<ASN1Sequence> rc = new ArrayList<>();
        ASN1TaggedObject replySequence = new DERTaggedObject(true, 131, new DERSequence(new ASN1Encodable[]{new RoutingCombo().toAsn1Object(DumpType.INTERNAL), new ASN1Integer(1), new UsagePeriod().toAsn1Object(DumpType.INTERNAL)}));

        paths.forEach((key, value) -> {
            Subpath mainPath = value.getSubpaths().get(0);
            TreeMap<Integer, Integer> subpaths = new TreeMap<>();
            ASN1Sequence previousRC;
            ASN1TaggedObject rb = null;
            try {
                rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{new RoutingCombo().toAsn1Object(DumpType.INTERNAL)}));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if(value.getSubpaths().size() > 1) {
                for(int i = 1; i < value.getSubpaths().size(); i++) {
                    subpaths.put(i, value.getSubpaths().get(i).getStartSequenceNumber());
                }
            }

            ASN1Sequence blendingSpec = null;
            try {
                blendingSpec = generator.createBlendingSpec("smtp", String.valueOf(mainPath.getHops().lastEntry().getValue().getEndNode().ID), "attach", getBlendingParams());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                //previousRC = generator.createRoutingCombo(mainPath.getHops().lastEntry().getValue().getStartTime(), mainPath.getHops().lastEntry().getValue().getEndTime(), blendingSpec, parseOperations(mainPath.getHops().lastEntry().getValue()), getPrefix(1), rb, 1056531872L, replySequence);
                previousRC = generator.createRoutingCombo(mainPath.getHops().lastEntry().getValue().getStartTime(), mainPath.getHops().lastEntry().getValue().getEndTime(), blendingSpec, payloadOps, getPrefix(1), rb, 1056531872L, replySequence);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for(int i = mainPath.getHops().size() - 2; i >= 0; i--) {
                ASN1EncodableVector v = new ASN1EncodableVector();
                v.add(previousRC);

                int finalI = i;
                subpaths.forEach((sKey, sValue) -> {
                    if(finalI == sValue) {
                        try {
                            v.add(createSubpath(value.getSubpaths().get(sKey)));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                try {
                    blendingSpec = generator.createBlendingSpec("smtp", String.valueOf(mainPath.getHops().get(i).getEndNode().ID), "attach", getBlendingParams());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                rb = new DERTaggedObject(true, 333, new DERSequence(v));
                try {
                    //previousRC = generator.createRoutingCombo(mainPath.getHops().get(i).getStartTime(), mainPath.getHops().get(i).getEndTime(), blendingSpec, parseOperations(mainPath.getHops().get(i)), getPrefix(v.size()), rb, 1056531872L, replySequence);
                    previousRC = generator.createRoutingCombo(mainPath.getHops().get(i).getStartTime(), mainPath.getHops().get(i).getEndTime(), blendingSpec, payloadOps, getPrefix(v.size()), rb, 1056531872L, replySequence);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            rc.add(previousRC);
        });

        ASN1EncodableVector v = new ASN1EncodableVector();
        for (ASN1Sequence s : rc) {
            v.add(s);
        }

        ASN1Sequence blendingSpec = generator.createBlendingSpec("smtp", String.valueOf(paths.get(0).getSubpaths().get(0).getHops().get(0).getEndNode().ID), "attach", getBlendingParams());
        ASN1TaggedObject rb = new DERTaggedObject(true, 333, new DERSequence(v));
        ASN1Sequence initialRC = generator.createRoutingCombo(0, 10, blendingSpec, payloadOps, getPrefix(rc.size()), rb, 1L, replySequence);

        return new RoutingCombo(initialRC);
    }

    private ASN1TaggedObject parseOperations(Hop h) {
        ASN1EncodableVector v = new ASN1EncodableVector();

        h.getOperations().forEach((key, value) -> {
            try {
                v.add(value.toAsn1Object(DumpType.INTERNAL));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return new DERTaggedObject(true, 132, new DERSequence(v));
    }

    private ASN1Sequence createSubpath(Subpath path) throws IOException {
        ASN1Sequence previousRC;
        ASN1TaggedObject replySequence = new DERTaggedObject(true, 131, new DERSequence(new ASN1Encodable[]{new RoutingCombo().toAsn1Object(DumpType.INTERNAL), new ASN1Integer(1), new UsagePeriod().toAsn1Object(DumpType.INTERNAL)}));

        ASN1TaggedObject rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{new RoutingCombo().toAsn1Object(DumpType.INTERNAL)}));
        ASN1Sequence blendingSpec = generator.createBlendingSpec("smtp", String.valueOf(path.getHops().lastEntry().getValue().getEndNode().ID), "attach", getBlendingParams());
        previousRC = generator.createRoutingCombo(path.getHops().lastEntry().getValue().getStartTime(), path.getHops().lastEntry().getValue().getEndTime(), blendingSpec, payloadOps, getPrefix(1), rb, 1056531872L, replySequence);

        for(int i = path.getHops().size() - 2; i >= 0; i--) {
            rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{previousRC}));
            blendingSpec = generator.createBlendingSpec("smtp", String.valueOf(path.getHops().get(i).getEndNode().ID), "attach", getBlendingParams());
            previousRC = generator.createRoutingCombo(path.getHops().get(i).getStartTime(), path.getHops().get(i).getEndTime(), blendingSpec, payloadOps, getPrefix(1), rb, 1056531872L, replySequence);
        }

        return previousRC;
    }

    private TreeMap<Integer, ASN1TaggedObject> getBlendingParams() {
        Random rand = new Random();
        int blendingParams = rand.nextInt(127);
        TreeMap<Integer, ASN1TaggedObject> params = new TreeMap<>();

        for(int i = 0; i < blendingParams; i++) {
            params.put(i, generator.createBlendingParameter(rand.nextInt(15)));
        }

        return params;
    }

    private ASN1TaggedObject getPrefix(int size) throws IOException {
        ASN1EncodableVector blocks = new ASN1EncodableVector();

        for(int i = 0; i < size; i++) {
            blocks.add(new PrefixBlock().toAsn1Object(DumpType.INTERNAL));
        }

        return new DERTaggedObject(true, 331, new DERSequence(blocks));
    }
}

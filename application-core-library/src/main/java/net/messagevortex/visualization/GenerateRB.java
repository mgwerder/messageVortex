package net.messagevortex.visualization;

import net.messagevortex.asn1.PrefixBlock;
import net.messagevortex.asn1.RoutingCombo;
import net.messagevortex.asn1.SymmetricKey;
import net.messagevortex.asn1.UsagePeriod;
import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.TreeMap;

public class GenerateRB {
    public RoutingCombo generateRoutingBlock() throws IOException {
        RoutingBlockGenerator generator = new RoutingBlockGenerator();

        ASN1Sequence sequencePayloadOps = new DERSequence(new ASN1Encodable[]{});
        ASN1TaggedObject payloadOps = new DERTaggedObject(true, 132, sequencePayloadOps);

        ASN1TaggedObject operations = getOperations();

        ASN1TaggedObject rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{new RoutingCombo().toAsn1Object(DumpType.INTERNAL)}));

        ASN1TaggedObject replySequence = new DERTaggedObject(true, 131, new DERSequence(new ASN1Encodable[]{new RoutingCombo().toAsn1Object(DumpType.INTERNAL), new ASN1Integer(1), new UsagePeriod().toAsn1Object(DumpType.INTERNAL)}));

        long timeStamp = System.currentTimeMillis() / 100000;
        
        ASN1Sequence blendingSpec = generator.createBlendingSpec("smtp", "5", "attach", getBlendingParams(generator));
        ASN1Sequence routingCombo1 = generator.createRoutingCombo(timeStamp + 5000, timeStamp + 6000, blendingSpec, payloadOps, getPrefix(generator, 1), rb, 325187265871L, replySequence);

        blendingSpec = generator.createBlendingSpec("smtp", "2", "attach", getBlendingParams(generator));
        rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{new RoutingCombo().toAsn1Object(DumpType.INTERNAL)}));
        ASN1Sequence routingCombo2 = generator.createRoutingCombo(timeStamp + 4500, timeStamp + 5500, blendingSpec, payloadOps, getPrefix(generator, 1), rb, 325187265871L, replySequence);

        blendingSpec = generator.createBlendingSpec("smtp", "6", "attach", getBlendingParams(generator));
        rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{new RoutingCombo().toAsn1Object(DumpType.INTERNAL)}));
        ASN1Sequence routingCombo3 = generator.createRoutingCombo(timeStamp + 4000, timeStamp + 5000, blendingSpec, payloadOps, getPrefix(generator, 1), rb, 325187265871L, replySequence);

        blendingSpec = generator.createBlendingSpec("smtp", "4", "attach", getBlendingParams(generator));
        rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{new RoutingCombo().toAsn1Object(DumpType.INTERNAL)}));
        ASN1Sequence routingCombo4 = generator.createRoutingCombo(timeStamp + 3500, timeStamp + 4500, blendingSpec, payloadOps, getPrefix(generator, 1), rb, 325187265871L, replySequence);

        blendingSpec = generator.createBlendingSpec("smtp", "0", "attach", getBlendingParams(generator));
        rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{new RoutingCombo().toAsn1Object(DumpType.INTERNAL)}));
        ASN1Sequence routingCombo5 = generator.createRoutingCombo(timeStamp + 3000, timeStamp + 4000, blendingSpec, payloadOps, getPrefix(generator, 1), rb, 325187265871L, replySequence);

        blendingSpec = generator.createBlendingSpec("smtp", "4", "attach", getBlendingParams(generator));
        rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{new RoutingCombo().toAsn1Object(DumpType.INTERNAL)}));
        ASN1Sequence routingCombo6 = generator.createRoutingCombo(timeStamp + 2500, timeStamp + 3500, blendingSpec, payloadOps, getPrefix(generator, 1), rb, 325187265871L, replySequence);

        blendingSpec = generator.createBlendingSpec("smtp", "0", "attach", getBlendingParams(generator));
        rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{new RoutingCombo().toAsn1Object(DumpType.INTERNAL)}));
        ASN1Sequence routingCombo7 = generator.createRoutingCombo(timeStamp + 2000, timeStamp + 3000, blendingSpec, payloadOps, getPrefix(generator, 1), rb, 325187265871L, replySequence);

        blendingSpec = generator.createBlendingSpec("smtp", "0", "attach", getBlendingParams(generator));
        rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{new RoutingCombo().toAsn1Object(DumpType.INTERNAL)}));
        ASN1Sequence routingCombo8 = generator.createRoutingCombo(timeStamp + 1500, timeStamp + 2500, blendingSpec, payloadOps, getPrefix(generator, 1), rb, 325187265871L, replySequence);

        blendingSpec = generator.createBlendingSpec("smtp", "1", "attach", getBlendingParams(generator));
        rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{new RoutingCombo().toAsn1Object(DumpType.INTERNAL)}));
        ASN1Sequence routingCombo9 = generator.createRoutingCombo(timeStamp + 1000, timeStamp + 2000, blendingSpec, payloadOps, getPrefix(generator, 1), rb, 325187265871L, replySequence);

        blendingSpec = generator.createBlendingSpec("smtp", "6", "attach", getBlendingParams(generator));
        rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{routingCombo5}));
        ASN1Sequence routingCombo10 = generator.createRoutingCombo(timeStamp + 900, timeStamp + 1900, blendingSpec, payloadOps, getPrefix(generator, 1), rb, 325187265871L, replySequence);

        blendingSpec = generator.createBlendingSpec("smtp", "5", "attach", getBlendingParams(generator));
        rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{routingCombo4, routingCombo6, routingCombo8}));
        ASN1Sequence routingCombo11 = generator.createRoutingCombo(timeStamp + 800, timeStamp + 1800, blendingSpec, payloadOps, getPrefix(generator, 3), rb, 325187265871L, replySequence);

        blendingSpec = generator.createBlendingSpec("smtp", "3", "attach", getBlendingParams(generator));
        rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{routingCombo7, routingCombo9, routingCombo11}));
        ASN1Sequence routingCombo12 = generator.createRoutingCombo(timeStamp + 700, timeStamp + 1700, blendingSpec, payloadOps, getPrefix(generator, 3), rb, 325187265871L, replySequence);

        blendingSpec = generator.createBlendingSpec("smtp", "4", "attach", getBlendingParams(generator));
        rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{new RoutingCombo().toAsn1Object(DumpType.INTERNAL)}));
        ASN1Sequence routingCombo13 = generator.createRoutingCombo(timeStamp + 600, timeStamp + 1600, blendingSpec, payloadOps, getPrefix(generator, 1), rb, 325187265871L, replySequence);

        blendingSpec = generator.createBlendingSpec("smtp", "1", "attach", getBlendingParams(generator));
        rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{new RoutingCombo().toAsn1Object(DumpType.INTERNAL)}));
        ASN1Sequence routingCombo14 = generator.createRoutingCombo(timeStamp + 500, timeStamp + 1500, blendingSpec, payloadOps, getPrefix(generator, 1), rb, 325187265871L, replySequence);

        blendingSpec = generator.createBlendingSpec("smtp", "2", "attach", getBlendingParams(generator));
        rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{routingCombo3, routingCombo1}));
        ASN1Sequence routingCombo15 = generator.createRoutingCombo(timeStamp + 400, timeStamp + 1400, blendingSpec, payloadOps, getPrefix(generator, 2), rb, 325187265871L, replySequence);

        blendingSpec = generator.createBlendingSpec("smtp", "3", "attach", getBlendingParams(generator));
        rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{routingCombo2, routingCombo13}));
        ASN1Sequence routingCombo16 = generator.createRoutingCombo(timeStamp + 300, timeStamp + 1400, blendingSpec, payloadOps, getPrefix(generator, 2), rb, 325187265871L, replySequence);

        blendingSpec = generator.createBlendingSpec("smtp", "1", "attach", getBlendingParams(generator));
        rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{routingCombo15}));
        ASN1Sequence routingCombo17 = generator.createRoutingCombo(timeStamp + 200, timeStamp + 1200, blendingSpec, payloadOps, getPrefix(generator, 1), rb, 325187265871L, replySequence);

        blendingSpec = generator.createBlendingSpec("smtp", "0", "attach", getBlendingParams(generator));
        rb = new DERTaggedObject(true, 333, new DERSequence(new ASN1Encodable[]{routingCombo17, routingCombo16, routingCombo14, routingCombo12, routingCombo10}));
        ASN1Sequence initialRB = generator.createRoutingCombo(timeStamp + 100, timeStamp + 1100, blendingSpec, operations, getPrefix(generator, 5), rb, 325187265871L, replySequence);

        return new RoutingCombo(initialRB);
    }

    private TreeMap<Integer, ASN1TaggedObject> getBlendingParams(RoutingBlockGenerator generator) throws IOException {
        Random rand = new Random();
        int blendingParams = rand.nextInt(127);
        TreeMap<Integer, ASN1TaggedObject> params = new TreeMap<>();

        for(int i = 0; i < blendingParams; i++) {
            params.put(i, generator.createBlendingParameter(rand.nextInt(15)));
        }

        return params;
    }

    private ASN1TaggedObject getPrefix(RoutingBlockGenerator generator, int size) throws IOException {
        ASN1EncodableVector blocks = new ASN1EncodableVector();

        for(int i = 0; i < size; i++) {
            blocks.add(new PrefixBlock().toAsn1Object(DumpType.INTERNAL));
        }

        return new DERTaggedObject(true, 331, new DERSequence(blocks));
    }

    private ASN1TaggedObject getOperations() throws IOException {
        ArrayList<SymmetricKey> keys = new ArrayList<>();
        ArrayList<SymmetricKey> keys2 = new ArrayList<>();
        for(int i = 0; i < 14; i++) {
            keys.add(new SymmetricKey());
        }
        for(int i = 0; i < 10; i++) {
            keys2.add(new SymmetricKey());
        }

        SymmetricKey k = new SymmetricKey();
        OperationGenerator opGen = new OperationGenerator();
        PayloadOperation merge = new PayloadOperation(0, 15, 22);
        PayloadOperation split = new PayloadOperation(33, 532, opGen.createSizeBlock(209, 5325, 15001), 521);
        PayloadOperation map = new PayloadOperation(9789, 213);
        PayloadOperation addRedundancy = new PayloadOperation(400, 55, 10, 4, keys, 99, 2);
        PayloadOperation removeRedundancy = new PayloadOperation(410, 589, 10, 4, keys, 8523, 2);
        PayloadOperation Encrypt = new PayloadOperation(300, 589, 11111, k);
        PayloadOperation Decrypt = new PayloadOperation(310, 11111, 8906, k);
        PayloadOperation map2 = new PayloadOperation(8906, 5382);
        PayloadOperation Encrypt2 = new PayloadOperation(300, 22, 5322, k);
        PayloadOperation addRedundancy2 = new PayloadOperation(400, 678, 6, 4, keys2, 870, 2);
        PayloadOperation Merge2 = new PayloadOperation(5322, 873, 29843);
        PayloadOperation removeRedundancy2 = new PayloadOperation(410, 860, 8, 5, keys, 63939, 2);
        PayloadOperation addRedundancy3 = new PayloadOperation(400, 58323, 25, 10, keys, 681, 2);

        Operations ops = new Operations(new ArrayList<>(Arrays.asList(merge, split, map, addRedundancy, removeRedundancy, Encrypt, Decrypt, map2, Encrypt2, addRedundancy2, Merge2, removeRedundancy2, addRedundancy3)));

        return ops.getOperations();
    }
}

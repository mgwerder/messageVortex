package net.messagevortex.generator;

import net.messagevortex.asn1.RoutingCombo;
import net.messagevortex.visualization.Layout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Tests {
    private TreeMap<Integer, Path> paths;
    private final TreeMap<Integer, testIterationResult> results = new TreeMap<>();
    private final ArrayList<RoutingCombo> routingCombos = new ArrayList<>();

    public void executeTests(int testIterations) throws IOException {
        ASN1Parser parser = new ASN1Parser();
        for(int i = 0; i < testIterations; i++) {
            AtomicInteger hops = new AtomicInteger();
            TreeMap<Node, Integer> outgoingHops = prepareTreemap(new TreeMap<>());
            TreeMap<Node, Integer> incomingHops = prepareTreemap(new TreeMap<>());
            Algorithm3 alg = new Algorithm3();

            paths = alg.getPaths();

            paths.forEach((key, value) -> {
                value.getSubpaths().forEach((sKey, sValue) -> {
                    hops.addAndGet(sValue.getHops().size());

                    sValue.getHops().forEach((hKey, hValue) -> {
                        outgoingHops.put(hValue.getStartNode(), outgoingHops.get(hValue.getStartNode()) + 1);
                        incomingHops.put(hValue.getEndNode(), incomingHops.get(hValue.getEndNode()) + 1);
                  });
              });
            });

            results.put(i, new testIterationResult(outgoingHops, incomingHops));
            routingCombos.add(parser.parse(paths));
        }

        printTestResults(testIterations);
        Layout l = new Layout(routingCombos);
    }

    private TreeMap<Node, Integer> prepareTreemap(TreeMap<Node, Integer> treemap) {
        MainClass.allNodes.forEach((key, value) -> treemap.put(value, 0));

        return treemap;
    }

    private void printTestResults(int testIterations) {
        AtomicReference<Double> averageMeanDeviationOutgoing = new AtomicReference<>(0.0);
        AtomicReference<Double> averageMeanDeviationIncoming = new AtomicReference<>(0.0);
        AtomicReference<Double> maxMeanDeviationOutgoing = new AtomicReference<>(0.0);
        AtomicReference<Double> maxMeanDeviationIncoming = new AtomicReference<>(0.0);
        AtomicReference<Double> averageSenderIncoming = new AtomicReference<>(0.0);
        AtomicReference<Double> averageSenderOutgoing = new AtomicReference<>(0.0);
        AtomicReference<Double> averageReceiverIncoming = new AtomicReference<>(0.0);
        AtomicReference<Double> averageReceiverOutgoing = new AtomicReference<>(0.0);

        results.forEach((key, value) -> {
            AtomicReference<Double> averageOutgoing = new AtomicReference<>(0.0);
            AtomicReference<Double> averageIncoming = new AtomicReference<>(0.0);
            AtomicReference<Double> averageMeanDeviationOutgoingLocal = new AtomicReference<>(0.0);
            AtomicReference<Double> averageMeanDeviationIncomingLocal = new AtomicReference<>(0.0);

            MainClass.allNodes.forEach((nKey, nValue) -> {
                averageOutgoing.updateAndGet(v -> v + value.getAbsoluteOutgoing().get(nValue));
                averageIncoming.updateAndGet(v -> v + value.getAbsoluteIncoming().get(nValue));
            });

            averageOutgoing.updateAndGet(v -> v / MainClass.allNodes.size());
            averageIncoming.updateAndGet(v -> v / MainClass.allNodes.size());

            MainClass.allNodes.forEach((nKey, nValue) -> {
                averageMeanDeviationOutgoingLocal.updateAndGet(v -> v + Math.abs(averageOutgoing.get() - value.getAbsoluteOutgoing().get(nValue)));
                averageMeanDeviationIncomingLocal.updateAndGet(v -> v + Math.abs(averageIncoming.get() - value.getAbsoluteIncoming().get(nValue)));

                maxMeanDeviationOutgoing.set(Math.max(Math.abs(averageOutgoing.get() - value.getAbsoluteOutgoing().get(nValue)), maxMeanDeviationOutgoing.get()));
                maxMeanDeviationIncoming.set(Math.max(Math.abs(averageIncoming.get() - value.getAbsoluteIncoming().get(nValue)), maxMeanDeviationIncoming.get()));
            });

            averageMeanDeviationOutgoingLocal.updateAndGet(v -> v / MainClass.allNodes.size());
            averageMeanDeviationIncomingLocal.updateAndGet(v -> v / MainClass.allNodes.size());

            averageMeanDeviationOutgoing.updateAndGet(v -> v + averageMeanDeviationOutgoingLocal.get());
            averageMeanDeviationIncoming.updateAndGet(v -> v + averageMeanDeviationIncomingLocal.get());

            averageSenderIncoming.updateAndGet(v -> v + value.getAbsoluteIncoming().get(MainClass.sender));
            averageSenderOutgoing.updateAndGet(v -> v + value.getAbsoluteOutgoing().get(MainClass.sender));
            averageReceiverIncoming.updateAndGet(v -> v + value.getAbsoluteIncoming().get(MainClass.receiver));
            averageReceiverOutgoing.updateAndGet(v -> v + value.getAbsoluteOutgoing().get(MainClass.receiver));
        });

        averageMeanDeviationOutgoing.updateAndGet(v -> v / results.size());
        averageMeanDeviationIncoming.updateAndGet(v -> v / results.size());

        averageSenderIncoming.updateAndGet(v -> v / results.size());
        averageSenderOutgoing.updateAndGet(v -> v / results.size());
        averageReceiverIncoming.updateAndGet(v -> v / results.size());
        averageReceiverOutgoing.updateAndGet(v -> v / results.size());

        System.out.format("%20s%8s%n", "Average Mean Deviation Outgoing", "Max Mean Deviation");
        System.out.format("%20s%8s%n", averageMeanDeviationOutgoing.get() + "   ", maxMeanDeviationOutgoing);

        System.out.format("%20s%8s%n", "Average Mean Deviation Incoming", "Max Mean Deviation");
        System.out.format("%20s%8s%n", averageMeanDeviationIncoming.get() + "   ", maxMeanDeviationIncoming);

        System.out.format("%n%n");
        System.out.format("%10s%10s%4s%10s%4s%n", "Sender: ",  "Incoming: ", averageSenderIncoming, " Outgoing: ", averageSenderOutgoing);
        System.out.format("%10s%10s%4s%10s%4s%n", "Receiver: ",  "Incoming: ", averageReceiverIncoming, " Outgoing: ", averageReceiverOutgoing);
    }
}

package net.messagevortex.generator;

import net.messagevortex.ExtendedSecureRandom;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

public class Algorithm3 {
    public TreeMap<Integer, Path> paths = new TreeMap<>();
    public TreeMap<Integer, Hop> hops = new TreeMap<>();

    public Algorithm3() {
        Node n = MainClass.receiver;

        AnonymitySet.createAnonymitySet(MainClass.receiver);
        Parameter.assignParameter(2);
        createHops();

        while(!tryPaths()) {
            hops = new TreeMap<>();
            createHops();
        }

        //buildDecoys();

        paths.forEach((key, value) -> assignTimeSlots(value, 0));

        paths.forEach((pKey, pValue) -> pValue.getSubpaths().get(0).getHops().forEach((hKey, hValue) -> {
            System.out.print(hValue.getStartNode().ID);
            System.out.print("|");
            System.out.print(hValue.getEndNode().ID);
            System.out.print("|");
            System.out.print(hValue.getStartTime());
            System.out.print("|");
            System.out.println(hValue.getEndTime());
        }));
    }

    public TreeMap<Integer, Path> getPaths() {
        return this.paths;
    }

    private boolean tryPaths() {
        int attempt = 0;
        while(paths.isEmpty() || paths.size() < Math.max(Parameter.redundantNodes + 1, (Math.round((float) AnonymitySet.getNodes().size() / 8) + 2))) {
            if(attempt == 5)
                break;

            buildPaths();
            attempt += 1;
        }

        return attempt != 5;
    }

    private void createHops() {
        int hopsNumber = (int) (Math.random() * (AnonymitySet.getNodes().size() * (Parameter.redundantNodes + 50) - AnonymitySet.getNodes().size() * (Parameter.redundantNodes + 40)) + AnonymitySet.getNodes().size() * (Parameter.redundantNodes + 15));
        TreeMap<Node, Integer> usedNodes = new TreeMap<>();
        Node sender = MainClass.sender;
        Node receiver = MainClass.getRandomNode(AnonymitySet.getNodes());

        AnonymitySet.getNodes().forEach((key, value) -> usedNodes.put(value, 0));

        for(int i = 0; i < hopsNumber; i++) {
            while(sender == receiver)
                receiver = MainClass.getRandomNode(AnonymitySet.getNodes());

            usedNodes.put(receiver, usedNodes.get(receiver) + 1);

            hops.put(getLastKey(hops) + 1, new Hop(sender, receiver, -1));

            do sender = MainClass.getRandomNode(AnonymitySet.getNodes());
            while (usedNodes.get(sender) == 0);
            receiver = MainClass.getRandomNode(AnonymitySet.getNodes());
        }

        usedNodes.forEach((key, value) -> {
            if(value < 4) {
                while(usedNodes.get(key) <= 4) {
                    Node randomSender = MainClass.getRandomNode(AnonymitySet.getNodes());

                    while(randomSender == key || usedNodes.get(randomSender) == 0)
                        randomSender = MainClass.getRandomNode(AnonymitySet.getNodes());

                    usedNodes.put(key, usedNodes.get(key) + 1);

                    hops.put(getLastKey(hops) + 1, new Hop(randomSender, key, -1));
                }
            }
        });
    }

    private void buildPaths() {
        int combinedPathLength = AnonymitySet.getNodes().size();
        TreeMap<Integer, Node> availableNodes = new TreeMap<>(AnonymitySet.getNodes());
        availableNodes.remove(0);

        for(int i = 0; i < Math.max(Parameter.redundantNodes + 1, (Math.round((float) AnonymitySet.getNodes().size() / 8) + 2)); i++) {
            int length = (int) (Math.random() * (Math.min(combinedPathLength, 8) - 3) + 3);
            combinedPathLength -= length;
            int receiverPosition = (int) (Math.random() * (length - 1) + 1);
            Node previousSender = MainClass.sender;
            Path p = new Path();

            if(receiverPosition == 1) {
                Hop h = findHopWithReceiverAndSender(previousSender, MainClass.receiver);
                if(h == null) {
                    hops = new TreeMap<>();
                    return;
                }
                p.addHop(h, 0);
                previousSender = MainClass.receiver;
                availableNodes.remove(getKey(previousSender, availableNodes));
            } else {
                for(int j = 0; j < receiverPosition - 1; j++) {
                    Hop h = findHopWithSender(previousSender);
                    if(h == null) {
                        paths = new TreeMap<>();
                        return;
                    }
                    while(!availableNodes.containsValue(h.getEndNode()))
                        h = findHopWithSender(previousSender);

                    previousSender = h.getEndNode();
                    p.addHop(h, 0);
                    availableNodes.remove(getKey(previousSender, availableNodes));
                }

                Hop h = findHopWithReceiverAndSender(previousSender, MainClass.receiver);
                if(h == null) {
                    hops = new TreeMap<>();
                    return;
                }
                p.addHop(h, 0);
                previousSender = MainClass.receiver;
                availableNodes.remove(getKey(previousSender, availableNodes));
            }

            for(int j = 0; j < length - receiverPosition; j++) {
                Hop h = findHopWithSender(previousSender);
                if(h == null) {
                    paths = new TreeMap<>();
                    return;
                }
                while(!availableNodes.containsValue(h.getEndNode()))
                    h = findHopWithSender(previousSender);

                previousSender = h.getEndNode();
                p.addHop(h, 0);
                availableNodes.remove(getKey(previousSender, availableNodes));
            }

            paths.put(getLastKey(paths) + 1, p);
        }
    }

    private void buildDecoys() {
        int amount = (int) (Math.random() * ((paths.size() + 6) - (paths.size() - 2)) + (paths.size() - 2));

        for(int i = 0; i < amount; i++) {
            int length = (int) (Math.random() * (12 - 4) + 4);
            TreeMap<Integer, Node> availableNodes = new TreeMap<>(AnonymitySet.getNodes());
            availableNodes.remove(0);
            Path p = paths.get((int) (Math.random() * (paths.size() - 1) + 1));
            Subpath s = p.getSubpaths().get(0);
            int index = (int) (Math.random() * (s.getHops().size() - 1) + 1);
            Node previousSender = s.getHops().get(index).getEndNode();
            TreeMap<Integer, Hop> hops = new TreeMap<>();

            for(int j = 0; j < length; j++) {
                Node r = MainClass.getRandomNode(availableNodes);
                Hop h = new Hop(previousSender, r, -1);
                availableNodes.remove(getKey(r, availableNodes));

                previousSender = r;
                hops.put(getLastKey(hops) + 1, h);
            }

            p.insertSubpath(new Subpath(index, hops));
        }

        paths.forEach((key, value) -> {
            if(value.getSubpaths().size() <= 1)
                return;

            assignDecoyTimeSlots(value, key);
        });
    }

    private void assignTimeSlots(Path p, int subpath) {
        long previousEndTime = 0;
        for(int i = 0; i < p.getSubpaths().get(subpath).getHops().size(); i++) {
            long nextStart = (long) (ExtendedSecureRandom.nextRandomTime(previousEndTime, previousEndTime + 30, previousEndTime + 35));
            previousEndTime = (long) (ExtendedSecureRandom.nextRandomTime(nextStart + 2, nextStart + 10, nextStart + 15));

            p.getSubpaths().get(subpath).getHops().get(i).setStartTime(nextStart);
            p.getSubpaths().get(subpath).getHops().get(i).setEndTime(previousEndTime);
        }
    }

    private void assignDecoyTimeSlots(Path p, int index) {
        long maxDeliveryTime = 5000L;
        AtomicLong startTime = new AtomicLong((long) (ExtendedSecureRandom.nextRandomTime(180, 220, 300) * (Math.random() * (index + 1))));
        long share = maxDeliveryTime / p.getSubpaths().get(0).getHops().size();

        for(int i = 1; i < p.getSubpaths().size(); i++) {
            startTime.set(p.getSubpaths().get(0).getHops().get(p.getSubpaths().get(i).getStartSequenceNumber()).getStartTime());
            p.getSubpaths().get(i).getHops().forEach((hKey, hValue) -> {
                long nextStart = (long) (ExtendedSecureRandom.nextRandomTime(startTime.get(), startTime.get() + share - 5, startTime.get() + share - 3));
                long nextEnd = (long) (ExtendedSecureRandom.nextRandomTime(startTime.get() + share - 2, startTime.get() + share - 1, startTime.get() + share));
                hValue.setStartTime(nextStart);
                hValue.setEndTime(nextEnd);
                startTime.set(nextEnd);
            });
        }
    }

    private Hop findHopWithSender(Node s) {
        ArrayList<Hop> selectedHops = new ArrayList<>();

        hops.forEach((key, value) -> {
            if(value.getStartNode() == s)
                selectedHops.add(value);
        });

        if(!selectedHops.isEmpty())
            return selectedHops.get((int) Math.floor(Math.random() * selectedHops.size()));
        else return null;
    }

    private Hop findHopWithReceiverAndSender(Node s, Node r) {
        ArrayList<Hop> selectedHops = new ArrayList<>();

        hops.forEach((key, value) -> {
            if(value.getEndNode() == r && value.getStartNode() == s)
                selectedHops.add(value);
        });

        if(!selectedHops.isEmpty())
            return selectedHops.get((int) Math.floor(Math.random() * selectedHops.size()));
        else return null;
    }

    private Integer getLastKey(TreeMap<Integer, ? extends GeneratorObject> tm) {
        if(!tm.isEmpty())
            return tm.lastKey();
        return -1;
    }

    private Integer getKey(Node n, TreeMap<Integer, ? extends GeneratorObject> tm) {
        int keyForValue = -1;
        for (Integer key : tm.keySet()) {
            if(tm.get(key) == n) {
                keyForValue = key;
                break;
            }
        }

        return keyForValue;
    }

    private Integer getRandomKey(TreeMap<Integer, Hop> tm) {
        ArrayList<Integer> keys = new ArrayList<Integer>(tm.keySet());

        return keys.get((int) (Math.random() * (keys.size() - 1)));
    }
}

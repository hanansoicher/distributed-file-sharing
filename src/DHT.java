import java.util.ArrayList;
import java.util.List;

public class DHT {
    private List<Node> nodes;
    private final int m; // Bits in identifier space

    public DHT(int m) {
        this.nodes = new ArrayList<>();
        this.m = m;
    }

    public int getM() {
        return m;
    }

    public Node findSuccessor(int keyId) {
        return nodes.get(0).findSuccessor(keyId);
    }

    public Node findPredecessor(int keyId) {
        Node node = nodes.get(0);
        while (!isInRange(keyId, node.getId(), node.getSuccessor().getId())) {
            node = node.closestPrecedingNode(keyId);
        }
        return node;
    }

    public void addNode(Node node) {
        if (!nodes.isEmpty()) {
            node.join(this, nodes.get(0));
        } else {
            node.join(this, null);
        }
        nodes.add(node);
    }


    public void removeNode(Node node) {
        // Remove node from Chord ring
        node.getPredecessor().setSuccessor(node.getSuccessor());
        node.getSuccessor().setPredecessor(node.getPredecessor());

        // Transfer files from departing node to successor
        node.getSuccessor().getFiles().putAll(node.getFiles());

        // Remove node from the nodes array
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i) != null && nodes.get(i).getId() == node.getId()) {
                nodes.set(i, null);
                break;
            }
        }
        updateFingerTables(node);
    }

    public void updateFingerTables(Node node) {
        for (Node n : nodes) {
            if (n != null) {
                n.updateFingerTable(this);
            }
        }
    }

    private boolean isInRange(int key, int start, int end) {
        if (start < end) {
            return key > start && key < end;
        } else { // Range wraps around  identifier space
            return key > start || key < end;
        }
    }
}

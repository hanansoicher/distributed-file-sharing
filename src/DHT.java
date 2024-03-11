import java.util.ArrayList;
import java.util.List;

public class DHT {
    private List<Node> nodes;
    private final int m; // Number of bits in the identifier space

    public DHT(int m) {
        this.nodes = new ArrayList<>();
        this.m = m;
    }

    public int getM() {
        return m;
    }

    public Node findSuccessor(int keyId) {
        // Assuming nodes[0] is an arbitrary node in the network
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
        // Add the node to the network and update its finger table
        if (nodes.get(0) != null) {
            node.join(this, nodes.get(0));
        } else {
            node.join(this, null);
        }
        // Add the node to the nodes array (for simplicity, assuming no removals)
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i) == null) {
                nodes.set(i, node);
                break;
            }
        }
    }

    public void removeNode(Node node) {
        // Remove the node from the Chord ring
        node.getPredecessor().setSuccessor(node.getSuccessor());
        node.getSuccessor().setPredecessor(node.getPredecessor());

        // Transfer files from the departing node to its successor
        node.getSuccessor().getFiles().putAll(node.getFiles());

        // Remove the node from the nodes array
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i) != null && nodes.get(i).getId() == node.getId()) {
                nodes.set(i, null);
                break;
            }
        }

        // Update the finger tables of other nodes
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
        } else { // The range wraps around the identifier space
            return key > start || key < end;
        }
    }


    // Additional methods for maintaining the Chord ring
}

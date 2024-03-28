package com.dfs.distributedfilesharing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DHT {
    private List<Node> nodes;

    @Value("${node.m}")
    private int m;

    public DHT(@Value("${node.m}") int m) {
        this.nodes = new ArrayList<>();
        this.m = m;
    }

    public int getM() {
        return m;
    }

    public Node findSuccessor(int keyId) {
        if (nodes.isEmpty()) {
            throw new IllegalStateException("DHT is empty");
        }
        return nodes.get(0).findSuccessor(keyId);
    }

    public Node findPredecessor(int keyId) {
        if (nodes.isEmpty()) {
            throw new IllegalStateException("DHT is empty");
        }
        Node node = nodes.get(0);
        while (!isInRange(keyId, node.getId(), node.getSuccessor().getId())) {
            node = node.closestPrecedingNode(keyId);
        }
        return node;
    }

    public void addNode(Node node) {
        if (nodes.stream().anyMatch(n -> n.getId() == node.getId())) {
            throw new IllegalArgumentException("Node with this ID already exists");
        }
        node.join(this, nodes.isEmpty() ? null : nodes.get(0));
        nodes.add(node);
    }

    public void removeNode(Node node) {
        if (!nodes.remove(node)) {
            throw new IllegalArgumentException("Node not found in DHT");
        }
        node.getPredecessor().setSuccessor(node.getSuccessor());
        node.getSuccessor().setPredecessor(node.getPredecessor());
        node.getSuccessor().getFiles().putAll(node.getFiles());
        updateFingerTables();
    }

    public void updateFingerTables() {
        for (Node n : nodes) {
            n.updateFingerTable(this);
        }
    }

    private boolean isInRange(int key, int start, int end) {
        if (start < end) {
            return key > start && key < end;
        } else { // Range wraps around identifier space
            return key > start || key < end;
        }
    }
}

package com.sternkn.djvu.file.chunks.annotations;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private final List<String> arguments;
    private final List<Node> children;

    public Node() {
        this.arguments = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public void addArgument(String argument) {
        this.arguments.add(argument);
    }
    public List<String> getArguments() {
        return arguments;
    }

    public void addChild(Node child) {
        this.children.add(child);
    }
    public List<Node> getChildren() {
        return children;
    }
}

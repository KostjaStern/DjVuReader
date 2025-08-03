package com.sternkn.djvu.file.chunks.annotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Node {

    private final List<String> arguments;
    private final List<Node> children;

    public Node() {
        this.arguments = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public Node(List<String> arguments) {
        this();
        this.arguments.addAll(arguments);
    }

    public Node(List<String> arguments, List<Node> children) {
        this(arguments);
        this.children.addAll(children);
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Node other)) {
            return false;
        }

        return arguments.equals(other.arguments)
                && children.equals(other.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arguments, children);
    }
}

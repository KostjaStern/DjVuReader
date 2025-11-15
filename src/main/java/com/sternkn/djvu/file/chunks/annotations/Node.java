/*
    Copyright (C) 2025 Kostya Stern

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation; either version 2 of the License, or (at your option)
    any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc., 51
    Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
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

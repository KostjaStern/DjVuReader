package com.sternkn.djvu.file.chunks;


import java.util.Objects;

public class ComponentInfo {

    private long offset;
    private int size;
    private int flag;
    private String id;
    private String name;
    private String title;

    public ComponentInfo() {
    }

    public long getOffset() {
        return offset;
    }
    public ComponentInfo setOffset(long offset) {
        this.offset = offset;
        return this;
    }

    public int getSize() {
        return size;
    }
    public ComponentInfo setSize(int size) {
        this.size = size;
        return this;
    }

    public int getFlag() {
        return flag;
    }
    public ComponentInfo setFlag(int flag) {
        this.flag = flag;
        return this;
    }

    public String getId() {
        return id;
    }
    public ComponentInfo setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }
    public ComponentInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getTitle() {
        return title;
    }
    public ComponentInfo setTitle(String title) {
        this.title = title;
        return this;
    }

    public boolean hasName() {
        return (flag & 0x80) == 0x80;
    }

    public boolean hasTitle() {
        return (flag & 0x40) == 0x40;
    }

    public ComponentType getType() {
        return ComponentType.valueOf(flag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, size, flag, id, name, title);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ComponentInfo other)) {
            return false;
        }

        return Objects.equals(this.offset, other.offset)
                && Objects.equals(this.size, other.size)
                && Objects.equals(this.flag, other.flag)
                && Objects.equals(this.id, other.id)
                && Objects.equals(this.name, other.name)
                && Objects.equals(this.title, other.title);
    }

    @Override
    public String toString() {
        final boolean hasName = hasName();
        final boolean hasTitle = hasTitle();
        final ComponentType type = getType();

        return String.format("ComponentInfo{offset = %s, size = %s, flag = %s, id = %s, " +
                        "name = %s, title = %s, type = %s, hasName = %s, hasTitle = %s }",
                offset, size, flag, id, name, title, type, hasName, hasTitle);
    }
}

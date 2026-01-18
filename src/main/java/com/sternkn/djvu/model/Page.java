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
package com.sternkn.djvu.model;

import java.util.Objects;

public class Page {
    private final Long offset;
    private final String id;

    public Page(Long offset, String id) {
        this.offset = offset;
        this.id = id;
    }

    public Long getOffset() {
        return offset;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Page other)) {
            return false;
        }
        return Objects.equals(offset, other.offset)
               && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, id);
    }

    @Override
    public String toString() {
        return "Page{offset: " + offset + ", id: '" + id + "'}";
    }
}

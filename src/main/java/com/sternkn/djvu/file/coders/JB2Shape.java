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
package com.sternkn.djvu.file.coders;

/**
 *  Shape data structure. A {JB2Image} contains an array of {JB2Shape} data structures.
 *  Each array entry represents an elementary blob of ink such as a character or a segment of line art.
 *  Member {bits} points to a bilevel image representing the shape pixels.
 *  Member #parent# is the subscript of the parent shape.
 **/
public class JB2Shape implements Parent {

    /** Subscript of the parent shape.  The parent shape must always be located
     before the current shape in the shape array.  A negative value indicates
     that this shape has no parent.  Any negative values smaller than #-1#
     further indicates that this shape does not look like a character.  This
     is used to enable a few internal optimizations.  This information is
     saved into the JB2 file, but the actual value of the #parent# variable
     is not. */
    private int parent;

    /** Bilevel image of the shape pixels.  This must be a pointer to a bilevel
     #GBitmap# image.  This pointer can also be null. The encoder will just
     silently discard all blits referring to a shape containing a null
     bitmap. */
    private GBitmap bits;

    /** Private user data. This long word is provided as a convenience for users
     of the JB2Image data structures.  Neither the rendering functions nor
     the coding functions ever access this value. */
    private long userdata;

    public JB2Shape() {

    }

    public int getParent() {
        return parent;
    }

    @Override
    public void setParent(int parent) {
        this.parent = parent;
    }

    public GBitmap getBits() {
        return bits;
    }
    public void setBits(GBitmap bits) {
        this.bits = bits;
    }

    public long getUserdata() {
        return userdata;
    }
    public void setUserdata(long userdata) {
        this.userdata = userdata;
    }
}

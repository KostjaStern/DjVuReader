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
package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.DjVuFileException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.sternkn.djvu.utils.InputStreamUtils.read16;
import static com.sternkn.djvu.utils.InputStreamUtils.read24;

public class TextZone {

    private static final int OFFSET = 0x8000;

    private final int zoneId;
    private final TextZoneType type;
    private GRect rect;
    private int textStart;
    private int textLength;
    private TextZone parent;
    private List<TextZone> children;

    public TextZone(TextZoneType type, int zoneId) {
        this.type = type;
        this.zoneId = zoneId;
        children = new ArrayList<>();
    }

    public int getZoneId() {
        return zoneId;
    }

    public TextZoneType getType() {
        return type;
    }

    public GRect getRect() {
        return rect;
    }
    public void setRect(GRect rect) {
        this.rect = rect;
    }

    public int getTextStart() {
        return textStart;
    }
    public void setTextStart(int textStart) {
        this.textStart = textStart;
    }

    public int getTextLength() {
        return textLength;
    }
    public void setTextLength(int textLength) {
        this.textLength = textLength;
    }

    private TextZone getParent() {
        return parent;
    }
    public void setParent(TextZone parent) {
        this.parent = parent;
    }

    public List<TextZone> getChildren() {
        return children;
    }
    public void setChildren(List<TextZone> children) {
        this.children = children;
    }

    int decode(InputStream inputStream, int maxtext, int zoneId, TextZone parent, TextZone prev) {

        int x = read16(inputStream) - OFFSET;
        int y = read16(inputStream) - OFFSET;
        int width = read16(inputStream) - OFFSET;
        int height = read16(inputStream) - OFFSET;

        int txtStart =  read16(inputStream) - OFFSET;
        int txtLength = read24(inputStream);

        if (prev != null) {
            if (type == TextZoneType.PAGE || type == TextZoneType.PARAGRAPH || type == TextZoneType.LINE) {
                x = x + prev.getRect().xmin();
                y = prev.getRect().ymin() - (y + height);
            }
            else {
                x = x + prev.getRect().xmax();
                y = y + prev.getRect().ymin();
            }
            txtStart += prev.getTextStart() + prev.getTextLength();
        }
        else if (parent != null) {
            x = x + parent.getRect().xmin();
            y = parent.getRect().ymax() - (y + height);
            txtStart += parent.getTextStart();
        }

        this.rect = new GRect(x, y, x + width, y + height);
        this.textStart = txtStart;
        this.textLength = txtLength;

        int size = read24(inputStream);
        if (rect.isEmpty() || textStart < 0 || (textStart + textLength) > maxtext) {
            throw new DjVuFileException("DjVuText.corrupt_text");
        }

        TextZone prevChild = null;
        while (size > 0) {
            TextZone zone = appendChild(inputStream, zoneId);
            zoneId = zone.getZoneId();
            zoneId = zone.decode(inputStream, maxtext, zoneId, this, prevChild);
            prevChild = zone;
            size--;
        }

        return zoneId;
    }

    private TextZone appendChild(InputStream inputStream, int zoneId) {
        try {
            int typeCode = inputStream.read();
            TextZoneType type = TextZoneType.valueOf(typeCode);
            TextZone textZone = new TextZone(type, zoneId + 1);
            textZone.setParent(this);
            children.add(textZone);
            return textZone;
        }
        catch (IOException e) {
            throw new DjVuFileException("We can not add child text zone", e);
        }
    }
}

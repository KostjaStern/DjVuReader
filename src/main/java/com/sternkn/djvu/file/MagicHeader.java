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
package com.sternkn.djvu.file;

public enum MagicHeader {
    AT_T("AT&T"),
    SDJV("SDJV");

    private final String header;

    MagicHeader(String header) {
        this.header = header;
    }

    public static MagicHeader of(String header) {
        for (MagicHeader magicHeader : MagicHeader.values()) {
            if (magicHeader.header.equals(header)) {
                return magicHeader;
            }
        }

        throw new DjVuFileException(String.format("Unexpected magic file header: %s", header));
    }
}

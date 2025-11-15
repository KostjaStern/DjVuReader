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

/**
 *  8.3.4.1.3 Initial Display level
 *
 *  (mode modevalue)
 */
public class InitialDisplayLevel extends Annotation {
    private final ModeType modeType;

    public InitialDisplayLevel(ModeType modeType) {
        super(AnnotationType.INITIAL_DISPLAY_LEVEL);
        this.modeType = modeType;
    }

    public ModeType getModeType() {
        return modeType;
    }

    @Override
    public String toString() {
        return String.format("{modeType: %s}", modeType);
    }
}

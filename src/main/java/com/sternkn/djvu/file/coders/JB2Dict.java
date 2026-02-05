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

import com.sternkn.djvu.file.DjVuFileException;

import java.util.ArrayList;
import java.util.List;

/**
 * JB2 Dictionary - class {JB2Dict} is a peculiar kind of {JB2Image} which only contains an array of shapes.
 * Several JB2Images can use shapes from a same JB2Dict encoded separately.
 * This is how several pages of a same document can share information.
 */
public class JB2Dict implements Dict {

    private JB2Dict inheritedDictionary;
    private final List<JB2Shape> shapes;
    private List<Integer> lib2shape;
    private final List<LibRect> boxes;
    private String comment;

    public JB2Dict() {
        this.shapes = new ArrayList<>();
        this.boxes = new ArrayList<>();

        lib2shape = new ArrayList<>();
    }

    @Override
    public JB2Dict getInheritedDictionary() {
        return inheritedDictionary;
    }

    @Override
    public void setInheritedDictionary(JB2Dict dictionary) {
        this.inheritedDictionary = dictionary;
    }

    @Override
    public int getInheritedShapeCount() {
        return this.inheritedDictionary == null ? 0 : this.inheritedDictionary.getShapeCount();
    }

    @Override
    public List<Integer> getLib2shape() {
        return lib2shape;
    }

    @Override
    public void initLibrary() {
        int nshape = getInheritedShapeCount();

        lib2shape = new ArrayList<>(nshape);

        for (int i = 0; i < nshape; i++) {
            lib2shape.add(i);

            LibRect libRect = this.inheritedDictionary.get_bounding_box(i);
            boxes.add(libRect);
        }
    }

    public int add_library(int shapeno, JB2Shape shape) {
        final int libno = lib2shape.size();
        lib2shape.add(shapeno);
        final LibRect libRect = new LibRect();
        libRect.compute_bounding_box(shape.getBits());
        boxes.add(libRect);

        return libno;
    }

    public LibRect get_bounding_box(int shapeno) {
        final JB2Shape shape = getShape(shapeno);
        LibRect libRect = new LibRect();
        libRect.compute_bounding_box(shape.getBits());

        return libRect;
    }

    public LibRect get_lib(int index) {
        return boxes.get(index);
    }

    @Override
    public JB2Shape getShape(int shapeno) {
        int inheritedShapes = getInheritedShapeCount();

        if(shapeno >= inheritedShapes) {
            return shapes.get(shapeno - inheritedShapes);
        }

        if (this.inheritedDictionary != null) {
            return this.inheritedDictionary.getShape(shapeno);
        }

        throw new DjVuFileException("JB2Image.bad_number");
    }

    @Override
    public int addShape(JB2Shape shape) {
        if (shape.getParent() >= getShapeCount()) {
            throw new DjVuFileException("JB2Image.bad_parent_shape");
        }

        int index = shapes.size();
        shapes.add(shape);
        return index + getInheritedShapeCount();
    }

    @Override
    public int getShapeCount() {
        return getInheritedShapeCount() + shapes.size();
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }
}

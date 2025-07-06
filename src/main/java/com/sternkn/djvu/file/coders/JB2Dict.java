package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;

import java.util.ArrayList;
import java.util.List;

/**
 * JB2 Dictionary - class {JB2Dict} is a peculiar kind of {JB2Image} which only contains an array of shapes.
 * These shapes can be referenced from another JB2Dict/JB2Image. This is arranged by setting the `inherited dictionary'
 * of a JB2Dict/JB2Image using function {JB2Dict.setInheritedDict}. Several JB2Images can use shapes from a
 * same JB2Dict encoded separately. This is how several pages of a same document can share information.
 */
public class JB2Dict implements Dict {

    private int inheritedShapes;
    private JB2Dict inheritedDict;

    private List<JB2Shape> shapes;
    private List<LibRect> boxes;

    private List<Integer> shape2lib;
    private List<Integer> lib2shape;
    private List<LibRect> libinfo;

    public JB2Dict() {
        this.inheritedShapes = 0;
        this.inheritedDict = null;
        this.shapes = new ArrayList<>();
        this.boxes = new ArrayList<>();

        this.libinfo = new ArrayList<>();
    }

    public int getInheritedShapes() {
        return inheritedShapes;
    }
    public void setInheritedShapes(int inheritedShapes) {
        this.inheritedShapes = inheritedShapes;
    }

    public JB2Dict getInheritedDict() {
        return inheritedDict;
    }
    public void setInheritedDict(JB2Dict inheritedDict) {
        this.inheritedDict = inheritedDict;
    }

    public List<JB2Shape> getShapes() {
        return shapes;
    }
    public void setShapes(List<JB2Shape> shapes) {
        this.shapes = shapes;
    }

    public void init_library() {
        int nshape = this.getInheritedShapes();

        shape2lib = new ArrayList<>(nshape);
        lib2shape = new ArrayList<>(nshape);

        for (int i = 0; i < nshape; i++) {
            shape2lib.add(i);
            lib2shape.add(i);

            this.get_bounding_box(i, libinfo.get(i));
        }
    }

    @Override
    public List<Integer> getLib2shape() {
        return lib2shape;
    }

    public int add_library(int shapeno, JB2Shape jshp) {
        final int libno = lib2shape.size();
        lib2shape.add(shapeno);

        shape2lib.add(libno);

        final LibRect libRect = new LibRect();
        libRect.compute_bounding_box(jshp.getBits());

        libinfo.add(libRect);

        return libno;
    }

    public LibRect get_bounding_box(int shapeno, LibRect dest) {
        LibRect libRect = dest;
        if (shapeno < inheritedShapes && inheritedDict != null) {
            libRect = inheritedDict.get_bounding_box(shapeno, libRect);
        }
        else if (shapeno >= inheritedShapes &&
                shapeno < inheritedShapes + boxes.size())
        {
            libRect = boxes.get(shapeno - inheritedShapes);
        }
        else
        {
            final JB2Shape shape = get_shape(shapeno);
            libRect = new LibRect();
            libRect.compute_bounding_box(shape.getBits());
        }
        return libRect;
    }

    public LibRect get_lib(int index) {
        return libinfo.get(index);
    }

    public JB2Shape get_shape(int shapeno)
    {
        JB2Shape shape;
        if(shapeno >= inheritedShapes) {
            shape = shapes.get(shapeno - inheritedShapes);
        }
        else if (inheritedDict != null) {
            shape = inheritedDict.get_shape(shapeno);
        }
        else {
            throw new DjVuFileException("JB2Image.bad_number");
        }
        return shape;
    }

    public int add_shape(JB2Shape shape) {
        if (shape.getParent() >= get_shape_count()) {
            throw new DjVuFileException("JB2Image.bad_parent_shape");
        }

        int index = shapes.size();
        shapes.add(shape);
        return index + inheritedShapes;
    }

    public int get_shape_count() {
        return inheritedShapes + shapes.size();
    }
}

package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;

import java.util.ArrayList;
import java.util.List;

public class JB2Dict {

    // int inherited_shapes;
    private int inheritedShapes;
    private JB2Dict inheritedDict;

    private List<JB2Shape> shapes;
    private List<LibRect> boxes;

    private List<Integer> shape2lib;
    private List<Integer> lib2shape;
    private List<LibRect> libinfo;


    /*
    // JB2Image.cpp (void JB2Dict::init())
    void JB2Dict::decode(const GP<ByteStream> &gbs, JB2DecoderCallback *cb, void *arg) {
        init();
        JB2Codec::Decode codec;
        codec.init(gbs);
        codec.set_dict_callback(cb,arg);
        codec.code(this);
    }
    */
    public JB2Dict() {
        this.inheritedShapes = 0;
        this.inheritedDict = null;
        this.shapes = new ArrayList<>();
        this.boxes = new ArrayList<>();

        // this.shape2lib = new ArrayList<>();
        // this.lib2shape = new ArrayList<>();
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

    // void JB2Dict::JB2Codec::init_library(JB2Dict &jim)
    public void init_library() {
        int nshape = this.getInheritedShapes(); // .get_inherited_shape_count();

        shape2lib = new ArrayList<>(nshape);
        lib2shape = new ArrayList<>(nshape);

        // shape2lib.resize(0,nshape-1);
        // lib2shape.resize(0,nshape-1);
        // libinfo.resize(0,nshape-1);
        for (int i = 0; i < nshape; i++) {
            shape2lib.add(i); // shape2lib[i] = i;
            lib2shape.add(i); // lib2shape[i] = i;

            this.get_bounding_box(i, libinfo.get(i));
        }
    }

    // List<Integer> lib2shape
    public List<Integer> getLib2shape() {
        return lib2shape;
    }

    // int JB2Dict::JB2Codec::add_library(const int shapeno, JB2Shape &jshp)
    public int add_library(int shapeno, JB2Shape jshp) {
        final int libno = lib2shape.size(); // lib2shape.hbound() + 1;
        // lib2shape.touch(libno);
        // lib2shape[libno] = shapeno;
        lib2shape.add(shapeno);
        // shape2lib.touch(shapeno);
        // shape2lib[shapeno] = libno;
        shape2lib.add(libno);
        // libinfo.touch(libno);
        // final LibRect libRect = libinfo.get(libno);
        final LibRect libRect = new LibRect();
        libRect.compute_bounding_box(jshp.getBits());

        libinfo.add(libRect);
        // libinfo[libno].compute_bounding_box(*(jshp.bits));
        return libno;
    }

    // GTArray<LibRect> boxes;
    // void JB2Dict::get_bounding_box(int shapeno, LibRect &dest) (see JB2Image.cpp)
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
            final JB2Shape jshp = get_shape(shapeno);
            // libRect.compute_bounding_box(jshp.getBits());
        }
        return libRect;
    }

    // LibRect libRect = libinfo.get(match);
    public LibRect get_lib(int index) {
        return libinfo.get(index);
    }

    // JB2Shape &JB2Dict::get_shape(const int shapeno)
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
            // G_THROW( ERR_MSG("JB2Image.bad_number") );
            throw new DjVuFileException("JB2Image.bad_number");
        }
        return shape;
    }

    // int
    //JB2Dict::add_shape(const JB2Shape &shape)
    public int add_shape(JB2Shape shape) {
        if (shape.getParent() >= get_shape_count()) {
            // G_THROW( ERR_MSG("JB2Image.bad_parent_shape") );
            throw new DjVuFileException("JB2Image.bad_parent_shape");
        }

        int index = shapes.size();
        // shapes.touch(index);
        shapes.add(shape);
        // return index + inherited_shapes;
        return index + inheritedShapes; // get_shape_count();
    }

    public int get_shape_count() {
        return inheritedShapes + shapes.size();
    }
}

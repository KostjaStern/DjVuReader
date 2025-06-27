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
}

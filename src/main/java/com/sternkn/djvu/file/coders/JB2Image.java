package com.sternkn.djvu.file.coders;

import com.sternkn.djvu.file.DjVuFileException;

import java.util.ArrayList;
import java.util.List;

import static com.sternkn.djvu.file.utils.NumberUtils.asUnsignedInt;

public class JB2Image implements Dict {

    private int width;
    private int height;
    private JB2Dict dictionary;

    private List<JB2Shape> shapes;
    private List<Integer> shape2lib;
    private List<Integer> lib2shape;
    private List<LibRect> libinfo;
    private List<JB2Blit> blits;

    private boolean reproduce_old_bug;

    public JB2Image() {
        this(null);
    }

    public JB2Image(JB2Dict dict) {
        this.dictionary = dict;

        this.shape2lib = new ArrayList<>();
        this.lib2shape = new ArrayList<>();
        this.libinfo = new ArrayList<>();
        this.shapes = new ArrayList<>();
        this.blits = new ArrayList<>();

        this.reproduce_old_bug = false;
    }

    public JB2Dict getDictionary() {
        return dictionary;
    }

    public void set_dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean isReproduceOldBug() {
        return reproduce_old_bug;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int get_blit_count() {
        return blits.size();
    }

    public JB2Blit get_blit(int index) {
        return blits.get(index);
    }

    public void init_library() {
        int nshape = get_inherited_shape_count();

        shape2lib = new ArrayList<>(nshape);
        lib2shape = new ArrayList<>(nshape);

        for (int i = 0; i < nshape; i++) {
            shape2lib.add(i);
            lib2shape.add(i);

            LibRect libRect = new LibRect();
            libRect = this.dictionary.get_bounding_box(i, libRect);
            libinfo.add(libRect);
        }
    }

    public JB2Shape get_shape(int shapeno) {
        int inheritedShapes = get_inherited_shape_count();

        JB2Shape shape;
        if(shapeno >= inheritedShapes) {
            shape = shapes.get(shapeno - inheritedShapes);
        }
        else if (this.dictionary != null) {
            shape = this.dictionary.get_shape(shapeno);
        }
        else {
            throw new DjVuFileException("JB2Image.bad_number");
        }
        return shape;
    }

    public LibRect get_lib(int index) {
        return libinfo.get(index);
    }

    public int add_shape(JB2Shape shape) {
        if (shape.getParent() >= get_shape_count()) {
            throw new DjVuFileException("JB2Image.bad_parent_shape");
        }

        int index = shapes.size();
        shapes.add(shape);

        return index + get_inherited_shape_count();
    }

    public int add_library(int shapeno, JB2Shape shape) {
        final int libno = lib2shape.size();
        lib2shape.add(shapeno);

        shape2lib.add(libno);

        final LibRect libRect = new LibRect();
        libRect.compute_bounding_box(shape.getBits());

        libinfo.add(libRect);

        return libno;
    }

    public int get_shape_count() {
        return get_inherited_shape_count() + shapes.size();
    }

    public int get_inherited_shape_count() {
        return this.dictionary == null ? 0 : this.dictionary.get_shape_count();
    }

    public int add_blit(JB2Blit blit) {
        if (blit.getShapeno() >= asUnsignedInt(get_shape_count())) {
            throw new DjVuFileException("JB2Image.bad_shape");
        }

        int index = blits.size();
        blits.add(blit);
        return index;
    }

    @Override
    public List<Integer> getLib2shape() {
        return lib2shape;
    }

    public GBitmap get_bitmap() {
        return this.get_bitmap(1, 1);
    }

    public GBitmap get_bitmap(int subsample, int align) {
        if (this.width == 0 || this.height == 0) {
            throw new DjVuFileException("JB2Image.cant_create");
        }

        int swidth = (width + subsample - 1) / subsample;
        int sheight = (height + subsample - 1) / subsample;
        int border = ((swidth + align - 1) & -align) - swidth;


        GBitmap bm = new GBitmap();
        bm.init(sheight, swidth, border);
        bm.set_grays(1 + subsample * subsample);
        for (int blitno = 0; blitno < get_blit_count(); blitno++)
        {
           JB2Blit pblit = get_blit(blitno);
           JB2Shape  pshape = get_shape(pblit.getShapeno());
            GBitmap pshapeBits = pshape.getBits();
            if (pshapeBits != null) {
                bm.blit(pshapeBits, pblit.getLeft(), pblit.getBottom(), subsample);
            }
        }
        return bm;
    }
}

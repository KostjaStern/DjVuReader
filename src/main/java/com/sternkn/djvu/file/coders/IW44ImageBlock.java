package com.sternkn.djvu.file.coders;

/*
    Represents a block of 32x32 coefficients after zigzagging and scaling
 */
public class IW44ImageBlock {

/*
    // creating
    Block(void);
    // accessing scaled coefficients
    short get(int n) const;
    void  set(int n, int val, IW44Image::Map *map);
    // converting from liftblock
    void  read_liftblock(const short *coeff, IW44Image::Map *map);
    void  write_liftblock(short *coeff, int bmin=0, int bmax=64) const;
    // sparse array access
  const short* data(int n) const;
    short* data(int n, IW44Image::Map *map);
    void   zero(int n);
    // sparse representation
    private:
    short **pdata[4];
*/

// https://medium.com/@AlexanderObregon/understanding-multi-dimensional-arrays-in-java-7ead0c3937dd
//    int[][] jaggedArray = new int[3][];
//    jaggedArray[0] = new int[2];  // First row has 2 columns
//    jaggedArray[1] = new int[3];  // Second row has 3 columns
//    jaggedArray[2] = new int[1];  // Third row has 1 column

    private BufferPointer[][] pdata;

    public IW44ImageBlock() {
        pdata = new BufferPointer[4][];
//        for (int i = 0; i < 4; i++) {
//            pdata[i] = 0;
//        }
    }

    public BufferPointer data(int n) {
        if (pdata[n >> 4] == null) {
            return null;
        }

        return pdata[n >> 4][n & 15];
    }

    public BufferPointer data(int n, IW44ImageMap map) {
        if (pdata[n >> 4] == null) {
            pdata[n >> 4] = new BufferPointer[16];  // map.allocp(16);
        }

        if (pdata[n >> 4][n & 15] == null) {
            pdata[n >> 4][n & 15] = map.alloc(16);
        }

        return pdata[n >> 4][n & 15];
    }

    public static void main(String[] args) {
        System.out.println("Hello ... ");
    }
}

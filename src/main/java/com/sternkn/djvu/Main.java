package com.sternkn.djvu;

import com.sternkn.djvu.file.DjVuFile;

import java.io.File;


public class Main {

    public static void main(String ... args) {
        System.out.println("Run ...");
        File file = new File("./test_files/Abert_Mozart_book__1.djvu");
        System.out.println("djvuFile.exists() = " + file.exists());
        System.out.println("djvuFile.length() = " + file.length());

        DjVuFile djvuFile = new DjVuFile(file);
    }
}

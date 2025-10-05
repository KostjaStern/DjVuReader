package com.sternkn.djvu.file.chunks;

import com.sternkn.djvu.file.coders.TestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLTAnnotationChunk extends TestSupport {

    @Test
    public void testLTAnnotationChunk() {
        Chunk chunk = readChunk("LTAz_12.data", ChunkId.LTAz);

        LTAnnotationChunk ltAnnotationChunk = new LTAnnotationChunk(chunk);

        String actualData = ltAnnotationChunk.getPlainText();
        String expectedData = "LTANNBEGIN\r\n" +
                "BEGINOBJECT\r\n0\r\n" +
                "VISIBLE\r\n1\r\n" +
                "TAG\r\n0\r\n" +
                "SCALARX\r\n0.33314253\r\n" +
                "SCALARY\r\n0.333136444\r\n" +
                "OFFSETX\r\n278\r\n" +
                "OFFSETY\r\n140\r\n" +
                "FORECOLOR\r\n0\r\n" +
                "BACKCOLOR\r\n16777215\r\n" +
                "FONTSIZE\r\n16.6666667\r\n" +
                "FONTBOLD\r\n0\r\n" +
                "FONTITALIC\r\n0\r\n" +
                "FONTSTRIKETHROUGH\r\n0\r\n" +
                "FONTUNDERLINE\r\n0\r\n" +
                "LINEWIDTH\r\n0\r\n" +
                "LINESTYLE\r\n5\r\n" +
                "FILLPATTERN\r\n0\r\n" +
                "FILLMODE\r\n0\r\n" +
                "GROUPING\r\n0\r\n" +
                "POINTS\r\n3\r\n0\r\n0\r\n0\r\n1693\r\n1747\r\n1693\r\n" +
                "ENDOBJECT\r\n" +
                "BLOCKBEGIN\r\n" +
                "BEGINOBJECT\r\n1\r\n" +
                "VISIBLE\r\n1\r\n" +
                "TAG\r\n0\r\n" +
                "SCALARX\r\n1\r\n" +
                "SCALARY\r\n1\r\n" +
                "OFFSETX\r\n0\r\n" +
                "OFFSETY\r\n0\r\n" +
                "HYPERLINK\r\n4\r\n0\r\n0\r\n" +
                "HYPERLINKTEXT\r\n4\r\n(S<P.0\r\n" +
                "FORECOLOR\r\n0\r\n" +
                "BACKCOLOR\r\n16777215\r\n" +
                "FONTSIZE\r\n34.1317391\r\n" +
                "FONTBOLD\r\n0\r\n" +
                "FONTITALIC\r\n0\r\n" +
                "FONTSTRIKETHROUGH\r\n0\r\n" +
                "FONTUNDERLINE\r\n0\r\n" +
                "FONTNAME\r\n5\r\n07)I86P\r\n" +
                "LINEWIDTH\r\n3.38608523\r\n" +
                "LINESTYLE\r\n4\r\n" +
                "FILLMODE\r\n1\r\n" +
                "ROP2\r\n1\r\n" +
                "SHOWNAME\r\n1\r\n" +
                "NAMEOFFSET\r\n0\r\n0\r\n" +
                "NAMEAUTOADJUST\r\n1\r\n" +
                "POINTS\r\n2\r\n1698.97251\r\n1617.95567\r\n1698.97251\r\n1659.9805\r\n" +
                "ENDOBJECT\r\n" +
                "BLOCKEND\r\n" +
                "LTANNEND\r\n";
        assertEquals(expectedData, actualData);
    }
}

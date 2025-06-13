package com.sternkn.djvu.file.coders;

/*
   p -  the amount by which the current arithmetic coding interval is reduced if the decoded symbol is the MPS.
   m -  the threshold above which an MPS triggers a probability state update.
   up - the next probability state index for context k after an MPS triggers a probability state index update.
        An LPS always triggers a probability state index update.
   dn - the next probability state index for context k after an LPS.
 */
public record ZpCodecTable(int p, int m, int up, int dn) {

}

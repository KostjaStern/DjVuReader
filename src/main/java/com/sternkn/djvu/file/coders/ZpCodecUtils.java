package com.sternkn.djvu.file.coders;

public final class ZpCodecUtils {

    /** Context variable.
     Variables of type #BitContext# hold a single byte describing how to encode
     or decode message bits with similar statistical properties.  This single
     byte simultaneously represents the current estimate of the bit probability
     distribution (which is determined by the frequencies of #1#s and #0#s
     already coded with this context) and the confidence in this estimate
     (which determines how fast the estimate can change.)

     A coding program typically allocates hundreds of context variables.  Each
     coding context is initialized to zero before encoding or decoding.  Value
     zero represents equal probabilities for #1#s and #0#s with a minimal
     confidence and therefore a maximum adaptation speed.  Each message bit is
     encoded using a coding context determined as a function of previously
     encoded message bits.  The decoder therefore can examine the previously
     decoded message bits and decode the current bit using the same context as
     the encoder.  This is critical for proper decoding.
     */
    // see ZPCodec.h
    // typedef unsigned char  BitContext;


    private ZpCodecUtils() {
    }

    public static ZpCodecTable[] getDefaultTable() {
        ZpCodecTable[] table = new ZpCodecTable[256];

        // #ifdef ZPCODER
        table[0] = new ZpCodecTable(0x8000,  0x0000,  84, 145);
        table[1] = new ZpCodecTable(0x8000,  0x0000,   3,   4);
        table[2] = new ZpCodecTable(0x8000,  0x0000,   4,   3);
        table[3] = new ZpCodecTable(0x6bbd,  0x10a5,   5,   1);
        table[4] = new ZpCodecTable(0x6bbd,  0x10a5,   6,   2);
        table[5] = new ZpCodecTable(0x5d45,  0x1f28,   7,   3);
        table[6] = new ZpCodecTable(0x5d45,  0x1f28,   8,   4);
        table[7] = new ZpCodecTable(0x51b9,  0x2bd3,   9,   5);
        table[8] = new ZpCodecTable(0x51b9,  0x2bd3,  10,   6);
        table[9] = new ZpCodecTable(0x4813,  0x36e3,  11,   7);

        table[10] = new ZpCodecTable(0x4813,  0x36e3,  12,   8);
        table[11] = new ZpCodecTable(0x3fd5,  0x408c,  13,   9);
        table[12] = new ZpCodecTable(0x3fd5,  0x408c,  14,  10);
        table[13] = new ZpCodecTable(0x38b1,  0x48fd,  15,  11);
        table[14] = new ZpCodecTable(0x38b1,  0x48fd,  16,  12);
        table[15] = new ZpCodecTable(0x3275,  0x505d,  17,  13);
        table[16] = new ZpCodecTable(0x3275,  0x505d,  18,  14);
        table[17] = new ZpCodecTable(0x2cfd,  0x56d0,  19,  15);
        table[18] = new ZpCodecTable(0x2cfd,  0x56d0,  20,  16);
        table[19] = new ZpCodecTable(0x2825,  0x5c71,  21,  17);

        table[20] = new ZpCodecTable(0x2825,  0x5c71,  22,  18);
        table[21] = new ZpCodecTable(0x23ab,  0x615b,  23,  19);
        table[22] = new ZpCodecTable(0x23ab,  0x615b,  24,  20);
        table[23] = new ZpCodecTable(0x1f87,  0x65a5,  25,  21);
        table[24] = new ZpCodecTable(0x1f87,  0x65a5,  26,  22);
        table[25] = new ZpCodecTable(0x1bbb,  0x6962,  27,  23);
        table[26] = new ZpCodecTable(0x1bbb,  0x6962,  28,  24);
        table[27] = new ZpCodecTable(0x1845,  0x6ca2,  29,  25);
        table[28] = new ZpCodecTable(0x1845,  0x6ca2,  30,  26);
        table[29] = new ZpCodecTable(0x1523,  0x6f74,  31,  27);

        table[30] = new ZpCodecTable(0x1523,  0x6f74,  32,  28);
        table[31] = new ZpCodecTable(0x1253,  0x71e6,  33,  29);
        table[32] = new ZpCodecTable(0x1253,  0x71e6,  34,  30);
        table[33] = new ZpCodecTable(0x0fcf,  0x7404,  35,  31);
        table[34] = new ZpCodecTable(0x0fcf,  0x7404,  36,  32);
        table[35] = new ZpCodecTable(0x0d95,  0x75d6,  37,  33);
        table[36] = new ZpCodecTable(0x0d95,  0x75d6,  38,  34);
        table[37] = new ZpCodecTable(0x0b9d,  0x7768,  39,  35);
        table[38] = new ZpCodecTable(0x0b9d,  0x7768,  40,  36);
        table[39] = new ZpCodecTable(0x09e3,  0x78c2,  41,  37);

        table[40] = new ZpCodecTable(0x09e3,  0x78c2,  42,  38);
        table[41] = new ZpCodecTable(0x0861,  0x79ea,  43,  39);
        table[42] = new ZpCodecTable(0x0861,  0x79ea,  44,  40);
        table[43] = new ZpCodecTable(0x0711,  0x7ae7,  45,  41);
        table[44] = new ZpCodecTable(0x0711,  0x7ae7,  46,  42);
        table[45] = new ZpCodecTable(0x05f1,  0x7bbe,  47,  43);
        table[46] = new ZpCodecTable(0x05f1,  0x7bbe,  48,  44);
        table[47] = new ZpCodecTable(0x04f9,  0x7c75,  49,  45);
        table[48] = new ZpCodecTable(0x04f9,  0x7c75,  50,  46);
        table[49] = new ZpCodecTable(0x0425,  0x7d0f,  51,  47);

        table[50] = new ZpCodecTable(0x0425,  0x7d0f,  52,  48);
        table[51] = new ZpCodecTable(0x0371,  0x7d91,  53,  49);
        table[52] = new ZpCodecTable(0x0371,  0x7d91,  54,  50);
        table[53] = new ZpCodecTable(0x02d9,  0x7dfe,  55,  51);
        table[54] = new ZpCodecTable(0x02d9,  0x7dfe,  56,  52);
        table[55] = new ZpCodecTable(0x0259,  0x7e5a,  57,  53);
        table[56] = new ZpCodecTable(0x0259,  0x7e5a,  58,  54);
        table[57] = new ZpCodecTable(0x01ed,  0x7ea6,  59,  55);
        table[58] = new ZpCodecTable(0x01ed,  0x7ea6,  60,  56);
        table[59] = new ZpCodecTable(0x0193,  0x7ee6,  61,  57);

        table[60] = new ZpCodecTable(0x0193,  0x7ee6,  62,  58);
        table[61] = new ZpCodecTable(0x0149,  0x7f1a,  63,  59);
        table[62] = new ZpCodecTable(0x0149,  0x7f1a,  64,  60);
        table[63] = new ZpCodecTable(0x010b,  0x7f45,  65,  61);
        table[64] = new ZpCodecTable(0x010b,  0x7f45,  66,  62);
        table[65] = new ZpCodecTable(0x00d5,  0x7f6b,  67,  63);
        table[66] = new ZpCodecTable(0x00d5,  0x7f6b,  68,  64);
        table[67] = new ZpCodecTable(0x00a5,  0x7f8d,  69,  65);
        table[68] = new ZpCodecTable(0x00a5,  0x7f8d,  70,  66);
        table[69] = new ZpCodecTable(0x007b,  0x7faa,  71,  67);

        table[70] = new ZpCodecTable(0x007b,  0x7faa,  72,  68);
        table[71] = new ZpCodecTable(0x0057,  0x7fc3,  73,  69);
        table[72] = new ZpCodecTable(0x0057,  0x7fc3,  74,  70);
        table[73] = new ZpCodecTable(0x003b,  0x7fd7,  75,  71);
        table[74] = new ZpCodecTable(0x003b,  0x7fd7,  76,  72);
        table[75] = new ZpCodecTable(0x0023,  0x7fe7,  77,  73);
        table[76] = new ZpCodecTable(0x0023,  0x7fe7,  78,  74);
        table[77] = new ZpCodecTable(0x0013,  0x7ff2,  79,  75);
        table[78] = new ZpCodecTable(0x0013,  0x7ff2,  80,  76);
        table[79] = new ZpCodecTable(0x0007,  0x7ffa,  81,  77);

        table[80] = new ZpCodecTable(0x0007,  0x7ffa,  82,  78);
        table[81] = new ZpCodecTable(0x0001,  0x7fff,  81,  79);
        table[82] = new ZpCodecTable(0x0001,  0x7fff,  82,  80);
        table[83] = new ZpCodecTable(0x5695,  0x0000,   9,  85);
        table[84] = new ZpCodecTable(0x24ee,  0x0000,  86, 226);
        table[85] = new ZpCodecTable(0x8000,  0x0000,   5,   6);
        table[86] = new ZpCodecTable(0x0d30,  0x0000,  88, 176);
        table[87] = new ZpCodecTable(0x481a,  0x0000,  89, 143);
        table[88] = new ZpCodecTable(0x0481,  0x0000,  90, 138);
        table[89] = new ZpCodecTable(0x3579,  0x0000,  91, 141);

        table[90] = new ZpCodecTable(0x017a,  0x0000,  92, 112);
        table[91] = new ZpCodecTable(0x24ef,  0x0000,  93, 135);
        table[92] = new ZpCodecTable(0x007b,  0x0000,  94, 104);
        table[93] = new ZpCodecTable(0x1978,  0x0000,  95, 133);
        table[94] = new ZpCodecTable(0x0028,  0x0000,  96, 100);
        table[95] = new ZpCodecTable(0x10ca,  0x0000,  97, 129);
        table[96] = new ZpCodecTable(0x000d,  0x0000,  82,  98);
        table[97] = new ZpCodecTable(0x0b5d,  0x0000,  99, 127);
        table[98] = new ZpCodecTable(0x0034,  0x0000,  76,  72);
        table[99] = new ZpCodecTable(0x078a,  0x0000, 101, 125);

        table[100] = new ZpCodecTable(0x00a0,  0x0000,  70, 102);
        table[101] = new ZpCodecTable(0x050f,  0x0000, 103, 123);
        table[102] = new ZpCodecTable(0x0117,  0x0000,  66,  60);
        table[103] = new ZpCodecTable(0x0358,  0x0000, 105, 121);
        table[104] = new ZpCodecTable(0x01ea,  0x0000, 106, 110);
        table[105] = new ZpCodecTable(0x0234,  0x0000, 107, 119);
        table[106] = new ZpCodecTable(0x0144,  0x0000,  66, 108);
        table[107] = new ZpCodecTable(0x0173,  0x0000, 109, 117);
        table[108] = new ZpCodecTable(0x0234,  0x0000,  60,  54);
        table[109] = new ZpCodecTable(0x00f5,  0x0000, 111, 115);

        table[110] = new ZpCodecTable(0x0353,  0x0000,  56,  48);
        table[111] = new ZpCodecTable(0x00a1,  0x0000,  69, 113);
        table[112] = new ZpCodecTable(0x05c5,  0x0000, 114, 134);
        table[113] = new ZpCodecTable(0x011a,  0x0000,  65,  59);
        table[114] = new ZpCodecTable(0x03cf,  0x0000, 116, 132);
        table[115] = new ZpCodecTable(0x01aa,  0x0000,  61,  55);
        table[116] = new ZpCodecTable(0x0285,  0x0000, 118, 130);
        table[117] = new ZpCodecTable(0x0286,  0x0000,  57,  51);
        table[118] = new ZpCodecTable(0x01ab,  0x0000, 120, 128);
        table[119] = new ZpCodecTable(0x03d3,  0x0000,  53,  47);

        table[120] = new ZpCodecTable(0x011a,  0x0000, 122, 126);
        table[121] = new ZpCodecTable(0x05c5,  0x0000,  49,  41);
        table[122] = new ZpCodecTable(0x00ba,  0x0000, 124,  62);
        table[123] = new ZpCodecTable(0x08ad,  0x0000,  43,  37);
        table[124] = new ZpCodecTable(0x007a,  0x0000,  72,  66);
        table[125] = new ZpCodecTable(0x0ccc,  0x0000,  39,  31);
        table[126] = new ZpCodecTable(0x01eb,  0x0000,  60,  54);
        table[127] = new ZpCodecTable(0x1302,  0x0000,  33,  25);
        table[128] = new ZpCodecTable(0x02e6,  0x0000,  56,  50);
        table[129] = new ZpCodecTable(0x1b81,  0x0000,  29, 131);

        table[130] = new ZpCodecTable(0x045e,  0x0000,  52,  46);
        table[131] = new ZpCodecTable(0x24ef,  0x0000,  23,  17);
        table[132] = new ZpCodecTable(0x0690,  0x0000,  48,  40);
        table[133] = new ZpCodecTable(0x2865,  0x0000,  23,  15);
        table[134] = new ZpCodecTable(0x09de,  0x0000,  42, 136);
        table[135] = new ZpCodecTable(0x3987,  0x0000, 137,   7);
        table[136] = new ZpCodecTable(0x0dc8,  0x0000,  38,  32);
        table[137] = new ZpCodecTable(0x2c99,  0x0000,  21, 139);
        table[138] = new ZpCodecTable(0x10ca,  0x0000, 140, 172);
        table[139] = new ZpCodecTable(0x3b5f,  0x0000,  15,   9);

        table[140] = new ZpCodecTable(0x0b5d,  0x0000, 142, 170);
        table[141] = new ZpCodecTable(0x5695,  0x0000,   9,  85);
        table[142] = new ZpCodecTable(0x078a,  0x0000, 144, 168);
        table[143] = new ZpCodecTable(0x8000,  0x0000, 141, 248);
        table[144] = new ZpCodecTable(0x050f,  0x0000, 146, 166);
        table[145] = new ZpCodecTable(0x24ee,  0x0000, 147, 247);
        table[146] = new ZpCodecTable(0x0358,  0x0000, 148, 164);
        table[147] = new ZpCodecTable(0x0d30,  0x0000, 149, 197);
        table[148] = new ZpCodecTable(0x0234,  0x0000, 150, 162);
        table[149] = new ZpCodecTable(0x0481,  0x0000, 151,  95);

        table[150] = new ZpCodecTable(0x0173,  0x0000, 152, 160);
        table[151] = new ZpCodecTable(0x017a,  0x0000, 153, 173);
        table[152] = new ZpCodecTable(0x00f5,  0x0000, 154, 158);
        table[153] = new ZpCodecTable(0x007b,  0x0000, 155, 165);
        table[154] = new ZpCodecTable(0x00a1,  0x0000,  70, 156);
        table[155] = new ZpCodecTable(0x0028,  0x0000, 157, 161);
        table[156] = new ZpCodecTable(0x011a,  0x0000,  66,  60);
        table[157] = new ZpCodecTable(0x000d,  0x0000,  81, 159);
        table[158] = new ZpCodecTable(0x01aa,  0x0000,  62,  56);
        table[159] = new ZpCodecTable(0x0034,  0x0000,  75,  71);

        table[160] = new ZpCodecTable(0x0286,  0x0000,  58,  52);
        table[161] = new ZpCodecTable(0x00a0,  0x0000,  69, 163);
        table[162] = new ZpCodecTable(0x03d3,  0x0000,  54,  48);
        table[163] = new ZpCodecTable(0x0117,  0x0000,  65,  59);
        table[164] = new ZpCodecTable(0x05c5,  0x0000,  50,  42);
        table[165] = new ZpCodecTable(0x01ea,  0x0000, 167, 171);
        table[166] = new ZpCodecTable(0x08ad,  0x0000,  44,  38);
        table[167] = new ZpCodecTable(0x0144,  0x0000,  65, 169);
        table[168] = new ZpCodecTable(0x0ccc,  0x0000,  40,  32);
        table[169] = new ZpCodecTable(0x0234,  0x0000,  59,  53);

        table[170] = new ZpCodecTable(0x1302,  0x0000,  34,  26);
        table[171] = new ZpCodecTable(0x0353,  0x0000,  55,  47);
        table[172] = new ZpCodecTable(0x1b81,  0x0000,  30, 174);
        table[173] = new ZpCodecTable(0x05c5,  0x0000, 175, 193);
        table[174] = new ZpCodecTable(0x24ef,  0x0000,  24,  18);
        table[175] = new ZpCodecTable(0x03cf,  0x0000, 177, 191);
        table[176] = new ZpCodecTable(0x2b74,  0x0000, 178, 222);
        table[177] = new ZpCodecTable(0x0285,  0x0000, 179, 189);
        table[178] = new ZpCodecTable(0x201d,  0x0000, 180, 218);
        table[179] = new ZpCodecTable(0x01ab,  0x0000, 181, 187);

        table[180] = new ZpCodecTable(0x1715,  0x0000, 182, 216);
        table[181] = new ZpCodecTable(0x011a,  0x0000, 183, 185);
        table[182] = new ZpCodecTable(0x0fb7,  0x0000, 184, 214);
        table[183] = new ZpCodecTable(0x00ba,  0x0000,  69,  61);
        table[184] = new ZpCodecTable(0x0a67,  0x0000, 186, 212);
        table[185] = new ZpCodecTable(0x01eb,  0x0000,  59,  53);
        table[186] = new ZpCodecTable(0x06e7,  0x0000, 188, 210);
        table[187] = new ZpCodecTable(0x02e6,  0x0000,  55,  49);
        table[188] = new ZpCodecTable(0x0496,  0x0000, 190, 208);
        table[189] = new ZpCodecTable(0x045e,  0x0000,  51,  45);

        table[190] = new ZpCodecTable(0x030d,  0x0000, 192, 206);
        table[191] = new ZpCodecTable(0x0690,  0x0000,  47,  39);
        table[192] = new ZpCodecTable(0x0206,  0x0000, 194, 204);
        table[193] = new ZpCodecTable(0x09de,  0x0000,  41, 195);
        table[194] = new ZpCodecTable(0x0155,  0x0000, 196, 202);
        table[195] = new ZpCodecTable(0x0dc8,  0x0000,  37,  31);
        table[196] = new ZpCodecTable(0x00e1,  0x0000, 198, 200);
        table[197] = new ZpCodecTable(0x2b74,  0x0000, 199, 243);
        table[198] = new ZpCodecTable(0x0094,  0x0000,  72,  64);
        table[199] = new ZpCodecTable(0x201d,  0x0000, 201, 239);

        table[200] = new ZpCodecTable(0x0188,  0x0000,  62,  56);
        table[201] = new ZpCodecTable(0x1715,  0x0000, 203, 237);
        table[202] = new ZpCodecTable(0x0252,  0x0000,  58,  52);
        table[203] = new ZpCodecTable(0x0fb7,  0x0000, 205, 235);
        table[204] = new ZpCodecTable(0x0383,  0x0000,  54,  48);
        table[205] = new ZpCodecTable(0x0a67,  0x0000, 207, 233);
        table[206] = new ZpCodecTable(0x0547,  0x0000,  50,  44);
        table[207] = new ZpCodecTable(0x06e7,  0x0000, 209, 231);
        table[208] = new ZpCodecTable(0x07e2,  0x0000,  46,  38);
        table[209] = new ZpCodecTable(0x0496,  0x0000, 211, 229);

        table[210] = new ZpCodecTable(0x0bc0,  0x0000,  40,  34);
        table[211] = new ZpCodecTable(0x030d,  0x0000, 213, 227);
        table[212] = new ZpCodecTable(0x1178,  0x0000,  36,  28);
        table[213] = new ZpCodecTable(0x0206,  0x0000, 215, 225);
        table[214] = new ZpCodecTable(0x19da,  0x0000,  30,  22);
        table[215] = new ZpCodecTable(0x0155,  0x0000, 217, 223);
        table[216] = new ZpCodecTable(0x24ef,  0x0000,  26,  16);
        table[217] = new ZpCodecTable(0x00e1,  0x0000, 219, 221);
        table[218] = new ZpCodecTable(0x320e,  0x0000,  20, 220);
        table[219] = new ZpCodecTable(0x0094,  0x0000,  71,  63);

        table[220] = new ZpCodecTable(0x432a,  0x0000,  14,   8);
        table[221] = new ZpCodecTable(0x0188,  0x0000,  61,  55);
        table[222] = new ZpCodecTable(0x447d,  0x0000,  14, 224);
        table[223] = new ZpCodecTable(0x0252,  0x0000,  57,  51);
        table[224] = new ZpCodecTable(0x5ece,  0x0000,   8,   2);
        table[225] = new ZpCodecTable(0x0383,  0x0000,  53,  47);
        table[226] = new ZpCodecTable(0x8000,  0x0000, 228,  87);
        table[227] = new ZpCodecTable(0x0547,  0x0000,  49,  43);
        table[228] = new ZpCodecTable(0x481a,  0x0000, 230, 246);
        table[229] = new ZpCodecTable(0x07e2,  0x0000,  45,  37);

        table[230] = new ZpCodecTable(0x3579,  0x0000, 232, 244);
        table[231] = new ZpCodecTable(0x0bc0,  0x0000,  39,  33);
        table[232] = new ZpCodecTable(0x24ef,  0x0000, 234, 238);
        table[233] = new ZpCodecTable(0x1178,  0x0000,  35,  27);
        table[234] = new ZpCodecTable(0x1978,  0x0000, 138, 236);
        table[235] = new ZpCodecTable(0x19da,  0x0000,  29,  21);
        table[236] = new ZpCodecTable(0x2865,  0x0000,  24,  16);
        table[237] = new ZpCodecTable(0x24ef,  0x0000,  25,  15);
        table[238] = new ZpCodecTable(0x3987,  0x0000, 240,   8);
        table[239] = new ZpCodecTable(0x320e,  0x0000,  19, 241);

        table[240] = new ZpCodecTable(0x2c99,  0x0000,  22, 242);
        table[241] = new ZpCodecTable(0x432a,  0x0000,  13,   7);
        table[242] = new ZpCodecTable(0x3b5f,  0x0000,  16,  10);
        table[243] = new ZpCodecTable(0x447d,  0x0000,  13, 245);
        table[244] = new ZpCodecTable(0x5695,  0x0000,  10,   2);
        table[245] = new ZpCodecTable(0x5ece,  0x0000,   7,   1);
        table[246] = new ZpCodecTable(0x8000,  0x0000, 244,  83);
        table[247] = new ZpCodecTable(0x8000,  0x0000, 249, 250);
        table[248] = new ZpCodecTable(0x5695,  0x0000,  10,   2);
        table[249] = new ZpCodecTable(0x481a,  0x0000,  89, 143);

        table[250] = new ZpCodecTable(0x481a,  0x0000, 230, 246);
        table[251] = new ZpCodecTable(0x0000,  0x0000, 0, 0);
        table[252] = new ZpCodecTable(0x0000,  0x0000, 0, 0);
        table[253] = new ZpCodecTable(0x0000,  0x0000, 0, 0);
        table[254] = new ZpCodecTable(0x0000,  0x0000, 0, 0);
        table[255] = new ZpCodecTable(0x0000,  0x0000, 0, 0);

        return table;
    }
}

package com.sternkn.djvu.file.coders;

public final class ZpCodecUtils {

    private ZpCodecUtils() {
    }

    // Create machine independent ffz table
    public static byte[] getFFZTable() {
        final byte[] ffzt = new byte[256];

        for (int index = 0; index < ffzt.length; index++) {
            ffzt[index] = 0;

            for (int j = index; (j & 0x80) != 0; j = j << 1) {
                ffzt[index] += 1;
            }
        }

        return ffzt;
    }

    public static ZpCodecTable[] getDefaultTable() {
        ZpCodecTable[] table = new ZpCodecTable[256];

        // #ifdef ZPCODER
        table[0] = new ZpCodecTable(32768,  0,  84, 145); //p: 0x8000, m: 0x0000
        table[1] = new ZpCodecTable(32768,  0,   3,   4); //p: 0x8000, m: 0x0000
        table[2] = new ZpCodecTable(32768,  0,   4,   3); //p: 0x8000, m: 0x0000
        table[3] = new ZpCodecTable(27581,  4261,   5,   1); //p: 0x6bbd, m: 0x10a5
        table[4] = new ZpCodecTable(27581,  4261,   6,   2); //p: 0x6bbd, m: 0x10a5
        table[5] = new ZpCodecTable(23877,  7976,   7,   3); //p: 0x5d45, m: 0x1f28
        table[6] = new ZpCodecTable(23877,  7976,   8,   4); //p: 0x5d45, m: 0x1f28
        table[7] = new ZpCodecTable(20921,  11219,   9,   5); //p: 0x51b9, m: 0x2bd3
        table[8] = new ZpCodecTable(20921,  11219,  10,   6); //p: 0x51b9, m: 0x2bd3
        table[9] = new ZpCodecTable(18451,  14051,  11,   7); //p: 0x4813, m: 0x36e3

        table[10] = new ZpCodecTable(18451,  14051,  12,   8); //p: 0x4813, m: 0x36e3
        table[11] = new ZpCodecTable(16341,  16524,  13,   9); // 0x3fd5,  0x408c
        table[12] = new ZpCodecTable(16341,  16524,  14,  10); // 0x3fd5,  0x408c
        table[13] = new ZpCodecTable(14513,  18685,  15,  11); // 0x38b1,  0x48fd
        table[14] = new ZpCodecTable(14513,  18685,  16,  12); // 0x38b1,  0x48fd
        table[15] = new ZpCodecTable(12917,  20573,  17,  13); // 0x3275,  0x505d
        table[16] = new ZpCodecTable(12917,  20573,  18,  14); // 0x3275,  0x505d
        table[17] = new ZpCodecTable(11517,  22224,  19,  15); // 0x2cfd,  0x56d0
        table[18] = new ZpCodecTable(11517,  22224,  20,  16); // 0x2cfd,  0x56d0
        table[19] = new ZpCodecTable(10277,  23665,  21,  17); // 0x2825,  0x5c71

        table[20] = new ZpCodecTable(10277,  23665,  22,  18); // 0x2825,  0x5c71
        table[21] = new ZpCodecTable(9131,  24923,  23,  19); // 0x23ab,  0x615b
        table[22] = new ZpCodecTable(9131,  24923,  24,  20); // 0x23ab,  0x615b
        table[23] = new ZpCodecTable(8071,  26021,  25,  21); // 0x1f87,  0x65a5
        table[24] = new ZpCodecTable(8071,  26021,  26,  22); // 0x1f87,  0x65a5
        table[25] = new ZpCodecTable(7099,  26978,  27,  23); // 0x1bbb,  0x6962
        table[26] = new ZpCodecTable(7099,  26978,  28,  24); // 0x1bbb,  0x6962
        table[27] = new ZpCodecTable(6213,  27810,  29,  25); // 0x1845,  0x6ca2
        table[28] = new ZpCodecTable(6213,  27810,  30,  26); // 0x1845,  0x6ca2
        table[29] = new ZpCodecTable(5411,  28532,  31,  27); // 0x1523,  0x6f74

        table[30] = new ZpCodecTable(5411,  28532,  32,  28); // 0x1523,  0x6f74
        table[31] = new ZpCodecTable(4691,  29158,  33,  29); // 0x1253,  0x71e6
        table[32] = new ZpCodecTable(4691,  29158,  34,  30); // 0x1253,  0x71e6
        table[33] = new ZpCodecTable(4047,  29700,  35,  31); // 0x0fcf,  0x7404
        table[34] = new ZpCodecTable(4047,  29700,  36,  32); // 0x0fcf,  0x7404
        table[35] = new ZpCodecTable(3477,  30166,  37,  33); // 0x0d95,  0x75d6
        table[36] = new ZpCodecTable(3477,  30166,  38,  34); // 0x0d95,  0x75d6
        table[37] = new ZpCodecTable(2973,  30568,  39,  35); // 0x0b9d,  0x7768
        table[38] = new ZpCodecTable(2973,  30568,  40,  36); // 0x0b9d,  0x7768
        table[39] = new ZpCodecTable(2531,  30914,  41,  37); // 0x09e3,  0x78c2

        table[40] = new ZpCodecTable(2531,  30914,  42,  38); // 0x09e3,  0x78c2
        table[41] = new ZpCodecTable(2145,  31210,  43,  39); // 0x0861,  0x79ea
        table[42] = new ZpCodecTable(2145,  31210,  44,  40); // 0x0861,  0x79ea
        table[43] = new ZpCodecTable(1809,  31463,  45,  41); // 0x0711,  0x7ae7
        table[44] = new ZpCodecTable(1809,  31463,  46,  42); // 0x0711,  0x7ae7
        table[45] = new ZpCodecTable(1521,  31678,  47,  43); // 0x05f1,  0x7bbe
        table[46] = new ZpCodecTable(1521,  31678,  48,  44); // 0x05f1,  0x7bbe
        table[47] = new ZpCodecTable(1273,  31861,  49,  45); // 0x04f9,  0x7c75
        table[48] = new ZpCodecTable(1273,  31861,  50,  46); // 0x04f9,  0x7c75
        table[49] = new ZpCodecTable(1061,  32015,  51,  47); // 0x0425,  0x7d0f

        table[50] = new ZpCodecTable(1061,  32015,  52,  48); // 0x0425,  0x7d0f
        table[51] = new ZpCodecTable(881,  32145,  53,  49); // 0x0371,  0x7d91
        table[52] = new ZpCodecTable(881,  32145,  54,  50); // 0x0371,  0x7d91
        table[53] = new ZpCodecTable(729,  32254,  55,  51); // 0x02d9,  0x7dfe
        table[54] = new ZpCodecTable(729,  32254,  56,  52); // 0x02d9,  0x7dfe
        table[55] = new ZpCodecTable(601,  32346,  57,  53); // 0x0259,  0x7e5a
        table[56] = new ZpCodecTable(601,  32346,  58,  54); // 0x0259,  0x7e5a
        table[57] = new ZpCodecTable(493,  32422,  59,  55); // 0x01ed,  0x7ea6
        table[58] = new ZpCodecTable(493,  32422,  60,  56); // 0x01ed,  0x7ea6
        table[59] = new ZpCodecTable(403,  32486,  61,  57); // 0x0193,  0x7ee6

        table[60] = new ZpCodecTable(403,  32486,  62,  58); // 0x0193,  0x7ee6
        table[61] = new ZpCodecTable(329,  32538,  63,  59); // 0x0149,  0x7f1a
        table[62] = new ZpCodecTable(329,  32538,  64,  60); // 0x0149,  0x7f1a
        table[63] = new ZpCodecTable(267,  32581,  65,  61); // 0x010b,  0x7f45
        table[64] = new ZpCodecTable(267,  32581,  66,  62); // 0x010b,  0x7f45
        table[65] = new ZpCodecTable(213,  32619,  67,  63); // 0x00d5,  0x7f6b
        table[66] = new ZpCodecTable(213,  32619,  68,  64); // 0x00d5,  0x7f6b
        table[67] = new ZpCodecTable(165,  32653,  69,  65); // 0x00a5,  0x7f8d
        table[68] = new ZpCodecTable(165,  32653,  70,  66); // 0x00a5,  0x7f8d
        table[69] = new ZpCodecTable(123,  32682,  71,  67); // 0x007b,  0x7faa

        table[70] = new ZpCodecTable(123,  32682,  72,  68); // 0x007b,  0x7faa
        table[71] = new ZpCodecTable(87,  32707,  73,  69); // 0x0057,  0x7fc3
        table[72] = new ZpCodecTable(87,  32707,  74,  70); // 0x0057,  0x7fc3
        table[73] = new ZpCodecTable(59,  32727,  75,  71); // 0x003b,  0x7fd7
        table[74] = new ZpCodecTable(59,  32727,  76,  72); // 0x003b,  0x7fd7
        table[75] = new ZpCodecTable(35,  32743,  77,  73); // 0x0023,  0x7fe7
        table[76] = new ZpCodecTable(35,  32743,  78,  74); // 0x0023,  0x7fe7
        table[77] = new ZpCodecTable(19,  32754,  79,  75); // 0x0013,  0x7ff2
        table[78] = new ZpCodecTable(19,  32754,  80,  76); // 0x0013,  0x7ff2
        table[79] = new ZpCodecTable(7,  32762,  81,  77); // 0x0007,  0x7ffa

        table[80] = new ZpCodecTable(7,  32762,  82,  78); // 0x0007,  0x7ffa
        table[81] = new ZpCodecTable(1,  32767,  81,  79); // 0x0001,  0x7fff
        table[82] = new ZpCodecTable(1,  32767,  82,  80); // 0x0001,  0x7fff
        table[83] = new ZpCodecTable(22165,  0,   9,  85); // 0x5695,  0x0000
        table[84] = new ZpCodecTable(9454,  0,  86, 226); // 0x24ee,  0x0000
        table[85] = new ZpCodecTable(32768,  0,   5,   6); // 0x8000,  0x0000
        table[86] = new ZpCodecTable(3376,  0,  88, 176); // 0x0d30,  0x0000
        table[87] = new ZpCodecTable(18458,  0,  89, 143); // 0x481a,  0x0000
        table[88] = new ZpCodecTable(1153,  0,  90, 138); // 0x0481,  0x0000
        table[89] = new ZpCodecTable(13689,  0,  91, 141); // 0x3579,  0x0000

        table[90] = new ZpCodecTable(378,  0,  92, 112); // 0x017a,  0x0000
        table[91] = new ZpCodecTable(9455,  0,  93, 135); // 0x24ef,  0x0000
        table[92] = new ZpCodecTable(123,  0,  94, 104); // 0x007b,  0x0000
        table[93] = new ZpCodecTable(6520,  0,  95, 133); // 0x1978,  0x0000
        table[94] = new ZpCodecTable(40,  0,  96, 100); // 0x0028,  0x0000
        table[95] = new ZpCodecTable(4298,  0,  97, 129); // 0x10ca,  0x0000
        table[96] = new ZpCodecTable(13,  0,  82,  98); // 0x000d,  0x0000
        table[97] = new ZpCodecTable(2909,  0,  99, 127); // 0x0b5d,  0x0000
        table[98] = new ZpCodecTable(52,  0,  76,  72); // 0x0034,  0x0000
        table[99] = new ZpCodecTable(1930,  0, 101, 125); // 0x078a,  0x0000

        table[100] = new ZpCodecTable(160,  0,  70, 102); // 0x00a0,  0x0000
        table[101] = new ZpCodecTable(1295,  0, 103, 123); // 0x050f,  0x0000
        table[102] = new ZpCodecTable(279,  0,  66,  60); // 0x0117,  0x0000
        table[103] = new ZpCodecTable(856,  0, 105, 121); // 0x0358,  0x0000
        table[104] = new ZpCodecTable(490,  0, 106, 110); // 0x01ea,  0x0000
        table[105] = new ZpCodecTable(564,  0, 107, 119); // 0x0234,  0x0000
        table[106] = new ZpCodecTable(324,  0,  66, 108); // 0x0144,  0x0000
        table[107] = new ZpCodecTable(371,  0, 109, 117); // 0x0173,  0x0000
        table[108] = new ZpCodecTable(564,  0,  60,  54); // 0x0234,  0x0000
        table[109] = new ZpCodecTable(245,  0, 111, 115); // 0x00f5,  0x0000

        table[110] = new ZpCodecTable(851,  0,  56,  48); // 0x0353,  0x0000
        table[111] = new ZpCodecTable(161,  0,  69, 113); // 0x00a1,  0x0000
        table[112] = new ZpCodecTable(1477,  0, 114, 134); // 0x05c5,  0x0000
        table[113] = new ZpCodecTable(282,  0,  65,  59); // 0x011a,  0x0000
        table[114] = new ZpCodecTable(975,  0, 116, 132); // 0x03cf,  0x0000
        table[115] = new ZpCodecTable(426,  0,  61,  55); // 0x01aa,  0x0000
        table[116] = new ZpCodecTable(645,  0, 118, 130); // 0x0285,  0x0000
        table[117] = new ZpCodecTable(646,  0,  57,  51); // 0x0286,  0x0000
        table[118] = new ZpCodecTable(427,  0, 120, 128); // 0x01ab,  0x0000
        table[119] = new ZpCodecTable(979,  0,  53,  47); // 0x03d3,  0x0000

        table[120] = new ZpCodecTable(282,  0, 122, 126); // 0x011a,  0x0000
        table[121] = new ZpCodecTable(1477,  0,  49,  41); // 0x05c5,  0x0000
        table[122] = new ZpCodecTable(186,  0, 124,  62); // 0x00ba,  0x0000
        table[123] = new ZpCodecTable(2221,  0,  43,  37); // 0x08ad,  0x0000
        table[124] = new ZpCodecTable(122,  0,  72,  66); // 0x007a,  0x0000
        table[125] = new ZpCodecTable(3276,  0,  39,  31); // 0x0ccc,  0x0000
        table[126] = new ZpCodecTable(491,  0,  60,  54); // 0x01eb,  0x0000
        table[127] = new ZpCodecTable(4866,  0,  33,  25); // 0x1302,  0x0000
        table[128] = new ZpCodecTable(742,  0,  56,  50); // 0x02e6,  0x0000
        table[129] = new ZpCodecTable(7041,  0,  29, 131); // 0x1b81,  0x0000

        table[130] = new ZpCodecTable(1118,  0,  52,  46); // 0x045e,  0x0000
        table[131] = new ZpCodecTable(9455,  0,  23,  17); // 0x24ef,  0x0000
        table[132] = new ZpCodecTable(1680,  0,  48,  40); // 0x0690,  0x0000
        table[133] = new ZpCodecTable(10341,  0,  23,  15); // 0x2865,  0x0000
        table[134] = new ZpCodecTable(2526,  0,  42, 136); // 0x09de,  0x0000
        table[135] = new ZpCodecTable(14727,  0, 137,   7); // 0x3987,  0x0000
        table[136] = new ZpCodecTable(3528,  0,  38,  32); // 0x0dc8,  0x0000
        table[137] = new ZpCodecTable(11417,  0,  21, 139); // 0x2c99,  0x0000
        table[138] = new ZpCodecTable(4298,  0, 140, 172); // 0x10ca,  0x0000
        table[139] = new ZpCodecTable(15199,  0,  15,   9); // 0x3b5f,  0x0000

        table[140] = new ZpCodecTable(2909,  0, 142, 170); // 0x0b5d,  0x0000
        table[141] = new ZpCodecTable(22165,  0,   9,  85); // 0x5695,  0x0000
        table[142] = new ZpCodecTable(1930,  0, 144, 168); // 0x078a,  0x0000
        table[143] = new ZpCodecTable(32768,  0, 141, 248); // 0x8000,  0x0000
        table[144] = new ZpCodecTable(1295,  0, 146, 166); // 0x050f,  0x0000
        table[145] = new ZpCodecTable(9454,  0, 147, 247); // 0x24ee,  0x0000
        table[146] = new ZpCodecTable(856,  0, 148, 164); // 0x0358,  0x0000
        table[147] = new ZpCodecTable(3376,  0, 149, 197); // 0x0d30,  0x0000
        table[148] = new ZpCodecTable(564,  0, 150, 162); // 0x0234,  0x0000
        table[149] = new ZpCodecTable(1153,  0, 151,  95); // 0x0481,  0x0000

        table[150] = new ZpCodecTable(371,  0, 152, 160); // 0x0173,  0x0000
        table[151] = new ZpCodecTable(378,  0, 153, 173); // 0x017a,  0x0000
        table[152] = new ZpCodecTable(245,  0, 154, 158); // 0x00f5,  0x0000
        table[153] = new ZpCodecTable(123,  0, 155, 165); // 0x007b,  0x0000
        table[154] = new ZpCodecTable(161,  0,  70, 156); // 0x00a1,  0x0000
        table[155] = new ZpCodecTable(40,  0, 157, 161); // 0x0028,  0x0000
        table[156] = new ZpCodecTable(282,  0,  66,  60); // 0x011a,  0x0000
        table[157] = new ZpCodecTable(13,  0,  81, 159); // 0x000d,  0x0000
        table[158] = new ZpCodecTable(426,  0,  62,  56); // 0x01aa,  0x0000
        table[159] = new ZpCodecTable(52,  0,  75,  71); // 0x0034,  0x0000

        table[160] = new ZpCodecTable(646,  0,  58,  52); // 0x0286,  0x0000
        table[161] = new ZpCodecTable(160,  0,  69, 163); // 0x00a0,  0x0000
        table[162] = new ZpCodecTable(979,  0,  54,  48); // 0x03d3,  0x0000
        table[163] = new ZpCodecTable(279,  0,  65,  59); // 0x0117,  0x0000
        table[164] = new ZpCodecTable(1477,  0,  50,  42); // 0x05c5,  0x0000
        table[165] = new ZpCodecTable(490,  0, 167, 171); // 0x01ea,  0x0000
        table[166] = new ZpCodecTable(2221,  0,  44,  38); // 0x08ad,  0x0000
        table[167] = new ZpCodecTable(324,  0,  65, 169); // 0x0144,  0x0000
        table[168] = new ZpCodecTable(3276,  0,  40,  32); // 0x0ccc,  0x0000
        table[169] = new ZpCodecTable(564,  0,  59,  53); // 0x0234,  0x0000

        table[170] = new ZpCodecTable(4866,  0,  34,  26); // 0x1302,  0x0000
        table[171] = new ZpCodecTable(851,  0,  55,  47); // 0x0353,  0x0000
        table[172] = new ZpCodecTable(7041,  0,  30, 174); // 0x1b81,  0x0000
        table[173] = new ZpCodecTable(1477,  0, 175, 193); // 0x05c5,  0x0000
        table[174] = new ZpCodecTable(9455,  0,  24,  18); // 0x24ef,  0x0000
        table[175] = new ZpCodecTable(975,  0, 177, 191); // 0x03cf,  0x0000
        table[176] = new ZpCodecTable(11124,  0, 178, 222); // 0x2b74,  0x0000
        table[177] = new ZpCodecTable(645,  0, 179, 189); // 0x0285,  0x0000
        table[178] = new ZpCodecTable(8221,  0, 180, 218); // 0x201d,  0x0000
        table[179] = new ZpCodecTable(427,  0, 181, 187); // 0x01ab,  0x0000

        table[180] = new ZpCodecTable(5909,  0, 182, 216); // 0x1715,  0x0000
        table[181] = new ZpCodecTable(282,  0, 183, 185); // 0x011a,  0x0000
        table[182] = new ZpCodecTable(4023,  0, 184, 214); // 0x0fb7,  0x0000
        table[183] = new ZpCodecTable(186,  0,  69,  61); // 0x00ba,  0x0000
        table[184] = new ZpCodecTable(2663,  0, 186, 212); // 0x0a67,  0x0000
        table[185] = new ZpCodecTable(491,  0,  59,  53); // 0x01eb,  0x0000
        table[186] = new ZpCodecTable(1767,  0, 188, 210); // 0x06e7,  0x0000
        table[187] = new ZpCodecTable(742,  0,  55,  49); // 0x02e6,  0x0000
        table[188] = new ZpCodecTable(1174,  0, 190, 208); // 0x0496,  0x0000
        table[189] = new ZpCodecTable(1118,  0,  51,  45); // 0x045e,  0x0000

        table[190] = new ZpCodecTable(781,  0, 192, 206); // 0x030d,  0x0000
        table[191] = new ZpCodecTable(1680,  0,  47,  39); // 0x0690,  0x0000
        table[192] = new ZpCodecTable(518,  0, 194, 204); // 0x0206,  0x0000
        table[193] = new ZpCodecTable(2526,  0,  41, 195); // 0x09de,  0x0000
        table[194] = new ZpCodecTable(341,  0, 196, 202); // 0x0155,  0x0000
        table[195] = new ZpCodecTable(3528,  0,  37,  31); // 0x0dc8,  0x0000
        table[196] = new ZpCodecTable(225,  0, 198, 200); // 0x00e1,  0x0000
        table[197] = new ZpCodecTable(11124,  0, 199, 243); // 0x2b74
        table[198] = new ZpCodecTable(148,  0,  72,  64); // 0x0094
        table[199] = new ZpCodecTable(8221,  0, 201, 239); // 0x201d

        table[200] = new ZpCodecTable(392,  0,  62,  56); // 0x0188
        table[201] = new ZpCodecTable(5909,  0, 203, 237); // 0x1715
        table[202] = new ZpCodecTable(594,  0,  58,  52); // 0x0252
        table[203] = new ZpCodecTable(4023,  0, 205, 235); // 0x0fb7
        table[204] = new ZpCodecTable(899,  0,  54,  48); // 0x0383
        table[205] = new ZpCodecTable(2663,  0, 207, 233); // 0x0a67
        table[206] = new ZpCodecTable(1351,  0,  50,  44); // 0x0547
        table[207] = new ZpCodecTable(1767,  0, 209, 231); // 0x06e7
        table[208] = new ZpCodecTable(2018,  0,  46,  38); // 0x07e2
        table[209] = new ZpCodecTable(1174,  0, 211, 229); // 0x0496

        table[210] = new ZpCodecTable(3008,  0,  40,  34); // 0x0bc0
        table[211] = new ZpCodecTable(781,  0, 213, 227); // 0x030d
        table[212] = new ZpCodecTable(4472,  0,  36,  28); // 0x1178
        table[213] = new ZpCodecTable(518,  0, 215, 225); // 0x0206
        table[214] = new ZpCodecTable(6618,  0,  30,  22); // 0x19da
        table[215] = new ZpCodecTable(341,  0, 217, 223); // 0x0155
        table[216] = new ZpCodecTable(9455,  0,  26,  16); // 0x24ef
        table[217] = new ZpCodecTable(225,  0, 219, 221); // 0x00e1
        table[218] = new ZpCodecTable(12814,  0,  20, 220); // 0x320e
        table[219] = new ZpCodecTable(148,  0,  71,  63); // 0x0094

        table[220] = new ZpCodecTable(17194,  0,  14,   8); // 0x432a
        table[221] = new ZpCodecTable(392,  0,  61,  55); // 0x0188
        table[222] = new ZpCodecTable(17533,  0,  14, 224); // 0x447d
        table[223] = new ZpCodecTable(594,  0,  57,  51); // 0x0252
        table[224] = new ZpCodecTable(24270,  0,   8,   2); // 0x5ece
        table[225] = new ZpCodecTable(899,  0,  53,  47); // 0x0383
        table[226] = new ZpCodecTable(32768,  0, 228,  87); // 0x8000
        table[227] = new ZpCodecTable(1351,  0,  49,  43); // 0x0547
        table[228] = new ZpCodecTable(18458,  0, 230, 246); // 0x481a
        table[229] = new ZpCodecTable(2018,  0,  45,  37); // 0x07e2

        table[230] = new ZpCodecTable(13689,  0, 232, 244); // 0x3579
        table[231] = new ZpCodecTable(3008,  0,  39,  33); // 0x0bc0
        table[232] = new ZpCodecTable(9455,  0, 234, 238); // 0x24ef
        table[233] = new ZpCodecTable(4472,  0,  35,  27); // 0x1178
        table[234] = new ZpCodecTable(6520,  0, 138, 236); // 0x1978
        table[235] = new ZpCodecTable(6618,  0,  29,  21); // 0x19da
        table[236] = new ZpCodecTable(10341,  0,  24,  16); // 0x2865
        table[237] = new ZpCodecTable(9455,  0,  25,  15); // 0x24ef
        table[238] = new ZpCodecTable(14727,  0, 240,   8); // 0x3987
        table[239] = new ZpCodecTable(12814,  0,  19, 241); // 0x320e

        table[240] = new ZpCodecTable(11417,  0,  22, 242); // 0x2c99
        table[241] = new ZpCodecTable(17194,  0,  13,   7); // 0x432a
        table[242] = new ZpCodecTable(15199,  0,  16,  10); // 0x3b5f
        table[243] = new ZpCodecTable(17533,  0,  13, 245); // 0x447d
        table[244] = new ZpCodecTable(22165,  0,  10,   2); // 0x5695
        table[245] = new ZpCodecTable(24270,  0,   7,   1); // 0x5ece
        table[246] = new ZpCodecTable(32768,  0, 244,  83); // 0x8000
        table[247] = new ZpCodecTable(32768,  0, 249, 250); // 0x8000
        table[248] = new ZpCodecTable(22165,  0,  10,   2); // 0x5695
        table[249] = new ZpCodecTable(18458,  0,  89, 143); // 0x481a

        table[250] = new ZpCodecTable(18458,  0, 230, 246); // 0x481a
        table[251] = new ZpCodecTable(0,  0, 0, 0);     //
        table[252] = new ZpCodecTable(0,  0, 0, 0);     //
        table[253] = new ZpCodecTable(0,  0, 0, 0);     //
        table[254] = new ZpCodecTable(0,  0, 0, 0);     //
        table[255] = new ZpCodecTable(0,  0, 0, 0);     //

        return table;
    }
}

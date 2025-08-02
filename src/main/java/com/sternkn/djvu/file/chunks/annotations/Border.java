package com.sternkn.djvu.file.chunks.annotations;

import com.sternkn.djvu.file.chunks.Color;

public record Border(Boolean none, Boolean xor, Color color) {
}

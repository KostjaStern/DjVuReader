/*
    Copyright (C) 2025 Kostya Stern

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation; either version 2 of the License, or (at your option)
    any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc., 51
    Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
package com.sternkn.djvu.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestSimplePageCache {

    @Mock
    private DjVuModel djvuModel;
    private SimplePageCache cache;

    private final Executor executor;
    private final List<Page> pages;
    private final PageData pageData;

    public TestSimplePageCache() {
        executor = Runnable::run;
        pages = List.of(new Page(7180L, "nb0001.djvu"),
                new Page(68514L, "nb0002.djvu"),
                new Page(83380L, "nb0003.djvu"),
                new Page(83448L, "nb0004.djvu"),
                new Page(100444L, "nb0005.djvu"),
                new Page(196002L, "nb0006.djvu"),
                new Page(278584L, "nb0007.djvu"),
                new Page(397042L, "nb0008.djvu"),
                new Page(515402L, "nb0009.djvu"));
        pageData = new PageData(null);
    }

    @BeforeEach
    public void setUp() {
        when(djvuModel.getPages()).thenReturn(pages);
        when(djvuModel.load(any())).thenReturn(pageData);

        cache = new SimplePageCache(djvuModel, executor);
    }

    @Test
    public void testCacheInitialization() {
        verify(djvuModel, times(2)).load(any(Page.class));
        verify(djvuModel, times(1)).load(eq(pages.get(0)));
        verify(djvuModel, times(1)).load(eq(pages.get(1)));

        assertEquals(Set.of(pages.get(0), pages.get(1)),
                     cache.getCache().keySet());
    }

    @Test
    public void testGetFromCacheOrLoad() {
        CompletableFuture<PageData> page = cache.getFromCacheOrLoad(pages.get(3));

        assertSame(pageData, page.join());

        verify(djvuModel, times(5)).load(any(Page.class));
        verify(djvuModel, times(1)).load(eq(pages.get(0)));
        verify(djvuModel, times(1)).load(eq(pages.get(1)));
        verify(djvuModel, times(1)).load(eq(pages.get(2)));
        verify(djvuModel, times(1)).load(eq(pages.get(3)));
        verify(djvuModel, times(1)).load(eq(pages.get(4)));

        assertEquals(Set.of(pages.get(2), pages.get(3), pages.get(4)),
                     cache.getCache().keySet());
    }

    @Test
    public void testGetFromCacheOrLoadFromTheEnd() {
        CompletableFuture<PageData> page = cache.getFromCacheOrLoad(pages.getLast());

        assertSame(pageData, page.join());

        verify(djvuModel, times(4)).load(any(Page.class));
        verify(djvuModel, times(1)).load(eq(pages.get(0)));
        verify(djvuModel, times(1)).load(eq(pages.get(1)));
        verify(djvuModel, times(1)).load(eq(pages.get(7)));
        verify(djvuModel, times(1)).load(eq(pages.get(8)));

        assertEquals(Set.of(pages.get(7), pages.get(8)),
                cache.getCache().keySet());
    }
}

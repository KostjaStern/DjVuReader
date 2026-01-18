package com.sternkn.djvu.model;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class PageCache {
    private static final Logger LOG = LoggerFactory.getLogger(PageCache.class);

    private static final long MAX_PAGES = 6;
    private static final int PREFETCH_SIZE = 3;

    private final AsyncLoadingCache<Page, PageData> cache;
    private final ExecutorService decodePool;
    private final List<Page> pages;

    public PageCache(DjVuModel model, ExecutorService decodePool) {
        this.decodePool = decodePool;
        this.pages = model.getPages();
        this.cache = Caffeine.newBuilder()
            .maximumSize(MAX_PAGES)
            .executor(decodePool)
            .removalListener((Page page, PageData data, RemovalCause cause) -> {
                LOG.debug("Page {} has been removed with cause {}", page, cause);
            })
            .buildAsync(model::load);
    }

    public CompletableFuture<PageData> get(Page page) {
        CompletableFuture<PageData> future = cache.get(page);
        future.thenRunAsync(() -> prefetchNext(page), decodePool);
        return future;
    }

    private void prefetchNext(Page page) {
        int currentIndex = -1;
        for (int index = 0; index < pages.size(); index++) {
            if (pages.get(index).equals(page)) {
                currentIndex = index;
                break;
            }
        }

        if (currentIndex == -1) {
            LOG.warn("Page {} was not found", page);
            return;
        }

        for (int index = currentIndex + 1;
                 index < Math.min(pages.size(), currentIndex + PREFETCH_SIZE); index++) {
            Page p = pages.get(index);
            cache.get(p);
        }
    }

    public void invalidateAll() {
        cache.synchronous().invalidateAll();
    }

    public void close() {
        decodePool.shutdown();
    }
}

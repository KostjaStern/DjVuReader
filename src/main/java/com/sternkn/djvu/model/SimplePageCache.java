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

import com.sternkn.djvu.file.DjVuFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

public class SimplePageCache implements PageCache {
    private static final Logger LOG = LoggerFactory.getLogger(SimplePageCache.class);

    /**
     * Please note that increasing this property to 2 may cause
     *    java.lang.OutOfMemoryError: Java heap space
     * even with MAVEN_OPTS="-Xmx2g"
     */
    private static final int MAX_PAGES_DISTANCE = 1;

    private final DjVuModel model;
    private final Map<Page, CompletableFuture<PageData>> cache;
    private final Executor executor;

    public SimplePageCache(DjVuModel model) {
        this(model, Executors.newVirtualThreadPerTaskExecutor());
    }

    public SimplePageCache(DjVuModel model, Executor executor) {
        this.model = requireNonNull(model);
        this.executor = requireNonNull(executor);
        this.cache = new ConcurrentHashMap<>(2 * MAX_PAGES_DISTANCE + 1);

        List<Page> pages = this.model.getPages();
        if (!pages.isEmpty()) {
            refreshCache(pages.getFirst());
        }
    }

    @Override
    public CompletableFuture<PageData> getFromCacheOrLoad(Page page) {
        CompletableFuture<PageData> cachedPage = cache.get(page);
        if (cachedPage != null) {
            LOG.debug("cache HIT page: {}", page);
        }
        else {
            LOG.debug("cache MISS page: {}", page);
            cachedPage = CompletableFuture.supplyAsync(() -> this.model.load(page), executor);
            cache.put(page, cachedPage);
        }

        refreshCache(page);

        return cachedPage;
    }

    private void refreshCache(Page page) {
        final List<Page> pages = model.getPages();
        final int currentPageIndex = pages.indexOf(page);
        if (currentPageIndex < 0) {
            throw new DjVuFileException("Page not found: " + page);
        }

        final int fromInclusive = Math.max(0, currentPageIndex - MAX_PAGES_DISTANCE);
        final int toExclusive = Math.min(pages.size(), currentPageIndex + MAX_PAGES_DISTANCE + 1);
        final Set<Page> desiredPages = IntStream
            .range(fromInclusive, toExclusive)
            .mapToObj(pages::get)
            .collect(Collectors.toUnmodifiableSet());

        cache.entrySet().removeIf(entry -> {
            Page key = entry.getKey();
            if (desiredPages.contains(key)) {
                return false;
            }

            CompletableFuture<PageData> future = entry.getValue();
            if (!future.isDone()) {
                LOG.debug("Canceling page {} decoding", key);
                future.cancel(true);
            }

            LOG.debug("Removing page: {} from cache", key);
            return true;
        });

        desiredPages.stream()
            .filter(p -> !cache.containsKey(p))
            .forEach(p -> {
                LOG.debug("Adding page: {} to cache", p);
                cache.put(p, CompletableFuture.supplyAsync(() -> model.load(p), executor));
            });
    }

    Map<Page, CompletableFuture<PageData>> getCache() {
        return cache;
    }
}

package com.campusloop.model;

import java.util.Collections;
import java.util.List;

/** One page of results plus paging metadata. */
public class PagedResult<T> {

    private final List<T> items;
    private final int page;
    private final int pageSize;
    private final int total;

    public PagedResult(List<T> items, int page, int pageSize, int total) {
        this.items = items == null ? Collections.<T>emptyList() : items;
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
    }

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotal() {
        return total;
    }

    public int getTotalPages() {
        return total == 0 ? 1 : (total + pageSize - 1) / pageSize;
    }

    public boolean isHasPrev() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < getTotalPages();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

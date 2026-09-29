package com.campusloop.model;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;

/**
 * Validated marketplace search criteria. Built by MarketServlet from request
 * parameters; invalid values are dropped rather than trusted.
 */
public class ListingFilter {

    public static final String SORT_NEWEST = "newest";
    public static final String SORT_PRICE_ASC = "price_asc";
    public static final String SORT_PRICE_DESC = "price_desc";
    public static final int DEFAULT_PAGE_SIZE = 12;

    private String query = "";
    private String categorySlug = "";
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private ItemCondition condition;
    private String sort = SORT_NEWEST;
    private int page = 1;
    private final int pageSize = DEFAULT_PAGE_SIZE;

    public static String normalizeSort(String sort) {
        if (SORT_PRICE_ASC.equals(sort) || SORT_PRICE_DESC.equals(sort)) {
            return sort;
        }
        return SORT_NEWEST;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query == null ? "" : query;
    }

    public String getCategorySlug() {
        return categorySlug;
    }

    public void setCategorySlug(String categorySlug) {
        this.categorySlug = categorySlug == null ? "" : categorySlug;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public ItemCondition getCondition() {
        return condition;
    }

    public void setCondition(ItemCondition condition) {
        this.condition = condition;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = normalizeSort(sort);
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(1, page);
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getOffset() {
        return (page - 1) * pageSize;
    }

    /** Plain text for re-filling the price inputs. */
    public String getMinPriceText() {
        return minPrice == null ? "" : minPrice.stripTrailingZeros().toPlainString();
    }

    public String getMaxPriceText() {
        return maxPrice == null ? "" : maxPrice.stripTrailingZeros().toPlainString();
    }

    public String getConditionCode() {
        return condition == null ? "" : condition.name();
    }

    /** True when any narrowing filter is applied (used for "Clear filters"). */
    public boolean isFiltered() {
        return !query.isEmpty() || !categorySlug.isEmpty() || minPrice != null
                || maxPrice != null || condition != null;
    }

    /** URL-encoded query string for a given page, e.g. "q=calc&category=books&page=2". */
    public String toQueryString(int targetPage) {
        StringBuilder sb = new StringBuilder();
        append(sb, "q", query);
        append(sb, "category", categorySlug);
        append(sb, "min", getMinPriceText());
        append(sb, "max", getMaxPriceText());
        append(sb, "condition", getConditionCode());
        if (!SORT_NEWEST.equals(sort)) {
            append(sb, "sort", sort);
        }
        if (targetPage > 1) {
            append(sb, "page", String.valueOf(targetPage));
        }
        return sb.toString();
    }

    public String getPrevPageQuery() {
        return toQueryString(page - 1);
    }

    public String getNextPageQuery() {
        return toQueryString(page + 1);
    }

    private static void append(StringBuilder sb, String key, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        if (sb.length() > 0) {
            sb.append('&');
        }
        try {
            sb.append(key).append('=').append(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}

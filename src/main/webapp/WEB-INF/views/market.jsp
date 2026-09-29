<%-- Marketplace. Model: categories, selectedCategory, filter (ListingFilter), result (PagedResult<Listing>), conditions. --%>
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<div class="container">
    <div class="page-head">
        <div>
            <h1><c:out value="${empty selectedCategory ? 'Marketplace' : selectedCategory.name}" /></h1>
            <p class="lead">
                <c:choose>
                    <c:when test="${not empty selectedCategory}"><c:out value="${selectedCategory.description}" /></c:when>
                    <c:otherwise>Pre-loved books, electronics and hostel essentials from students on your campus.</c:otherwise>
                </c:choose>
            </p>
        </div>
        <a class="btn btn-primary" href="${ctx}/sell">Sell an Item</a>
    </div>

    <div class="market-layout">
        <aside class="filters card" aria-label="Filters">
            <div class="card-body">
                <div>
                    <p class="filter-title">Category</p>
                    <ul class="filter-list">
                        <c:url var="allUrl" value="/market">
                            <c:if test="${not empty filter.query}"><c:param name="q" value="${filter.query}" /></c:if>
                            <c:if test="${not empty filter.minPriceText}"><c:param name="min" value="${filter.minPriceText}" /></c:if>
                            <c:if test="${not empty filter.maxPriceText}"><c:param name="max" value="${filter.maxPriceText}" /></c:if>
                            <c:if test="${not empty filter.conditionCode}"><c:param name="condition" value="${filter.conditionCode}" /></c:if>
                            <c:if test="${filter.sort != 'newest'}"><c:param name="sort" value="${filter.sort}" /></c:if>
                        </c:url>
                        <li><a class="${empty filter.categorySlug ? 'active' : ''}" href="${allUrl}">All categories</a></li>
                        <c:forEach var="cat" items="${categories}">
                            <c:url var="catUrl" value="/market">
                                <c:param name="category" value="${cat.slug}" />
                                <c:if test="${not empty filter.query}"><c:param name="q" value="${filter.query}" /></c:if>
                                <c:if test="${not empty filter.minPriceText}"><c:param name="min" value="${filter.minPriceText}" /></c:if>
                                <c:if test="${not empty filter.maxPriceText}"><c:param name="max" value="${filter.maxPriceText}" /></c:if>
                                <c:if test="${not empty filter.conditionCode}"><c:param name="condition" value="${filter.conditionCode}" /></c:if>
                                <c:if test="${filter.sort != 'newest'}"><c:param name="sort" value="${filter.sort}" /></c:if>
                            </c:url>
                            <li><a class="${filter.categorySlug == cat.slug ? 'active' : ''}" href="${catUrl}"><span><c:out value="${cat.name}" /></span><span class="n"><c:out value="${cat.availableCount}" /></span></a></li>
                        </c:forEach>
                    </ul>
                </div>

                <form class="form" method="get" action="${ctx}/market">
                    <c:if test="${not empty filter.query}"><input type="hidden" name="q" value="${fn:escapeXml(filter.query)}"></c:if>
                    <c:if test="${not empty filter.categorySlug}"><input type="hidden" name="category" value="${fn:escapeXml(filter.categorySlug)}"></c:if>
                    <c:if test="${filter.sort != 'newest'}"><input type="hidden" name="sort" value="${fn:escapeXml(filter.sort)}"></c:if>
                    <div>
                        <p class="filter-title">Price (&#8377;)</p>
                        <div class="price-range">
                            <label class="sr-only" for="min">Minimum price</label>
                            <input class="input" id="min" name="min" inputmode="decimal" placeholder="Min" value="${fn:escapeXml(filter.minPriceText)}">
                            <span class="muted">&ndash;</span>
                            <label class="sr-only" for="max">Maximum price</label>
                            <input class="input" id="max" name="max" inputmode="decimal" placeholder="Max" value="${fn:escapeXml(filter.maxPriceText)}">
                        </div>
                    </div>
                    <div>
                        <label class="filter-title" for="condition">Condition</label>
                        <select class="select" id="condition" name="condition">
                            <option value="">Any condition</option>
                            <c:forEach var="cond" items="${conditions}">
                                <option value="${cond.code}"${filter.conditionCode == cond.code ? ' selected' : ''}><c:out value="${cond.label}" /></option>
                            </c:forEach>
                        </select>
                    </div>
                    <button class="btn btn-dark btn-block" type="submit">Apply filters</button>
                    <c:if test="${filter.filtered}"><a class="btn btn-ghost btn-block btn-sm" href="${ctx}/market">Clear all filters</a></c:if>
                </form>
            </div>
        </aside>

        <section aria-label="Results">
            <div class="toolbar">
                <form class="search-inline" method="get" action="${ctx}/market" role="search">
                    <c:if test="${not empty filter.categorySlug}"><input type="hidden" name="category" value="${fn:escapeXml(filter.categorySlug)}"></c:if>
                    <c:if test="${not empty filter.minPriceText}"><input type="hidden" name="min" value="${fn:escapeXml(filter.minPriceText)}"></c:if>
                    <c:if test="${not empty filter.maxPriceText}"><input type="hidden" name="max" value="${fn:escapeXml(filter.maxPriceText)}"></c:if>
                    <c:if test="${not empty filter.conditionCode}"><input type="hidden" name="condition" value="${fn:escapeXml(filter.conditionCode)}"></c:if>
                    <c:if test="${filter.sort != 'newest'}"><input type="hidden" name="sort" value="${fn:escapeXml(filter.sort)}"></c:if>
                    <label class="sr-only" for="q">Search</label>
                    <input class="input" id="q" type="search" name="q" maxlength="80" placeholder="Search items" value="${fn:escapeXml(filter.query)}">
                    <button class="btn btn-dark" type="submit">Search</button>
                </form>
                <form method="get" action="${ctx}/market" class="row">
                    <c:if test="${not empty filter.query}"><input type="hidden" name="q" value="${fn:escapeXml(filter.query)}"></c:if>
                    <c:if test="${not empty filter.categorySlug}"><input type="hidden" name="category" value="${fn:escapeXml(filter.categorySlug)}"></c:if>
                    <c:if test="${not empty filter.minPriceText}"><input type="hidden" name="min" value="${fn:escapeXml(filter.minPriceText)}"></c:if>
                    <c:if test="${not empty filter.maxPriceText}"><input type="hidden" name="max" value="${fn:escapeXml(filter.maxPriceText)}"></c:if>
                    <c:if test="${not empty filter.conditionCode}"><input type="hidden" name="condition" value="${fn:escapeXml(filter.conditionCode)}"></c:if>
                    <label class="sr-only" for="sort">Sort by</label>
                    <select class="select" id="sort" name="sort" data-autosubmit>
                        <option value="newest"${filter.sort == 'newest' ? ' selected' : ''}>Newest first</option>
                        <option value="price_asc"${filter.sort == 'price_asc' ? ' selected' : ''}>Price: low to high</option>
                        <option value="price_desc"${filter.sort == 'price_desc' ? ' selected' : ''}>Price: high to low</option>
                    </select>
                    <noscript><button class="btn btn-outline btn-sm" type="submit">Sort</button></noscript>
                </form>
            </div>

            <p class="toolbar result-count">
                <span><strong><c:out value="${result.total}" /></strong> ${result.total == 1 ? 'item' : 'items'} available<c:if test="${not empty filter.query}"> for &ldquo;<c:out value="${filter.query}" />&rdquo;</c:if></span>
            </p>

            <c:choose>
                <c:when test="${empty result.items}">
                    <div class="card empty">
                        <div class="empty-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="7"/><line x1="16.5" y1="16.5" x2="21" y2="21"/></svg></div>
                        <h3>No items match your search</h3>
                        <p>Try a different keyword, widen the price range, or browse all categories.</p>
                        <a class="btn btn-primary" href="${ctx}/market">Show all items</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="product-grid">
                        <c:forEach var="item" items="${result.items}">
                            <cl:listingCard listing="${item}" />
                        </c:forEach>
                    </div>
                    <c:if test="${result.totalPages > 1}">
                        <nav class="pagination" aria-label="Pages">
                            <c:choose>
                                <c:when test="${result.hasPrev}"><a class="btn btn-outline btn-sm" href="${ctx}/market?${fn:escapeXml(filter.prevPageQuery)}">&larr; Previous</a></c:when>
                                <c:otherwise><span class="btn btn-outline btn-sm disabled" aria-disabled="true">&larr; Previous</span></c:otherwise>
                            </c:choose>
                            <span class="page-info">Page <c:out value="${result.page}" /> of <c:out value="${result.totalPages}" /></span>
                            <c:choose>
                                <c:when test="${result.hasNext}"><a class="btn btn-outline btn-sm" href="${ctx}/market?${fn:escapeXml(filter.nextPageQuery)}">Next &rarr;</a></c:when>
                                <c:otherwise><span class="btn btn-outline btn-sm disabled" aria-disabled="true">Next &rarr;</span></c:otherwise>
                            </c:choose>
                        </nav>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </section>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

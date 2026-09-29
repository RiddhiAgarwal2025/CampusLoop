<%-- Product detail. Model: view (ProductView: listing, sellerRating, sellerReviews, moreFromSeller, owner). --%>
<c:set var="item" value="${view.listing}" />
<c:set var="pageTitle" value="${item.title}" scope="request" />
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<div class="container">
    <nav class="breadcrumbs" aria-label="Breadcrumb">
        <a href="${ctx}/market">Marketplace</a><span>/</span>
        <c:url var="catUrl" value="/market"><c:param name="category" value="${item.categorySlug}" /></c:url>
        <a href="${catUrl}"><c:out value="${item.categoryName}" /></a><span>/</span>
        <span class="truncate"><c:out value="${item.title}" /></span>
    </nav>

    <div class="product-detail">
        <div class="detail-media">
            <img src="${ctx}/${fn:escapeXml(item.imageUrl)}" alt="${fn:escapeXml(item.title)}">
        </div>

        <div class="detail-info">
            <div class="detail-tags">
                <cl:statusBadge tone="${item.status.tone}" label="${item.status.label}" />
                <span class="badge badge-outline badge-plain"><c:out value="${item.categoryName}" /></span>
            </div>
            <h1><c:out value="${item.title}" /></h1>
            <div class="price-lg"><c:out value="${item.priceLabel}" /></div>

            <dl class="spec-list">
                <dt>Condition</dt><dd><c:out value="${item.conditionLabel}" /><span class="muted text-sm"> &middot; <c:out value="${item.condition.hint}" /></span></dd>
                <dt>Category</dt><dd><c:out value="${item.categoryName}" /></dd>
                <dt>Listed</dt><dd><c:out value="${item.createdAtLabel}" /></dd>
                <dt>Availability</dt><dd><c:out value="${item.statusLabel}" /></dd>
            </dl>

            <div class="buy-box">
                <c:choose>
                    <c:when test="${view.owner}">
                        <div class="alert alert-info">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="11" x2="12" y2="16.5"/><circle cx="12" cy="7.5" r=".6" fill="currentColor"/></svg>
                            <p>This is your listing, so you can't buy it. Buyers see an <strong>Add to Cart</strong> button here.</p>
                        </div>
                        <div class="row">
                            <c:if test="${item.editable}"><a class="btn btn-dark" href="${ctx}/edit?id=${item.id}">Edit listing</a></c:if>
                            <a class="btn btn-outline" href="${ctx}/my-listings">My listings</a>
                        </div>
                    </c:when>
                    <c:when test="${item.available}">
                        <form method="post" action="${ctx}/cart/add">
                            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="listingId" value="${item.id}">
                            <button type="submit" class="btn btn-primary btn-lg btn-block" data-busy="Adding…">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 6h15l-1.5 9h-12z"/><path d="M6 6L5 3H2"/><circle cx="9" cy="20" r="1.5"/><circle cx="18" cy="20" r="1.5"/></svg>
                                Add to Cart
                            </button>
                        </form>
                        <p class="buy-note">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2l8 4v6c0 5-3.5 8.5-8 10-4.5-1.5-8-5-8-10V6z"/><polyline points="8.5 12 11 14.5 15.5 9.5"/></svg>
                            <span>No payment now. At checkout you propose a meetup spot and time, and your wallet is only charged after the seller confirms.</span>
                        </p>
                    </c:when>
                    <c:when test="${item.status == 'RESERVED'}">
                        <div class="alert alert-warning"><p>This item is reserved: the seller has confirmed an exchange with another student.</p></div>
                        <a class="btn btn-outline" href="${ctx}/market">Browse similar items</a>
                    </c:when>
                    <c:otherwise>
                        <div class="alert alert-success"><p>This item has already found its second home.</p></div>
                        <a class="btn btn-outline" href="${ctx}/market">Browse available items</a>
                    </c:otherwise>
                </c:choose>
            </div>

            <h3 class="mt-3">Description</h3>
            <p class="description"><c:out value="${item.description}" /></p>

            <div class="card seller-card mt-3">
                <cl:avatar name="${item.sellerName}" image="${item.sellerImage}" size="md" />
                <div class="who">
                    <span class="text-xs muted">Sold by</span>
                    <strong><c:out value="${item.sellerName}" /></strong>
                    <div class="row" style="gap:.4rem"><cl:stars rating="${view.sellerRating.average}" count="${view.sellerRating.count}" /><c:if test="${view.sellerRating.rated}"><span class="text-xs muted"><c:out value="${view.sellerRating.countLabel}" /></span></c:if></div>
                </div>
                <a class="btn btn-outline btn-sm" href="${ctx}/seller?id=${item.sellerId}">View seller</a>
            </div>

            <c:if test="${not empty view.sellerReviews}">
                <div class="card mt-2">
                    <div class="card-head"><h3>What buyers say about <c:out value="${item.sellerFirstName}" /></h3></div>
                    <c:forEach var="rev" items="${view.sellerReviews}">
                        <div class="review-item">
                            <div class="review-head">
                                <cl:avatar name="${rev.reviewerName}" image="${rev.reviewerImage}" size="xs" />
                                <strong><c:out value="${rev.reviewerFirstName}" /></strong>
                                <cl:stars rating="${0.0 + rev.rating}" count="${1}" />
                                <span class="when"><c:out value="${rev.createdAtLabel}" /></span>
                            </div>
                            <p><c:out value="${rev.comment}" /></p>
                        </div>
                    </c:forEach>
                </div>
            </c:if>
        </div>
    </div>

    <c:if test="${not empty view.moreFromSeller}">
        <section class="section">
            <div class="section-head">
                <h2>More from <c:out value="${item.sellerFirstName}" /></h2>
                <a class="btn btn-ghost btn-sm" href="${ctx}/seller?id=${item.sellerId}">See all &rarr;</a>
            </div>
            <div class="product-grid cols-4">
                <c:forEach var="other" items="${view.moreFromSeller}">
                    <cl:listingCard listing="${other}" />
                </c:forEach>
            </div>
        </section>
    </c:if>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

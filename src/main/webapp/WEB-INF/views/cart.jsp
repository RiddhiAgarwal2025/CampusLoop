<%-- Cart. Model: cart (CartView: items, guest, count, activeCount, unavailableCount, totalLabel). --%>
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<div class="container">
    <div class="page-head">
        <div>
            <h1>Your cart</h1>
            <p class="lead">
                <c:choose>
                    <c:when test="${cart.count == 0}">Nothing here yet.</c:when>
                    <c:when test="${cart.count == 1}">1 item</c:when>
                    <c:otherwise><c:out value="${cart.count}" /> items</c:otherwise>
                </c:choose>
            </p>
        </div>
        <a class="btn btn-outline" href="${ctx}/market">Continue browsing</a>
    </div>

    <c:if test="${cart.guest and cart.count > 0}">
        <div class="alert alert-info mb-2">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="11" x2="12" y2="16.5"/><circle cx="12" cy="7.5" r=".6" fill="currentColor"/></svg>
            <p>You're browsing as a guest. This cart is saved on this device for 7 days. <a href="${ctx}/login">Log in</a> or <a href="${ctx}/register">create an account</a> to check out; your items move to your account automatically.</p>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${cart.count == 0}">
            <div class="card empty mb-3">
                <div class="empty-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 6h15l-1.5 9h-12z"/><path d="M6 6L5 3H2"/><circle cx="9" cy="20" r="1.5"/><circle cx="18" cy="20" r="1.5"/></svg></div>
                <h3>Your cart is empty</h3>
                <p>Find a pre-loved textbook, calculator or hostel essential from a student on your campus.</p>
                <a class="btn btn-primary" href="${ctx}/market">Browse Marketplace</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="split mb-3">
                <div class="list">
                    <c:forEach var="line" items="${cart.items}">
                        <c:set var="item" value="${line.listing}" />
                        <div class="list-item">
                            <a href="${ctx}/product?id=${item.id}"><img class="list-thumb" src="${ctx}/${fn:escapeXml(item.imageUrl)}" alt="${fn:escapeXml(item.title)}" loading="lazy"></a>
                            <div class="list-body">
                                <p class="list-title"><a href="${ctx}/product?id=${item.id}"><c:out value="${item.title}" /></a></p>
                                <div class="list-meta">
                                    <span><cl:avatar name="${item.sellerName}" image="${item.sellerImage}" size="xs" />Sold by <c:out value="${item.sellerName}" /></span>
                                    <span><cl:stars rating="${item.sellerRating}" count="${item.sellerReviewCount}" /></span>
                                    <span><c:out value="${item.conditionLabel}" /></span>
                                </div>
                                <div class="row" style="gap:.5rem">
                                    <c:choose>
                                        <c:when test="${line.hasActiveRequest}">
                                            <cl:statusBadge tone="${line.activeTransactionStatus.tone}" label="${line.activeTransactionStatus.label}" />
                                            <span class="text-sm muted">
                                                <c:choose>
                                                    <c:when test="${line.activeTransactionStatus == 'AWAITING_PAYMENT'}">The seller confirmed your exchange.</c:when>
                                                    <c:otherwise>Your exchange request is with the seller.</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </c:when>
                                        <c:when test="${line.unavailable}">
                                            <cl:statusBadge tone="${item.status.tone}" label="${item.status.label}" />
                                            <span class="text-sm muted">No longer available. You can remove it.</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-success">Available</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div class="list-side">
                                <span class="list-price"><c:out value="${item.priceLabel}" /></span>
                                <div class="list-actions">
                                    <c:choose>
                                        <c:when test="${line.hasActiveRequest and line.activeTransactionStatus == 'AWAITING_PAYMENT'}">
                                            <a class="btn btn-primary btn-sm" href="${ctx}/exchange?id=${line.activeTransactionId}">Pay now</a>
                                        </c:when>
                                        <c:when test="${line.hasActiveRequest}">
                                            <a class="btn btn-outline btn-sm" href="${ctx}/exchange?id=${line.activeTransactionId}">View request</a>
                                        </c:when>
                                        <c:when test="${line.checkoutReady}">
                                            <a class="btn btn-primary btn-sm" href="${ctx}/checkout?listingId=${item.id}">Proceed to checkout</a>
                                        </c:when>
                                    </c:choose>
                                    <form class="inline-form" method="post" action="${ctx}/cart/remove">
                                        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                        <input type="hidden" name="listingId" value="${item.id}">
                                        <button type="submit" class="btn btn-ghost btn-sm">Remove</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <aside class="card card-pad summary-card">
                    <h3>Summary</h3>
                    <div class="summary-row"><span>Items you can get</span><strong class="ink"><c:out value="${cart.activeCount}" /></strong></div>
                    <c:if test="${cart.unavailableCount > 0}">
                        <div class="summary-row muted"><span>No longer available</span><span><c:out value="${cart.unavailableCount}" /></span></div>
                    </c:if>
                    <div class="summary-row summary-total"><span>Total</span><span><c:out value="${cart.totalLabel}" /></span></div>
                    <p class="sim-note mt-2">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2l8 4v6c0 5-3.5 8.5-8 10-4.5-1.5-8-5-8-10V6z"/><polyline points="8.5 12 11 14.5 15.5 9.5"/></svg>
                        <span>Each item is checked out separately because you meet each seller in person. Nothing is charged until the seller confirms your exchange.</span>
                    </p>
                    <c:if test="${cart.guest}">
                        <div class="stack-sm mt-2">
                            <a class="btn btn-primary btn-block" href="${ctx}/login">Log in to check out</a>
                            <a class="btn btn-outline btn-block" href="${ctx}/register">Create an account</a>
                        </div>
                    </c:if>
                </aside>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

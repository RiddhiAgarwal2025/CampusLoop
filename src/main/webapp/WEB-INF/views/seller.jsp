<%-- Public seller page. Model: seller (SellerProfile). No email, wallet or other private data. --%>
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<div class="container">
    <div class="page-head">
        <div class="row">
            <cl:avatar name="${seller.name}" image="${seller.profileImage}" size="lg" />
            <div>
                <h1><c:out value="${seller.name}" /></h1>
                <p class="lead">Member since <c:out value="${seller.memberSinceLabel}" /> &middot; <c:out value="${seller.completedSales}" /> completed ${seller.completedSales == 1 ? 'sale' : 'sales'}</p>
            </div>
        </div>
        <div class="card card-pad rating-summary">
            <c:choose>
                <c:when test="${seller.rating.rated}">
                    <span class="big"><c:out value="${seller.rating.averageLabel}" /></span>
                    <div>
                        <cl:stars rating="${seller.rating.average}" count="${seller.rating.count}" />
                        <div class="text-sm muted"><c:out value="${seller.rating.countLabel}" /></div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div><strong class="ink">New seller</strong><div class="text-sm muted">No reviews yet</div></div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <section class="section-tight">
        <div class="section-head"><h2>Available from <c:out value="${seller.firstName}" /></h2></div>
        <c:choose>
            <c:when test="${empty seller.listings}">
                <div class="card empty"><h3>Nothing available right now</h3><p><c:out value="${seller.firstName}" /> has no items for sale at the moment.</p><a class="btn btn-outline" href="${ctx}/market">Browse the marketplace</a></div>
            </c:when>
            <c:otherwise>
                <div class="product-grid cols-4">
                    <c:forEach var="item" items="${seller.listings}"><cl:listingCard listing="${item}" /></c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <section class="section">
        <div class="card">
            <div class="card-head"><h2>Reviews</h2><span class="muted text-sm"><c:out value="${seller.rating.countLabel}" /></span></div>
            <c:choose>
                <c:when test="${empty seller.reviews}">
                    <div class="empty"><p>Reviews appear here after buyers complete an exchange with <c:out value="${seller.firstName}" />.</p></div>
                </c:when>
                <c:otherwise>
                    <c:forEach var="rev" items="${seller.reviews}">
                        <div class="review-item">
                            <div class="review-head">
                                <cl:avatar name="${rev.reviewerName}" image="${rev.reviewerImage}" size="xs" />
                                <strong><c:out value="${rev.reviewerFirstName}" /></strong>
                                <cl:stars rating="${0.0 + rev.rating}" count="${1}" />
                                <span class="when"><c:out value="${rev.createdAtLabel}" /></span>
                            </div>
                            <p><c:out value="${rev.comment}" /></p>
                            <p class="about">Bought: <c:out value="${rev.listingTitle}" /></p>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

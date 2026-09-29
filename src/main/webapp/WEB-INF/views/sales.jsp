<%-- Sales. Model: history (TradeHistory from the seller's point of view). --%>
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<div class="container">
    <div class="page-head">
        <div>
            <h1>Sales</h1>
            <p class="lead">Exchange requests for your items, and everything you've sold.</p>
        </div>
        <div class="page-actions">
            <a class="btn btn-ghost" href="${ctx}/dashboard">Back to Dashboard</a>
            <a class="btn btn-primary" href="${ctx}/sell">Sell an item</a>
        </div>
    </div>

    <c:choose>
        <c:when test="${history.total == 0}">
            <div class="card empty mb-3">
                <div class="empty-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.6 13.4l-7.2 7.2a2 2 0 0 1-2.8 0L3 13V3h10l7.6 7.6a2 2 0 0 1 0 2.8z"/><circle cx="7.5" cy="7.5" r="1.5"/></svg></div>
                <h3>No sales yet</h3>
                <p>When a student proposes an exchange for one of your items, it appears here for you to confirm.</p>
                <a class="btn btn-primary" href="${ctx}/sell">List an item</a>
            </div>
        </c:when>
        <c:otherwise>
            <section class="section-tight">
                <div class="section-head">
                    <div>
                        <h2>Pending confirmations <span class="badge badge-warning badge-plain"><c:out value="${fn:length(history.needsAction)}" /></span></h2>
                        <p>Check the proposed place and time, then confirm or reject. Confirming reserves the item for that buyer.</p>
                    </div>
                </div>
                <c:choose>
                    <c:when test="${empty history.needsAction}">
                        <div class="card card-pad text-sm muted">No requests are waiting for you right now.</div>
                    </c:when>
                    <c:otherwise>
                        <div class="list"><c:forEach var="t" items="${history.needsAction}"><cl:tradeRow t="${t}" asBuyer="${false}" highlight="${true}" /></c:forEach></div>
                    </c:otherwise>
                </c:choose>
            </section>
            <c:if test="${not empty history.inProgress}">
                <section class="section-tight">
                    <div class="section-head"><div><h2>Awaiting buyer payment</h2><p>You confirmed these; your wallet is credited when the buyer pays.</p></div></div>
                    <div class="list"><c:forEach var="t" items="${history.inProgress}"><cl:tradeRow t="${t}" asBuyer="${false}" /></c:forEach></div>
                </section>
            </c:if>
            <c:if test="${not empty history.completed}">
                <section class="section-tight">
                    <div class="section-head"><div><h2>Completed sales</h2><p>Paid and credited to your wallet.</p></div></div>
                    <div class="list"><c:forEach var="t" items="${history.completed}"><cl:tradeRow t="${t}" asBuyer="${false}" /></c:forEach></div>
                </section>
            </c:if>
            <c:if test="${not empty history.closed}">
                <section class="section-tight mb-3">
                    <div class="section-head"><div><h2>Declined or cancelled</h2><p>Closed without a sale.</p></div></div>
                    <div class="list"><c:forEach var="t" items="${history.closed}"><cl:tradeRow t="${t}" asBuyer="${false}" /></c:forEach></div>
                </section>
            </c:if>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

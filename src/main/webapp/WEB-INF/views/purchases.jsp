<%-- Purchases. Model: history (TradeHistory from the buyer's point of view). --%>
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<div class="container">
    <div class="page-head">
        <div>
            <h1>Purchases</h1>
            <p class="lead">Everything you've bought or asked to buy on CampusLoop.</p>
        </div>
        <div class="page-actions">
            <a class="btn btn-ghost" href="${ctx}/dashboard">Back to Dashboard</a>
            <a class="btn btn-primary" href="${ctx}/market">Browse Marketplace</a>
        </div>
    </div>

    <c:choose>
        <c:when test="${history.total == 0}">
            <div class="card empty mb-3">
                <div class="empty-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 6h15l-1.5 9h-12z"/><path d="M6 6L5 3H2"/><circle cx="9" cy="20" r="1.5"/><circle cx="18" cy="20" r="1.5"/></svg></div>
                <h3>No purchases yet</h3>
                <p>When you propose an exchange for an item, it appears here with its status, meetup details and payment.</p>
                <a class="btn btn-primary" href="${ctx}/market">Find something</a>
            </div>
        </c:when>
        <c:otherwise>
            <c:if test="${not empty history.needsAction}">
                <section class="section-tight">
                    <div class="section-head"><div><h2>Ready to pay</h2><p>The seller confirmed; pay from your wallet to complete the exchange.</p></div></div>
                    <div class="list"><c:forEach var="t" items="${history.needsAction}"><cl:tradeRow t="${t}" asBuyer="${true}" highlight="${true}" /></c:forEach></div>
                </section>
            </c:if>
            <c:if test="${not empty history.inProgress}">
                <section class="section-tight">
                    <div class="section-head"><div><h2>Waiting for the seller</h2><p>Nothing is charged until they confirm.</p></div></div>
                    <div class="list"><c:forEach var="t" items="${history.inProgress}"><cl:tradeRow t="${t}" asBuyer="${true}" /></c:forEach></div>
                </section>
            </c:if>
            <c:if test="${not empty history.completed}">
                <section class="section-tight">
                    <div class="section-head"><div><h2>Completed</h2><p>Paid purchases. Rate the seller to help other students.</p></div></div>
                    <div class="list"><c:forEach var="t" items="${history.completed}"><cl:tradeRow t="${t}" asBuyer="${true}" /></c:forEach></div>
                </section>
            </c:if>
            <c:if test="${not empty history.closed}">
                <section class="section-tight mb-3">
                    <div class="section-head"><div><h2>Declined or cancelled</h2><p>Closed without a sale. Nobody was charged.</p></div></div>
                    <div class="list"><c:forEach var="t" items="${history.closed}"><cl:tradeRow t="${t}" asBuyer="${true}" /></c:forEach></div>
                </section>
            </c:if>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

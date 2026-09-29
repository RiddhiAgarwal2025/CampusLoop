<%-- Dashboard. Model: dash (DashboardView). Every number is a live database value. --%>
<c:set var="s" value="${dash.stats}" />
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<div class="container">
    <div class="dash-head">
        <div>
            <span class="eyebrow">CampusLoop</span>
            <h1 class="greeting"><c:out value="${dash.greeting}" />, <c:out value="${dash.student.firstName}" />.</h1>
            <p class="date"><c:out value="${dash.dateLabel}" /></p>
        </div>
        <div class="page-actions">
            <a class="btn btn-outline" href="${ctx}/market">Browse Marketplace</a>
            <a class="btn btn-primary" href="${ctx}/sell">Sell an Item</a>
        </div>
    </div>

    <c:if test="${s.pendingConfirmations > 0 or s.awaitingPayment > 0}">
        <div class="attention mb-2">
            <c:if test="${s.pendingConfirmations > 0}">
                <a class="alert alert-warning" href="${ctx}/sales">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6.5 12 12 15.5 14"/></svg>
                    <p><strong><c:out value="${s.pendingConfirmations}" />${s.pendingConfirmations == 1 ? ' exchange request is' : ' exchange requests are'}</strong> waiting for your confirmation. Review them in Sales &rarr;</p>
                </a>
            </c:if>
            <c:if test="${s.awaitingPayment > 0}">
                <a class="alert alert-info" href="${ctx}/purchases">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2.5" y="6" width="19" height="13" rx="2.5"/><path d="M2.5 9.5h19"/></svg>
                    <p><strong><c:out value="${s.awaitingPayment}" />${s.awaitingPayment == 1 ? ' confirmed exchange is' : ' confirmed exchanges are'}</strong> ready for you to pay. Open Purchases &rarr;</p>
                </a>
            </c:if>
        </div>
    </c:if>

    <section class="stats-grid" aria-label="Your numbers">
        <div class="card stat-card wallet-card">
            <div class="stat-top"><span class="stat-label">Wallet</span><span class="stat-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2.5" y="6" width="19" height="13" rx="2.5"/><path d="M16 12.5h2"/><path d="M2.5 9.5h19"/></svg></span></div>
            <div class="stat-value"><c:out value="${s.walletLabel}" /></div>
            <div class="stat-sub"><a href="${ctx}/wallet">Add money &rarr;</a></div>
        </div>
        <div class="card stat-card">
            <div class="stat-top"><span class="stat-label">Listings</span><span class="stat-icon navy"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.6 13.4l-7.2 7.2a2 2 0 0 1-2.8 0L3 13V3h10l7.6 7.6a2 2 0 0 1 0 2.8z"/><circle cx="7.5" cy="7.5" r="1.5"/></svg></span></div>
            <div class="stat-value"><c:out value="${s.activeListings}" /></div>
            <div class="stat-sub">active &middot; <c:out value="${s.soldListings}" /> sold &middot; <a href="${ctx}/my-listings">Manage</a></div>
        </div>
        <div class="card stat-card impact-stat">
            <div class="stat-top"><span class="stat-label">Impact Points</span><span class="stat-icon green"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 11a8 8 0 0 0-14.3-4.9"/><polyline points="5 2.5 5.6 6.2 9.3 5.6"/><path d="M4 13a8 8 0 0 0 14.3 4.9"/><polyline points="19 21.5 18.4 17.8 14.7 18.4"/></svg></span></div>
            <div class="stat-value"><c:out value="${s.impactPoints}" /><span aria-hidden="true"> &#9851;</span></div>
            <div class="stat-sub">from <c:out value="${s.completedResales}" /> completed ${s.completedResales == 1 ? 'resale' : 'resales'}</div>
        </div>
        <div class="card stat-card">
            <div class="stat-top"><span class="stat-label">Exchanges</span><span class="stat-icon sky"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="17 1 21 5 17 9"/><path d="M3 11V9a4 4 0 0 1 4-4h14"/><polyline points="7 23 3 19 7 15"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/></svg></span></div>
            <div class="stat-value"><c:out value="${s.completedPurchases + s.completedSales}" /></div>
            <div class="stat-sub"><a href="${ctx}/purchases"><c:out value="${s.completedPurchases}" /> bought</a> &middot; <a href="${ctx}/sales"><c:out value="${s.completedSales}" /> sold</a></div>
        </div>
    </section>

    <div class="split mt-3 mb-3">
        <div class="card">
            <div class="card-head">
                <h2>Recent activity</h2>
                <a class="text-sm" href="${ctx}/wallet">Wallet activity &rarr;</a>
            </div>
            <c:choose>
                <c:when test="${empty dash.activity}">
                    <div class="empty">
                        <div class="empty-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 11a8 8 0 0 0-14.3-4.9"/><polyline points="5 2.5 5.6 6.2 9.3 5.6"/><path d="M4 13a8 8 0 0 0 14.3 4.9"/><polyline points="19 21.5 18.4 17.8 14.7 18.4"/></svg></div>
                        <h3>Your loop starts here</h3>
                        <p>List something you no longer need or find your next textbook. Everything you do will show up here.</p>
                        <div class="row" style="justify-content:center"><a class="btn btn-primary" href="${ctx}/sell">Sell an Item</a><a class="btn btn-outline" href="${ctx}/wallet">Add money</a></div>
                    </div>
                </c:when>
                <c:otherwise>
                    <ul class="activity-list">
                        <c:forEach var="a" items="${dash.activity}">
                            <li class="activity-item">
                                <span class="activity-icon ${a.kind}" aria-hidden="true">
                                    <c:choose>
                                        <c:when test="${a.kind == 'topup'}"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg></c:when>
                                        <c:when test="${a.kind == 'sale'}"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="7 17 17 7"/><polyline points="8 7 17 7 17 16"/></svg></c:when>
                                        <c:when test="${a.kind == 'purchase'}"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 6h15l-1.5 9h-12z"/><path d="M6 6L5 3H2"/><circle cx="9" cy="20" r="1.5"/><circle cx="18" cy="20" r="1.5"/></svg></c:when>
                                        <c:when test="${a.kind == 'review'}"><svg viewBox="0 0 24 24" fill="currentColor"><polygon points="12,2.5 14.9,8.6 21.5,9.4 16.6,13.9 17.9,20.5 12,17.2 6.1,20.5 7.4,13.9 2.5,9.4 9.1,8.6"/></svg></c:when>
                                        <c:when test="${a.kind == 'listing'}"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.6 13.4l-7.2 7.2a2 2 0 0 1-2.8 0L3 13V3h10l7.6 7.6a2 2 0 0 1 0 2.8z"/><circle cx="7.5" cy="7.5" r="1.5"/></svg></c:when>
                                        <c:otherwise><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="17 1 21 5 17 9"/><path d="M3 11V9a4 4 0 0 1 4-4h14"/><polyline points="7 23 3 19 7 15"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/></svg></c:otherwise>
                                    </c:choose>
                                </span>
                                <div class="activity-text">
                                    <strong><a href="${ctx}${a.link}"><c:out value="${a.title}" /></a></strong>
                                    <span><c:out value="${a.detail}" /> &middot; <c:out value="${a.whenLabel}" /></span>
                                </div>
                                <c:if test="${not empty a.amountLabel}"><span class="activity-amount${a.credit ? ' credit' : ''}"><c:out value="${a.amountLabel}" /></span></c:if>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </div>

        <aside class="stack">
            <div class="card card-pad">
                <h3>Quick actions</h3>
                <div class="quick-actions mt-1">
                    <a class="quick-action" href="${ctx}/sell"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>Sell an item</a>
                    <a class="quick-action" href="${ctx}/my-listings"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.6 13.4l-7.2 7.2a2 2 0 0 1-2.8 0L3 13V3h10l7.6 7.6a2 2 0 0 1 0 2.8z"/></svg>My listings</a>
                    <a class="quick-action" href="${ctx}/purchases"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 6h15l-1.5 9h-12z"/><path d="M6 6L5 3H2"/></svg>Purchases</a>
                    <a class="quick-action" href="${ctx}/sales"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="7 17 17 7"/><polyline points="8 7 17 7 17 16"/></svg>Sales</a>
                    <a class="quick-action" href="${ctx}/wallet"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2.5" y="6" width="19" height="13" rx="2.5"/><path d="M2.5 9.5h19"/></svg>Wallet</a>
                    <a class="quick-action" href="${ctx}/profile"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/></svg>Profile</a>
                </div>
            </div>

            <div class="card card-pad impact-explainer">
                <h3 class="mb-0">Your impact</h3>
                <div class="impact-rule">
                    <span class="num">+<c:out value="${dash.pointsPerResale}" /></span>
                    <p>Impact Points for you <strong>and</strong> the other student every time a CampusLoop resale is completed and paid.</p>
                </div>
                <p class="text-sm mb-0"><strong class="ink"><c:out value="${s.completedResales}" /></strong> completed ${s.completedResales == 1 ? 'resale' : 'resales'} &times; <c:out value="${dash.pointsPerResale}" /> = <strong class="green"><c:out value="${s.completedResales * dash.pointsPerResale}" /> points</strong></p>
                <p class="text-sm mb-0">Together, students have recirculated <strong class="ink"><c:out value="${dash.itemsRecirculated}" /></strong> ${dash.itemsRecirculated == 1 ? 'item' : 'items'} through CampusLoop.</p>
                <p class="fine-print">Impact Points count participation in reuse on CampusLoop. They are not an estimate of carbon, waste or money saved.</p>
            </div>
        </aside>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

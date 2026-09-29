<%-- Wallet. Model: wallet (WalletView), amount, errors, quickAmounts (value -> label),
     minTopUp/maxTopUp (numbers for validation) and their display labels. --%>
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<div class="container">
    <div class="page-head">
        <div>
            <h1>Wallet</h1>
            <p class="lead">Your CampusLoop balance and every movement in and out of it.</p>
        </div>
        <a class="btn btn-ghost" href="${ctx}/dashboard">Back to Dashboard</a>
    </div>

    <section class="balance-hero mb-3">
        <div class="label">Available balance</div>
        <div class="amount"><c:out value="${wallet.balanceLabel}" /></div>
        <p>Added <strong><c:out value="${wallet.totalTopUpsLabel}" /></strong> &middot; Spent <strong><c:out value="${wallet.totalSpentLabel}" /></strong> &middot; Earned from sales <strong><c:out value="${wallet.totalEarnedLabel}" /></strong></p>
    </section>

    <div class="split mb-3">
        <div class="card">
            <div class="card-head">
                <h2>Wallet activity</h2>
                <span class="muted text-sm">Latest <c:out value="${fn:length(wallet.entries)}" /></span>
            </div>
            <c:choose>
                <c:when test="${empty wallet.entries}">
                    <div class="empty">
                        <div class="empty-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2.5" y="6" width="19" height="13" rx="2.5"/><path d="M16 12.5h2"/><path d="M2.5 9.5h19"/></svg></div>
                        <h3>No wallet activity yet</h3>
                        <p>Add money to get started. Purchases and sale credits will appear here automatically.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table class="table">
                            <thead>
                                <tr><th>Date</th><th>Details</th><th class="amount">Amount</th><th class="amount hide-sm">Balance</th></tr>
                            </thead>
                            <tbody>
                                <c:forEach var="e" items="${wallet.entries}">
                                    <tr>
                                        <td class="date"><c:out value="${e.createdAtLabel}" /></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${e.type == 'TOPUP'}"><span class="badge badge-info badge-plain">Top-up</span></c:when>
                                                <c:when test="${e.type == 'SALE_CREDIT'}"><span class="badge badge-success badge-plain">Sale credit</span></c:when>
                                                <c:otherwise><span class="badge badge-neutral badge-plain">Purchase</span></c:otherwise>
                                            </c:choose>
                                            <div class="mt-1">
                                                <c:choose>
                                                    <c:when test="${not empty e.transactionId}"><a href="${ctx}/exchange?id=${e.transactionId}"><c:out value="${e.description}" /></a></c:when>
                                                    <c:otherwise><c:out value="${e.description}" /></c:otherwise>
                                                </c:choose>
                                            </div>
                                        </td>
                                        <td class="amount ${e.credit ? 'credit' : 'debit'}"><c:out value="${e.amountLabel}" /></td>
                                        <td class="amount muted hide-sm"><c:out value="${e.balanceAfterLabel}" /></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <aside class="stack">
            <div class="card card-pad">
                <h3>Add money</h3>
                <p class="text-sm muted">Choose a quick amount or enter your own (<c:out value="${minTopUpLabel}" />&ndash;<c:out value="${maxTopUpLabel}" /> per top-up).</p>

                <form method="post" action="${ctx}/wallet" class="choice-group mb-2" aria-label="Quick amounts">
                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                    <c:forEach var="q" items="${quickAmounts}">
                        <button type="submit" name="amount" value="${q.key}" class="btn btn-outline btn-sm">+ <c:out value="${q.value}" /></button>
                    </c:forEach>
                </form>

                <form class="form" method="post" action="${ctx}/wallet" data-validate>
                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                    <div class="field${not empty errors.amount ? ' has-error' : ''}">
                        <label class="label" for="amount">Custom amount</label>
                        <div class="input-group">
                            <span class="prefix">&#8377;</span>
                            <input class="input" id="amount" name="amount" inputmode="decimal" value="${fn:escapeXml(amount)}"
                                   required data-money min="${minTopUp}" max="${maxTopUp}" placeholder="1500"
                                   data-msg-required="Please enter an amount to add.">
                        </div>
                        <c:if test="${not empty errors.amount}"><p class="field-error"><c:out value="${errors.amount}" /></p></c:if>
                    </div>
                    <button type="submit" class="btn btn-primary btn-block" data-busy="Adding…">Add money</button>
                </form>
            </div>

            <div class="card card-pad">
                <p class="sim-note">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="11" x2="12" y2="16.5"/><circle cx="12" cy="7.5" r=".6" fill="currentColor"/></svg>
                    <span><strong class="ink">Simulated wallet.</strong> CampusLoop is an academic project: Add Money does not charge a card or bank account and no payment gateway is used. The balance is only spent on confirmed CampusLoop exchanges.</span>
                </p>
            </div>
        </aside>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

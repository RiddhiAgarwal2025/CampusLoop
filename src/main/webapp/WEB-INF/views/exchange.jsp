<%-- Exchange status page. Model: view (ExchangeView: transaction, buyer/seller, viewer balance, currentStep). --%>
<c:set var="t" value="${view.transaction}" />
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<div class="container">
    <div class="page-head">
        <div>
            <span class="eyebrow">Exchange #<c:out value="${t.id}" /></span>
            <h1><c:out value="${t.listingTitle}" /></h1>
            <p class="lead">
                <c:choose>
                    <c:when test="${view.buyer}">You're buying from <c:out value="${t.sellerName}" />.</c:when>
                    <c:otherwise><c:out value="${t.buyerName}" /> wants to buy your item.</c:otherwise>
                </c:choose>
            </p>
        </div>
        <div class="page-actions">
            <c:choose>
                <c:when test="${view.buyer}"><a class="btn btn-ghost" href="${ctx}/purchases">My purchases</a></c:when>
                <c:otherwise><a class="btn btn-ghost" href="${ctx}/sales">My sales</a></c:otherwise>
            </c:choose>
            <a class="btn btn-outline" href="${ctx}/dashboard">Back to Dashboard</a>
        </div>
    </div>

    <ol class="steps" aria-label="Exchange progress">
        <c:forEach var="i" begin="1" end="4">
            <c:set var="stepClass" value="${i < view.currentStep ? 'done' : (i == view.currentStep ? (view.closedWithoutSale ? 'failed' : 'current') : '')}" />
            <li class="step ${stepClass}">
                <span class="dot">
                    <c:choose>
                        <c:when test="${stepClass == 'done'}"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="5 12.5 10 17.5 19 7.5"/></svg></c:when>
                        <c:when test="${stepClass == 'failed'}">&times;</c:when>
                        <c:otherwise><c:out value="${i}" /></c:otherwise>
                    </c:choose>
                </span>
                <c:choose>
                    <c:when test="${i == 1}">Request sent</c:when>
                    <c:when test="${i == 2}">Seller confirms</c:when>
                    <c:when test="${i == 3}">Payment</c:when>
                    <c:otherwise>Completed</c:otherwise>
                </c:choose>
            </li>
        </c:forEach>
    </ol>

    <div class="split mb-3">
        <div class="card">
            <div class="card-body stack">
                <div class="row-between">
                    <div class="item-summary">
                        <img src="${ctx}/${fn:escapeXml(t.listingImage)}" alt="">
                        <div>
                            <div class="t"><a href="${ctx}/product?id=${t.listingId}"><c:out value="${t.listingTitle}" /></a></div>
                            <div class="price-lg mt-1" style="font-size:1.6rem"><c:out value="${t.amountLabel}" /></div>
                        </div>
                    </div>
                    <cl:statusBadge tone="${t.statusTone}" label="${t.statusLabel}" />
                </div>

                <hr>

                <div class="detail-grid">
                    <div><div class="k">Buyer</div><div class="v row" style="gap:.4rem"><cl:avatar name="${t.buyerName}" image="${t.buyerImage}" size="xs" /><c:out value="${t.buyerName}" /></div></div>
                    <div><div class="k">Seller</div><div class="v row" style="gap:.4rem"><cl:avatar name="${t.sellerName}" image="${t.sellerImage}" size="xs" /><a href="${ctx}/seller?id=${t.sellerId}"><c:out value="${t.sellerName}" /></a></div></div>
                    <div><div class="k">Location</div><div class="v"><c:out value="${t.exchangeLocation}" /></div></div>
                    <div><div class="k">Date</div><div class="v"><c:out value="${t.exchangeLongDateLabel}" /></div></div>
                    <div><div class="k">Time</div><div class="v"><c:out value="${t.exchangeTimeLabel}" /></div></div>
                    <div><div class="k">Amount</div><div class="v"><c:out value="${t.amountLabel}" /></div></div>
                    <div><div class="k">Requested</div><div class="v"><c:out value="${t.transactionDateLabel}" /></div></div>
                    <c:if test="${not empty t.confirmedAt}"><div><div class="k">Confirmed</div><div class="v"><c:out value="${t.confirmedAtLabel}" /></div></div></c:if>
                    <c:if test="${not empty t.completedAt}"><div><div class="k">Paid</div><div class="v"><c:out value="${t.completedAtLabel}" /></div></div></c:if>
                </div>

                <c:if test="${not empty t.exchangeNote}">
                    <div>
                        <div class="k text-xs muted fw-bold" style="letter-spacing:.06em;text-transform:uppercase">Note from <c:out value="${t.buyerFirstName}" /></div>
                        <blockquote class="note-quote mt-1"><c:out value="${t.exchangeNote}" /></blockquote>
                    </div>
                </c:if>
            </div>
        </div>

        <aside class="stack">
            <div class="card card-pad">
                <c:choose>
                    <%-- ================= BUYER ================= --%>
                    <c:when test="${view.buyer and t.awaitingSeller}">
                        <h3>Waiting for <c:out value="${t.sellerFirstName}" /></h3>
                        <p class="text-sm muted">Your request is with the seller. You'll be able to pay here as soon as they confirm. Nothing has been charged.</p>
                        <form method="post" action="${ctx}/exchange/cancel" data-confirm="Cancel this exchange request?">
                            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="id" value="${t.id}">
                            <button type="submit" class="btn btn-danger btn-block">Cancel request</button>
                        </form>
                    </c:when>

                    <c:when test="${view.buyer and t.awaitingPayment}">
                        <h3>Confirmed &mdash; ready to pay</h3>
                        <p class="text-sm muted"><c:out value="${t.sellerFirstName}" /> confirmed the exchange and reserved the item for you.</p>
                        <div class="summary-card">
                            <div class="summary-row"><span>Amount</span><strong class="ink"><c:out value="${t.amountLabel}" /></strong></div>
                            <div class="summary-row"><span>Your wallet</span><span><c:out value="${view.viewerBalanceLabel}" /></span></div>
                            <c:if test="${view.canAfford}">
                                <div class="summary-row summary-total"><span>After payment</span><span><c:out value="${view.balanceAfterPaymentLabel}" /></span></div>
                            </c:if>
                        </div>
                        <c:choose>
                            <c:when test="${view.canAfford}">
                                <form class="mt-2" method="post" action="${ctx}/pay" data-confirm="Pay ${fn:escapeXml(t.amountLabel)} from your wallet to ${fn:escapeXml(t.sellerName)}?">
                                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="id" value="${t.id}">
                                    <button type="submit" class="btn btn-primary btn-lg btn-block" data-busy="Processing…">Pay <c:out value="${t.amountLabel}" /></button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <div class="alert alert-warning mt-2"><p>Your wallet is <strong><c:out value="${view.shortfallLabel}" /></strong> short. Add money, then come back to pay.</p></div>
                                <a class="btn btn-primary btn-block mt-1" href="${ctx}/wallet">Add money to wallet</a>
                            </c:otherwise>
                        </c:choose>
                        <form class="mt-1" method="post" action="${ctx}/exchange/cancel" data-confirm="Cancel this confirmed exchange? The item will be released.">
                            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="id" value="${t.id}">
                            <button type="submit" class="btn btn-ghost btn-block">Cancel exchange</button>
                        </form>
                    </c:when>

                    <c:when test="${view.buyer and t.completed}">
                        <h3>Paid &mdash; enjoy your item!</h3>
                        <p class="text-sm muted">You paid <c:out value="${t.amountLabel}" /> on <c:out value="${t.completedAtLabel}" />. Meet at <strong class="ink"><c:out value="${t.exchangeLocation}" /></strong> to collect it.</p>
                        <c:choose>
                            <c:when test="${t.reviewable}"><a class="btn btn-primary btn-block" href="${ctx}/review?transactionId=${t.id}">Review <c:out value="${t.sellerFirstName}" /></a></c:when>
                            <c:otherwise><p class="text-sm green fw-bold mb-0">&#10003; You reviewed this seller. Thank you!</p></c:otherwise>
                        </c:choose>
                    </c:when>

                    <%-- ================= SELLER ================= --%>
                    <c:when test="${view.seller and t.awaitingSeller}">
                        <h3>Confirm this exchange?</h3>
                        <p class="text-sm muted">Check that you can meet <c:out value="${t.buyerFirstName}" /> at this place and time. Confirming reserves the item for them and closes other requests. You're paid when they pay.</p>
                        <form method="post" action="${ctx}/exchange/confirm">
                            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="id" value="${t.id}">
                            <button type="submit" class="btn btn-primary btn-block" data-busy="Confirming…">Confirm exchange</button>
                        </form>
                        <form class="mt-1" method="post" action="${ctx}/exchange/reject" data-confirm="Decline this exchange request?">
                            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="id" value="${t.id}">
                            <button type="submit" class="btn btn-danger btn-block">Decline</button>
                        </form>
                    </c:when>

                    <c:when test="${view.seller and t.awaitingPayment}">
                        <h3>Waiting for payment</h3>
                        <p class="text-sm muted">You confirmed this exchange and the item is reserved for <c:out value="${t.buyerFirstName}" />. <c:out value="${t.amountLabel}" /> will be credited to your wallet when they pay.</p>
                        <form method="post" action="${ctx}/exchange/reject" data-confirm="Release this reservation? The item will be available to others again.">
                            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="id" value="${t.id}">
                            <button type="submit" class="btn btn-danger btn-block">Release reservation</button>
                        </form>
                    </c:when>

                    <c:when test="${view.seller and t.completed}">
                        <h3>Sold!</h3>
                        <p class="text-sm muted"><c:out value="${t.amountLabel}" /> was credited to your wallet on <c:out value="${t.completedAtLabel}" />. Hand the item over at <strong class="ink"><c:out value="${t.exchangeLocation}" /></strong>.</p>
                        <a class="btn btn-outline btn-block" href="${ctx}/wallet">View wallet</a>
                    </c:when>

                    <%-- ================= CLOSED ================= --%>
                    <c:otherwise>
                        <h3><c:out value="${t.status == 'REJECTED' ? 'Request declined' : 'Request cancelled'}" /></h3>
                        <p class="text-sm muted">This exchange was closed without a sale. Nobody was charged.</p>
                        <c:if test="${view.buyer}"><a class="btn btn-primary btn-block" href="${ctx}/market">Browse other items</a></c:if>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="card card-pad">
                <p class="sim-note">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2l8 4v6c0 5-3.5 8.5-8 10-4.5-1.5-8-5-8-10V6z"/><polyline points="8.5 12 11 14.5 15.5 9.5"/></svg>
                    <span>Meet in a busy public place, check the item before paying, and keep the details of your exchange on CampusLoop.</span>
                </p>
            </div>
        </aside>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

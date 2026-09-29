<%-- Checkout / exchange details. Model: checkout (CheckoutView), form (ExchangeForm), errors, formError. --%>
<c:set var="item" value="${checkout.listing}" />
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<div class="container">
    <div class="page-head">
        <div>
            <span class="eyebrow">Checkout</span>
            <h1>Propose an exchange</h1>
            <p class="lead">Tell <c:out value="${item.sellerFirstName}" /> where and when you'd like to meet on campus.</p>
        </div>
        <a class="btn btn-ghost" href="${ctx}/cart">Back to cart</a>
    </div>

    <ol class="steps" aria-label="Exchange progress">
        <li class="step current"><span class="dot">1</span>Exchange details</li>
        <li class="step"><span class="dot">2</span>Seller confirms</li>
        <li class="step"><span class="dot">3</span>Payment</li>
        <li class="step"><span class="dot">4</span>Completed</li>
    </ol>

    <div class="split mb-3">
        <form class="card card-pad-lg form" method="post" action="${ctx}/checkout" data-validate>
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
            <input type="hidden" name="listingId" value="${item.id}">

            <c:if test="${not empty formError}">
                <div class="alert alert-error" role="alert">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="7.5" x2="12" y2="13"/><circle cx="12" cy="16.5" r=".6" fill="currentColor"/></svg>
                    <p><c:out value="${formError}" /></p>
                </div>
            </c:if>

            <div class="field${not empty errors.location ? ' has-error' : ''}">
                <label class="label" for="location">Exchange location</label>
                <input class="input" id="location" name="location" list="campus-locations" value="${fn:escapeXml(form.location)}"
                       required minlength="3" maxlength="120" placeholder="e.g. Central Library Entrance"
                       data-msg-required="Please choose where on campus to meet.">
                <datalist id="campus-locations">
                    <c:forEach var="loc" items="${checkout.suggestedLocations}"><option value="${fn:escapeXml(loc)}"></option></c:forEach>
                </datalist>
                <p class="hint">Pick a suggestion or type any public, well-lit spot on campus.</p>
                <c:if test="${not empty errors.location}"><p class="field-error"><c:out value="${errors.location}" /></p></c:if>
            </div>

            <div class="form-row">
                <div class="field${not empty errors.date ? ' has-error' : ''}">
                    <label class="label" for="date">Date</label>
                    <input class="input" type="date" id="date" name="date" value="${fn:escapeXml(form.date)}" required
                           min="${checkout.minDate}" max="${checkout.maxDate}"
                           data-min-date="${checkout.minDate}" data-max-date="${checkout.maxDate}"
                           data-msg-required="Please choose a date.">
                    <p class="hint">Within the next 30 days.</p>
                    <c:if test="${not empty errors.date}"><p class="field-error"><c:out value="${errors.date}" /></p></c:if>
                </div>
                <div class="field${not empty errors.time ? ' has-error' : ''}">
                    <label class="label" for="time">Time</label>
                    <input class="input" type="time" id="time" name="time" value="${fn:escapeXml(form.time)}" required
                           min="${checkout.earliestTime}" max="${checkout.latestTime}" step="900"
                           data-min-time="${checkout.earliestTime}" data-max-time="${checkout.latestTime}"
                           data-msg-required="Please choose a time.">
                    <p class="hint">Between 7:00 AM and 9:30 PM.</p>
                    <c:if test="${not empty errors.time}"><p class="field-error"><c:out value="${errors.time}" /></p></c:if>
                </div>
            </div>

            <div class="field${not empty errors.note ? ' has-error' : ''}">
                <label class="label" for="note">Note for the seller <span class="optional">(optional)</span></label>
                <textarea class="textarea" id="note" name="note" maxlength="300" data-counter style="min-height:96px"
                          placeholder="e.g. I'll be wearing a black backpack."><c:out value="${form.note}" /></textarea>
                <p class="counter"></p>
                <c:if test="${not empty errors.note}"><p class="field-error"><c:out value="${errors.note}" /></p></c:if>
            </div>

            <div class="form-actions">
                <button type="submit" class="btn btn-primary btn-lg" data-busy="Sending…">Send exchange request</button>
                <span class="text-sm muted">You won't be charged now.</span>
            </div>
        </form>

        <aside class="stack">
            <div class="card card-pad summary-card">
                <h3>Order summary</h3>
                <div class="item-summary mt-2">
                    <img src="${ctx}/${fn:escapeXml(item.imageUrl)}" alt="">
                    <div>
                        <div class="t"><c:out value="${item.title}" /></div>
                        <div class="text-sm muted"><c:out value="${item.conditionLabel}" /> &middot; <c:out value="${item.categoryName}" /></div>
                        <div class="row mt-1" style="gap:.4rem">
                            <cl:avatar name="${item.sellerName}" image="${item.sellerImage}" size="xs" />
                            <span class="text-sm"><c:out value="${item.sellerName}" /></span>
                            <cl:stars rating="${checkout.sellerRating.average}" count="${checkout.sellerRating.count}" />
                        </div>
                    </div>
                </div>
                <div class="summary-row summary-total mt-2"><span>Price</span><span><c:out value="${item.priceLabel}" /></span></div>
                <div class="summary-row text-sm"><span class="muted">Your wallet</span><span><c:out value="${checkout.walletLabel}" /></span></div>
                <c:if test="${not checkout.canAfford}">
                    <div class="alert alert-warning mt-1">
                        <p>You'll need <strong><c:out value="${checkout.shortfallLabel}" /></strong> more before paying. You can still send the request and <a href="${ctx}/wallet">top up</a> while the seller confirms.</p>
                    </div>
                </c:if>
            </div>
            <div class="card card-pad">
                <h3>What happens next</h3>
                <ol class="text-sm muted" style="padding-left:1.1rem;margin:0">
                    <li>The seller sees your proposed place and time under Sales.</li>
                    <li>If they confirm, the item is reserved for you.</li>
                    <li>You pay from your wallet on the exchange page, then meet up.</li>
                </ol>
            </div>
        </aside>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

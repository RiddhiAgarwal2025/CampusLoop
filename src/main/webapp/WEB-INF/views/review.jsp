<%-- Review a seller. Model: t (completed, unreviewed Transaction), rating, comment, errors, formError. --%>
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<div class="container">
    <div class="page-head">
        <div>
            <span class="eyebrow">Seller review</span>
            <h1>How was your exchange with <c:out value="${t.sellerFirstName}" />?</h1>
            <p class="lead">Your review appears on <c:out value="${t.sellerFirstName}" />'s profile and helps other students buy with confidence.</p>
        </div>
        <a class="btn btn-ghost" href="${ctx}/purchases">Back to purchases</a>
    </div>

    <div class="split mb-3">
        <form class="card card-pad-lg form" method="post" action="${ctx}/review" data-validate>
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
            <input type="hidden" name="transactionId" value="${t.id}">

            <c:if test="${not empty formError}">
                <div class="alert alert-error" role="alert"><p><c:out value="${formError}" /></p></div>
            </c:if>

            <div class="item-summary">
                <img src="${ctx}/${fn:escapeXml(t.listingImage)}" alt="">
                <div>
                    <div class="t"><c:out value="${t.listingTitle}" /></div>
                    <div class="text-sm muted">Bought from <c:out value="${t.sellerName}" /> for <c:out value="${t.amountLabel}" /> &middot; <c:out value="${t.completedAtLabel}" /></div>
                </div>
            </div>

            <div class="field${not empty errors.rating ? ' has-error' : ''}">
                <span class="label">Your rating</span>
                <div class="star-input" role="radiogroup" aria-label="Rating">
                    <c:forEach var="s" begin="1" end="5">
                        <c:set var="v" value="${6 - s}" />
                        <input type="radio" id="star${v}" name="rating" value="${v}"${rating == fn:trim(v) ? ' checked' : ''} required
                               data-msg-required="Please choose a rating from 1 to 5 stars.">
                        <label for="star${v}" title="${v} ${v == 1 ? 'star' : 'stars'}">
                            <svg viewBox="0 0 24 24" fill="currentColor"><polygon points="12,2.5 14.9,8.6 21.5,9.4 16.6,13.9 17.9,20.5 12,17.2 6.1,20.5 7.4,13.9 2.5,9.4 9.1,8.6"/></svg>
                            <span class="sr-only"><c:out value="${v}" /> ${v == 1 ? 'star' : 'stars'}</span>
                        </label>
                    </c:forEach>
                </div>
                <p class="hint">5 = excellent &middot; 1 = poor</p>
                <c:if test="${not empty errors.rating}"><p class="field-error"><c:out value="${errors.rating}" /></p></c:if>
            </div>

            <div class="field${not empty errors.comment ? ' has-error' : ''}">
                <label class="label" for="comment">Comment</label>
                <textarea class="textarea" id="comment" name="comment" required minlength="10" maxlength="500" data-counter
                          placeholder="Was the item as described? Was the seller on time and friendly?"
                          data-msg-required="Please write a short comment about the exchange."><c:out value="${comment}" /></textarea>
                <p class="counter"></p>
                <c:if test="${not empty errors.comment}"><p class="field-error"><c:out value="${errors.comment}" /></p></c:if>
            </div>

            <div class="form-actions">
                <button type="submit" class="btn btn-primary btn-lg" data-busy="Posting…">Post review</button>
            </div>
        </form>

        <aside class="card card-pad">
            <h3>Reviewing fairly</h3>
            <ul class="text-sm muted" style="padding-left:1.1rem;margin:0">
                <li>Rate the seller and the exchange: accuracy of the listing, punctuality and communication.</li>
                <li>Only buyers of completed, paid exchanges can review, once per purchase.</li>
                <li>Reviews are public and can't be edited, so keep them honest and respectful.</li>
            </ul>
        </aside>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

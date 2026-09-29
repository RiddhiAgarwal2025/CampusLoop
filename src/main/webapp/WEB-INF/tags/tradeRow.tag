<%@ tag description="One exchange row for Purchases (asBuyer=true) or Sales (asBuyer=false)" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="cl" tagdir="/WEB-INF/tags" %>
<%@ attribute name="t" required="true" type="com.campusloop.model.Transaction" %>
<%@ attribute name="asBuyer" required="true" type="java.lang.Boolean" %>
<%@ attribute name="highlight" required="false" type="java.lang.Boolean" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<div class="list-item${highlight ? ' highlight' : ''}">
    <a href="${base}/exchange?id=${t.id}"><img class="list-thumb" src="${base}/${fn:escapeXml(t.listingImage)}" alt="${fn:escapeXml(t.listingTitle)}" loading="lazy"></a>
    <div class="list-body">
        <div class="row" style="gap:.5rem">
            <cl:statusBadge tone="${t.statusTone}" label="${t.statusLabel}" />
            <span class="text-xs muted">#<c:out value="${t.id}" /></span>
        </div>
        <p class="list-title"><a href="${base}/exchange?id=${t.id}"><c:out value="${t.listingTitle}" /></a></p>
        <div class="list-meta">
            <c:choose>
                <c:when test="${asBuyer}"><span><cl:avatar name="${t.sellerName}" image="${t.sellerImage}" size="xs" />Seller: <c:out value="${t.sellerName}" /></span></c:when>
                <c:otherwise><span><cl:avatar name="${t.buyerName}" image="${t.buyerImage}" size="xs" />Buyer: <c:out value="${t.buyerName}" /></span></c:otherwise>
            </c:choose>
            <span><c:choose><c:when test="${t.completed}">Paid <c:out value="${t.completedAtLabel}" /></c:when><c:otherwise>Requested <c:out value="${t.transactionDateLabel}" /></c:otherwise></c:choose></span>
        </div>
        <div class="exchange-box">
            <span><strong class="ink"><c:out value="${t.exchangeLocation}" /></strong> &middot; <c:out value="${t.exchangeDateLabel}" /> &middot; <c:out value="${t.exchangeTimeLabel}" /></span>
            <c:if test="${not empty t.exchangeNote}"><span class="note">&ldquo;<c:out value="${t.exchangeNote}" />&rdquo;</span></c:if>
        </div>
    </div>
    <div class="list-side">
        <span class="list-price"><c:out value="${t.amountLabel}" /></span>
        <div class="list-actions">
            <c:choose>
                <c:when test="${asBuyer and t.awaitingPayment}">
                    <a class="btn btn-primary btn-sm" href="${base}/exchange?id=${t.id}">Pay now</a>
                </c:when>
                <c:when test="${asBuyer and t.reviewable}">
                    <a class="btn btn-primary btn-sm" href="${base}/review?transactionId=${t.id}">Review seller</a>
                    <a class="btn btn-outline btn-sm" href="${base}/exchange?id=${t.id}">Details</a>
                </c:when>
                <c:when test="${asBuyer and t.completed}">
                    <span class="badge badge-success badge-plain">&#10003; Reviewed</span>
                    <a class="btn btn-outline btn-sm" href="${base}/exchange?id=${t.id}">Details</a>
                </c:when>
                <c:when test="${not asBuyer and t.awaitingSeller}">
                    <form class="inline-form" method="post" action="${base}/exchange/confirm">
                        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="id" value="${t.id}">
                        <button type="submit" class="btn btn-primary btn-sm">Confirm</button>
                    </form>
                    <form class="inline-form" method="post" action="${base}/exchange/reject" data-confirm="Decline this exchange request from ${fn:escapeXml(t.buyerName)}?">
                        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="id" value="${t.id}">
                        <button type="submit" class="btn btn-danger btn-sm">Reject</button>
                    </form>
                </c:when>
                <c:otherwise>
                    <a class="btn btn-outline btn-sm" href="${base}/exchange?id=${t.id}">Details</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

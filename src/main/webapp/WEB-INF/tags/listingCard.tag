<%@ tag description="Marketplace product card" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="cl" tagdir="/WEB-INF/tags" %>
<%@ attribute name="listing" required="true" type="com.campusloop.model.Listing" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<article class="product-card${listing.available ? '' : ' is-unavailable'}">
    <div class="product-media">
        <img src="${base}/${fn:escapeXml(listing.imageUrl)}" alt="${fn:escapeXml(listing.title)}" loading="lazy">
        <cl:statusBadge tone="${listing.status.tone}" label="${listing.status.label}" />
        <span class="cat-tag"><c:out value="${listing.categoryName}" /></span>
    </div>
    <div class="product-body">
        <h3 class="product-title"><a href="${base}/product?id=${listing.id}"><c:out value="${listing.title}" /></a></h3>
        <div class="product-meta">
            <span class="product-price"><c:out value="${listing.priceLabel}" /></span>
            <span class="condition"><c:out value="${listing.conditionLabel}" /></span>
        </div>
        <div class="seller-line">
            <cl:avatar name="${listing.sellerName}" image="${listing.sellerImage}" size="xs" />
            <span class="name"><c:out value="${listing.sellerFirstName}" /></span>
            <cl:stars rating="${listing.sellerRating}" count="${listing.sellerReviewCount}" />
        </div>
    </div>
</article>

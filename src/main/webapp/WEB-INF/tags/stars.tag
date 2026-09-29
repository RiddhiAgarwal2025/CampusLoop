<%@ tag description="Seller rating computed from real reviews; 'New seller' when there are none" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="rating" required="true" type="java.lang.Double" %>
<%@ attribute name="count" required="true" type="java.lang.Integer" %>
<%@ attribute name="showCount" required="false" type="java.lang.Boolean" %>
<c:choose>
<c:when test="${count > 0}">
<span class="stars" title="Rated ${rating} out of 5 from ${count} ${count == 1 ? 'review' : 'reviews'}">
<span class="star-track" aria-hidden="true"><svg class="star-empty" viewBox="0 0 80 16" fill="currentColor"><polygon points="8,1 10.1,5.6 15,6.2 11.4,9.5 12.4,14.4 8,12 3.6,14.4 4.6,9.5 1,6.2 5.9,5.6"/><polygon transform="translate(16 0)" points="8,1 10.1,5.6 15,6.2 11.4,9.5 12.4,14.4 8,12 3.6,14.4 4.6,9.5 1,6.2 5.9,5.6"/><polygon transform="translate(32 0)" points="8,1 10.1,5.6 15,6.2 11.4,9.5 12.4,14.4 8,12 3.6,14.4 4.6,9.5 1,6.2 5.9,5.6"/><polygon transform="translate(48 0)" points="8,1 10.1,5.6 15,6.2 11.4,9.5 12.4,14.4 8,12 3.6,14.4 4.6,9.5 1,6.2 5.9,5.6"/><polygon transform="translate(64 0)" points="8,1 10.1,5.6 15,6.2 11.4,9.5 12.4,14.4 8,12 3.6,14.4 4.6,9.5 1,6.2 5.9,5.6"/></svg><span class="star-fill" style="width:${rating * 20}%"><svg class="star-full" viewBox="0 0 80 16" fill="currentColor"><polygon points="8,1 10.1,5.6 15,6.2 11.4,9.5 12.4,14.4 8,12 3.6,14.4 4.6,9.5 1,6.2 5.9,5.6"/><polygon transform="translate(16 0)" points="8,1 10.1,5.6 15,6.2 11.4,9.5 12.4,14.4 8,12 3.6,14.4 4.6,9.5 1,6.2 5.9,5.6"/><polygon transform="translate(32 0)" points="8,1 10.1,5.6 15,6.2 11.4,9.5 12.4,14.4 8,12 3.6,14.4 4.6,9.5 1,6.2 5.9,5.6"/><polygon transform="translate(48 0)" points="8,1 10.1,5.6 15,6.2 11.4,9.5 12.4,14.4 8,12 3.6,14.4 4.6,9.5 1,6.2 5.9,5.6"/><polygon transform="translate(64 0)" points="8,1 10.1,5.6 15,6.2 11.4,9.5 12.4,14.4 8,12 3.6,14.4 4.6,9.5 1,6.2 5.9,5.6"/></svg></span></span>
<strong>${rating}</strong><c:if test="${showCount}"><span>(${count})</span></c:if>
<span class="sr-only">Rated ${rating} out of 5 from ${count} ${count == 1 ? 'review' : 'reviews'}</span>
</span>
</c:when>
<c:otherwise><span class="stars"><em class="new-seller">New seller</em></span></c:otherwise>
</c:choose>

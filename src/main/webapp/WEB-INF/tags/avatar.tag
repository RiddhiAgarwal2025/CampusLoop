<%@ tag description="Profile photo, or initials when the student has no photo" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ attribute name="name" required="true" type="java.lang.String" %>
<%@ attribute name="image" required="false" type="java.lang.String" %>
<%@ attribute name="size" required="false" type="java.lang.String" description="xs | sm | md | lg | xl (default: 36px)" %>
<c:set var="parts" value="${fn:split(fn:trim(name), ' ')}" />
<c:set var="first" value="${fn:length(parts) > 0 ? fn:toUpperCase(fn:substring(parts[0], 0, 1)) : '?'}" />
<c:set var="last" value="${fn:length(parts) > 1 ? fn:toUpperCase(fn:substring(parts[fn:length(parts) - 1], 0, 1)) : ''}" />
<span class="avatar${empty size ? '' : ' avatar-'.concat(size)}" aria-hidden="true"><c:choose><c:when test="${not empty image}"><img src="${pageContext.request.contextPath}/${fn:escapeXml(image)}" alt="" loading="lazy"></c:when><c:otherwise><c:out value="${first}${last}" /></c:otherwise></c:choose></span>

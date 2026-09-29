<%-- Document head + site header. Set request attributes before including:
     pageTitle, bodyClass (optional), activeNav (browse|sell|how), inlineFlash (auth pages). --%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:choose><c:when test="${not empty pageTitle}"><c:out value="${pageTitle}" /> &middot; CampusLoop</c:when><c:otherwise>CampusLoop &middot; Good Stuff. Second Chances.</c:otherwise></c:choose></title>
    <meta name="description" content="CampusLoop is a peer-to-peer campus marketplace for used books, electronics, calculators and hostel essentials. Buy less new. Give more a second life.">
    <meta name="theme-color" content="#0F1B2D">
    <link rel="icon" type="image/svg+xml" href="${ctx}/assets/images/favicon.svg">
    <link rel="stylesheet" href="${ctx}/assets/app.css?v=1.0.0">
    <script src="${ctx}/assets/js/app.js?v=1.0.0" defer></script>
</head>
<body class="${fn:escapeXml(bodyClass)}">
<a class="skip-link" href="#main">Skip to content</a>
<jsp:include page="/WEB-INF/views/common/nav.jsp" />
<main id="main">
<c:if test="${not inlineFlash}">
    <jsp:include page="/WEB-INF/views/common/flash.jsp" />
</c:if>

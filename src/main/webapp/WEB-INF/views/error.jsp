<%-- Friendly error page for 400/403/404/405/413/500 and uncaught exceptions.
     Never shows stack traces or exception messages. --%>
<c:set var="code" value="${requestScope['javax.servlet.error.status_code']}" />
<c:if test="${empty code}"><c:set var="code" value="500" /></c:if>
<c:choose>
    <c:when test="${code == 404}">
        <c:set var="title" value="We couldn't find that page" />
        <c:set var="message" value="The page or item you're looking for doesn't exist, or it may have been removed by its seller." />
    </c:when>
    <c:when test="${code == 403}">
        <c:set var="title" value="You don't have access to this" />
        <c:set var="message" value="This page belongs to another student's account, so it isn't available to you." />
    </c:when>
    <c:when test="${code == 405}">
        <c:set var="title" value="That action isn't available here" />
        <c:set var="message" value="Please use the buttons on CampusLoop pages to perform this action." />
    </c:when>
    <c:when test="${code == 413}">
        <c:set var="title" value="That upload is too large" />
        <c:set var="message" value="Please choose a JPG, PNG or WebP image up to ${maxUploadMb} MB and try again." />
    </c:when>
    <c:when test="${code == 400}">
        <c:set var="title" value="Something about that request wasn't right" />
        <c:set var="message" value="Please go back and try again." />
    </c:when>
    <c:otherwise>
        <c:set var="code" value="500" />
        <c:set var="title" value="Something went wrong on our side" />
        <c:set var="message" value="We couldn't complete that request. Nothing was charged. Please try again in a moment." />
    </c:otherwise>
</c:choose>
<c:set var="pageTitle" value="${title}" scope="request" />
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<section class="container error-page">
    <div class="error-code"><c:out value="${code}" /></div>
    <h1><c:out value="${title}" /></h1>
    <p><c:out value="${message}" /></p>
    <div class="row">
        <a class="btn btn-primary" href="${ctx}/home">Go to home</a>
        <a class="btn btn-outline" href="${ctx}/market">Browse marketplace</a>
        <c:if test="${not empty sessionScope.authUser}">
            <a class="btn btn-ghost" href="${ctx}/dashboard">Back to dashboard</a>
        </c:if>
    </div>
</section>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

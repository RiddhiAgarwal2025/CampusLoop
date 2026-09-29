<%-- One-time messages stored in the session by SessionUtil.flashSuccess/flashError.
     Rendered once, then removed. requestScope.inlineFlash renders without the page container. --%>
<c:if test="${not empty sessionScope.flashSuccess or not empty sessionScope.flashError}">
    <div class="${inlineFlash ? 'stack-sm mb-2' : 'container flash-wrap stack-sm'}">
        <c:if test="${not empty sessionScope.flashSuccess}">
            <div class="alert alert-success" role="status">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="8 12.5 11 15.5 16 9.5"/></svg>
                <p><c:out value="${sessionScope.flashSuccess}" /></p>
                <button type="button" class="alert-close" aria-label="Dismiss">&times;</button>
            </div>
        </c:if>
        <c:if test="${not empty sessionScope.flashError}">
            <div class="alert alert-error" role="alert">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="7.5" x2="12" y2="13"/><circle cx="12" cy="16.5" r=".6" fill="currentColor"/></svg>
                <p><c:out value="${sessionScope.flashError}" /></p>
                <button type="button" class="alert-close" aria-label="Dismiss">&times;</button>
            </div>
        </c:if>
    </div>
    <c:remove var="flashSuccess" scope="session" />
    <c:remove var="flashError" scope="session" />
</c:if>

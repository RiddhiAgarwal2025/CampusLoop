<%-- Sign in. Model: email, remember, errors (map), formError. --%>
<c:set var="inlineFlash" value="${true}" scope="request" />
<c:set var="bodyClass" value="page-auth" scope="request" />
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<div class="auth-shell">
    <aside class="auth-panel">
        <span class="eyebrow">Good stuff. Second chances.</span>
        <h2>Buy less new.<br><span>Give more a second life.</span></h2>
        <p>Pick up where you left off: your cart, your listings and your exchanges are waiting.</p>
        <ul class="auth-points">
            <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="5 12.5 10 17.5 19 7.5"/></svg><span>Meet on campus. You choose the place and time.</span></li>
            <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="5 12.5 10 17.5 19 7.5"/></svg><span>Nothing is charged until the seller confirms the exchange.</span></li>
            <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="5 12.5 10 17.5 19 7.5"/></svg><span>Every completed resale earns Impact Points for both students.</span></li>
        </ul>
    </aside>

    <section class="auth-main">
        <div class="auth-card">
            <h1>Welcome back</h1>
            <p class="lead">Sign in to your CampusLoop account.</p>

            <jsp:include page="/WEB-INF/views/common/flash.jsp" />
            <c:if test="${not empty formError}">
                <div class="alert alert-error mb-2" role="alert">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="7.5" x2="12" y2="13"/><circle cx="12" cy="16.5" r=".6" fill="currentColor"/></svg>
                    <p><c:out value="${formError}" /></p>
                </div>
            </c:if>

            <form class="form" method="post" action="${ctx}/login" data-validate>
                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">

                <div class="field${not empty errors.email ? ' has-error' : ''}">
                    <label class="label" for="email">Email address</label>
                    <input class="input" type="email" id="email" name="email" value="${fn:escapeXml(email)}"
                           autocomplete="email" maxlength="120" required autofocus
                           data-msg-required="Please enter your email address.">
                    <c:if test="${not empty errors.email}"><p class="field-error"><c:out value="${errors.email}" /></p></c:if>
                </div>

                <div class="field${not empty errors.password ? ' has-error' : ''}">
                    <label class="label" for="password">Password</label>
                    <div class="input-group">
                        <input class="input has-suffix" type="password" id="password" name="password"
                               autocomplete="current-password" maxlength="64" required
                               data-msg-required="Please enter your password.">
                        <button type="button" class="suffix-btn" data-toggle-password="#password" aria-pressed="false">Show</button>
                    </div>
                    <c:if test="${not empty errors.password}"><p class="field-error"><c:out value="${errors.password}" /></p></c:if>
                </div>

                <label class="check">
                    <input type="checkbox" name="remember" value="1"${remember ? ' checked' : ''}>
                    <span>Remember my email on this device</span>
                </label>

                <button type="submit" class="btn btn-primary btn-lg btn-block" data-busy="Signing in…">Sign in</button>
                <p class="form-footnote">New to CampusLoop? <a href="${ctx}/register">Create an account</a></p>
            </form>

            <div class="demo-box">
                <strong>Demo accounts</strong> (password <code>password123</code>)<br>
                <code>riddhi@campusloop.local</code> &middot; <code>aarav@campusloop.local</code> &middot; <code>ananya@campusloop.local</code>
            </div>
        </div>
    </section>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

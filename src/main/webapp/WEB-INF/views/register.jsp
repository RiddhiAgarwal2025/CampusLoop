<%-- Sign up. Model: form (name, email), errors (map), formError. --%>
<c:set var="inlineFlash" value="${true}" scope="request" />
<c:set var="bodyClass" value="page-auth" scope="request" />
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<div class="auth-shell">
    <aside class="auth-panel">
        <span class="eyebrow">Join your campus loop</span>
        <h2>Good stuff.<br><span>Second chances.</span></h2>
        <p>Sell the textbooks, calculators and hostel gear you no longer need, and pick up what you do, from students on your own campus.</p>
        <ul class="auth-points">
            <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="5 12.5 10 17.5 19 7.5"/></svg><span>List an item in under a minute, with a real photo.</span></li>
            <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="5 12.5 10 17.5 19 7.5"/></svg><span>A CampusLoop wallet keeps payments simple.</span></li>
            <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="5 12.5 10 17.5 19 7.5"/></svg><span>Honest seller ratings from completed exchanges only.</span></li>
        </ul>
    </aside>

    <section class="auth-main">
        <div class="auth-card">
            <h1>Create your account</h1>
            <p class="lead">It's free and takes less than a minute.</p>

            <jsp:include page="/WEB-INF/views/common/flash.jsp" />
            <c:if test="${not empty formError}">
                <div class="alert alert-error mb-2" role="alert">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="7.5" x2="12" y2="13"/><circle cx="12" cy="16.5" r=".6" fill="currentColor"/></svg>
                    <p><c:out value="${formError}" /></p>
                </div>
            </c:if>

            <form class="form" method="post" action="${ctx}/register" data-validate>
                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">

                <div class="field${not empty errors.name ? ' has-error' : ''}">
                    <label class="label" for="name">Full name</label>
                    <input class="input" type="text" id="name" name="name" value="${fn:escapeXml(form.name)}"
                           autocomplete="name" minlength="2" maxlength="80" required autofocus
                           data-msg-required="Please enter your name.">
                    <c:if test="${not empty errors.name}"><p class="field-error"><c:out value="${errors.name}" /></p></c:if>
                </div>

                <div class="field${not empty errors.email ? ' has-error' : ''}">
                    <label class="label" for="email">Email address</label>
                    <input class="input" type="email" id="email" name="email" value="${fn:escapeXml(form.email)}"
                           autocomplete="email" maxlength="120" required
                           data-msg-required="Please enter your email address.">
                    <c:if test="${not empty errors.email}"><p class="field-error"><c:out value="${errors.email}" /></p></c:if>
                </div>

                <div class="field${not empty errors.password ? ' has-error' : ''}">
                    <label class="label" for="password">Password</label>
                    <div class="input-group">
                        <input class="input has-suffix" type="password" id="password" name="password"
                               autocomplete="new-password" maxlength="64" required data-password
                               data-msg-required="Please choose a password.">
                        <button type="button" class="suffix-btn" data-toggle-password="#password" aria-pressed="false">Show</button>
                    </div>
                    <p class="hint">At least 8 characters, including a letter and a number.</p>
                    <c:if test="${not empty errors.password}"><p class="field-error"><c:out value="${errors.password}" /></p></c:if>
                </div>

                <div class="field${not empty errors.confirmPassword ? ' has-error' : ''}">
                    <label class="label" for="confirmPassword">Confirm password</label>
                    <input class="input" type="password" id="confirmPassword" name="confirmPassword"
                           autocomplete="new-password" maxlength="64" required data-match="#password"
                           data-msg-required="Please confirm your password." data-msg-match="Passwords do not match.">
                    <c:if test="${not empty errors.confirmPassword}"><p class="field-error"><c:out value="${errors.confirmPassword}" /></p></c:if>
                </div>

                <button type="submit" class="btn btn-primary btn-lg btn-block" data-busy="Creating account…">Create account</button>
                <p class="form-footnote">Already have an account? <a href="${ctx}/login">Sign in</a></p>
            </form>
        </div>
    </section>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

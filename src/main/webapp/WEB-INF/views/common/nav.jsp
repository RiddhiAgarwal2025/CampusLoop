<%-- Site header. Uses sessionScope.authUser, sessionScope.csrfToken,
     requestScope.activeNav and requestScope.navCartCount. --%>
<c:set var="me" value="${sessionScope.authUser}" />
<header class="site-header">
    <div class="container nav">
        <a class="brand" href="${ctx}/home" aria-label="CampusLoop home">
            <img src="${ctx}/assets/images/logo.svg" alt="" width="34" height="34">
            <span>CAMPUS<span class="brand-accent">LOOP</span></span>
        </a>

        <button class="nav-toggle" type="button" aria-expanded="false" aria-label="Open menu">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><line x1="4" y1="7" x2="20" y2="7"/><line x1="4" y1="12" x2="20" y2="12"/><line x1="4" y1="17" x2="20" y2="17"/></svg>
        </button>

        <nav class="nav-links" aria-label="Main">
            <a class="nav-link${activeNav == 'browse' ? ' active' : ''}" href="${ctx}/market">Browse</a>
            <a class="nav-link${activeNav == 'sell' ? ' active' : ''}" href="${ctx}/sell">Sell</a>
            <a class="nav-link${activeNav == 'how' ? ' active' : ''}" href="${ctx}/how-it-works">How it works</a>
        </nav>

        <div class="nav-actions">
            <a class="cart-link" href="${ctx}/cart" title="Cart" aria-label="Cart">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M6 6h15l-1.5 9h-12z"/><path d="M6 6L5 3H2"/><circle cx="9" cy="20" r="1.5"/><circle cx="18" cy="20" r="1.5"/></svg>
                <span class="cart-label">Cart</span>
                <c:if test="${navCartCount > 0}"><span class="count-badge"><c:out value="${navCartCount}" /></span></c:if>
            </a>

            <c:choose>
                <c:when test="${not empty me}">
                    <details class="user-menu">
                        <summary>
                            <cl:avatar name="${me.name}" image="${me.profileImage}" size="sm" />
                            <span><c:out value="${me.firstName}" /></span>
                            <svg class="chev" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="6 9 12 15 18 9"/></svg>
                        </summary>
                        <div class="menu" role="menu">
                            <div class="menu-head">
                                <strong><c:out value="${me.name}" /></strong>
                                <span><c:out value="${me.email}" /></span>
                            </div>
                            <a href="${ctx}/dashboard" role="menuitem">Dashboard</a>
                            <a href="${ctx}/my-listings" role="menuitem">My listings</a>
                            <a href="${ctx}/purchases" role="menuitem">Purchases</a>
                            <a href="${ctx}/sales" role="menuitem">Sales</a>
                            <a href="${ctx}/wallet" role="menuitem">Wallet</a>
                            <a href="${ctx}/profile" role="menuitem">Profile</a>
                            <div class="menu-sep"></div>
                            <form method="post" action="${ctx}/logout">
                                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                <button type="submit" class="danger" role="menuitem">Sign out</button>
                            </form>
                        </div>
                    </details>
                </c:when>
                <c:otherwise>
                    <a class="btn btn-ghost btn-sm" href="${ctx}/login">Log in</a>
                    <a class="btn btn-primary btn-sm" href="${ctx}/register">Sign up</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>

<%-- Closes <main> opened in head.jsp, renders the footer and closes the document. --%>
</main>
<footer class="site-footer">
    <div class="container">
        <div class="footer-grid">
            <div>
                <a class="brand" href="${ctx}/home">
                    <img src="${ctx}/assets/images/logo.svg" alt="" width="30" height="30">
                    <span>CAMPUS<span class="brand-accent">LOOP</span></span>
                </a>
                <p class="footer-tagline">Good stuff. Second chances. A peer-to-peer marketplace that keeps useful things circulating on campus.</p>
            </div>
            <div>
                <h4>Marketplace</h4>
                <ul>
                    <li><a href="${ctx}/market">Browse items</a></li>
                    <li><a href="${ctx}/sell">Sell an item</a></li>
                    <li><a href="${ctx}/how-it-works">How it works</a></li>
                    <li><a href="${ctx}/cart">Cart</a></li>
                </ul>
            </div>
            <div>
                <h4>Your account</h4>
                <ul>
                    <c:choose>
                        <c:when test="${not empty sessionScope.authUser}">
                            <li><a href="${ctx}/dashboard">Dashboard</a></li>
                            <li><a href="${ctx}/wallet">Wallet</a></li>
                            <li><a href="${ctx}/purchases">Purchases</a></li>
                            <li><a href="${ctx}/profile">Profile</a></li>
                        </c:when>
                        <c:otherwise>
                            <li><a href="${ctx}/login">Log in</a></li>
                            <li><a href="${ctx}/register">Create an account</a></li>
                        </c:otherwise>
                    </c:choose>
                </ul>
            </div>
        </div>
        <div class="footer-bottom">
            <span>&copy; CampusLoop &middot; Buy less new. Give more a second life.</span>
            <span>Wallet payments are simulated for academic use.</span>
        </div>
    </div>
</footer>
</body>
</html>

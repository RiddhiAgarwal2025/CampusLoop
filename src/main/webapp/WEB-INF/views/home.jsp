<%-- Home. Model: home (HomeSummary). Every number is a live database count. --%>
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<section class="hero">
    <div class="container hero-inner">
        <div>
            <div class="hero-brand">CAMPUSLOOP</div>
            <h1 class="hero-title">Buy less new.<br>Give more a<br><span class="accent">second life.</span></h1>
            <p class="hero-sub">A marketplace built for your campus.</p>

            <form class="search-bar" method="get" action="${ctx}/market" role="search">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="7"/><line x1="16.5" y1="16.5" x2="21" y2="21"/></svg>
                <label class="sr-only" for="home-search">Search CampusLoop</label>
                <input id="home-search" type="search" name="q" maxlength="80" placeholder="Search textbooks, calculators, lamps…">
                <button class="btn btn-dark" type="submit">Search</button>
            </form>

            <div class="hero-actions">
                <a class="btn btn-primary btn-lg" href="${ctx}/market">Browse Marketplace</a>
                <a class="btn btn-outline btn-lg" href="${ctx}/sell">Sell an Item</a>
            </div>
        </div>

        <div class="hero-art">
            <img src="${ctx}/assets/images/hero-loop.svg" alt="" width="420" height="420">
            <div class="impact-pill">
                <span class="stat-icon green">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 11a8 8 0 0 0-14.3-4.9"/><polyline points="5 2.5 5.6 6.2 9.3 5.6"/><path d="M4 13a8 8 0 0 0 14.3 4.9"/><polyline points="19 21.5 18.4 17.8 14.7 18.4"/></svg>
                </span>
                <div>
                    <div class="big"><c:out value="${home.itemsRecirculated}" /></div>
                    <div class="small">Items Recirculated<br>through completed resales</div>
                </div>
            </div>
        </div>
    </div>
</section>

<section class="section">
    <div class="container">
        <div class="section-head">
            <div>
                <h2>Shop by category</h2>
                <p><c:out value="${home.availableListings}" /> items available right now from <c:out value="${home.students}" /> students.</p>
            </div>
        </div>
        <div class="category-strip">
            <c:forEach var="cat" items="${home.categories}">
                <c:url var="catUrl" value="/market"><c:param name="category" value="${cat.slug}" /></c:url>
                <a class="category-chip" href="${catUrl}"><c:out value="${cat.name}" /><span class="n"> <c:out value="${cat.availableCount}" /></span></a>
            </c:forEach>
        </div>
    </div>
</section>

<section class="section">
    <div class="container">
        <div class="section-head">
            <div>
                <h2>Trending on Campus</h2>
                <p>Available items that the most students have added to their carts.</p>
            </div>
            <a class="btn btn-outline btn-sm" href="${ctx}/market">View all items</a>
        </div>
        <c:choose>
            <c:when test="${empty home.trending}">
                <div class="card empty">
                    <div class="empty-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M3 7h18l-2 13H5z"/><path d="M8 7a4 4 0 0 1 8 0"/></svg></div>
                    <h3>No items listed yet</h3>
                    <p>Be the first to give something a second life on your campus.</p>
                    <a class="btn btn-primary" href="${ctx}/sell">Sell an Item</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="product-grid cols-4">
                    <c:forEach var="item" items="${home.trending}">
                        <cl:listingCard listing="${item}" />
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<section class="section">
    <div class="container">
        <div class="section-head">
            <div>
                <h2>How CampusLoop works</h2>
                <p>Meet on campus. Pay only after the seller confirms.</p>
            </div>
            <a class="btn btn-ghost btn-sm" href="${ctx}/how-it-works">Learn more &rarr;</a>
        </div>
        <div class="how-steps">
            <div class="card how-step"><h3>Find or list an item</h3><p>Browse by category or post what you no longer need with a real photo.</p></div>
            <div class="card how-step"><h3>Propose an exchange</h3><p>Pick a spot on campus, a date and a time that work for you.</p></div>
            <div class="card how-step"><h3>Seller confirms</h3><p>Nothing is charged until the seller accepts your meetup.</p></div>
            <div class="card how-step"><h3>Pay and meet</h3><p>Pay from your CampusLoop wallet, collect the item and rate the seller.</p></div>
        </div>
    </div>
</section>

<section class="section">
    <div class="container">
        <div class="band">
            <div>
                <span class="eyebrow">Your impact</span>
                <h2>Every resale keeps something useful in the loop.</h2>
                <p>Each completed resale earns <strong><c:out value="${pointsPerResale}" /> Impact Points</strong> for the buyer and <strong><c:out value="${pointsPerResale}" /></strong> for the seller. Points count real reuse on campus; they are not a carbon estimate.</p>
            </div>
            <div>
                <div class="band-stat"><c:out value="${home.itemsRecirculated}" /></div>
                <p>items recirculated through completed CampusLoop resales so far.</p>
            </div>
        </div>
    </div>
</section>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

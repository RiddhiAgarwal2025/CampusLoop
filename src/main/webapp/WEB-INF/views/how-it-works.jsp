<%-- How it works. Model: itemsRecirculated, impactRule. --%>
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<div class="container">
    <div class="page-head">
        <div>
            <span class="eyebrow">How it works</span>
            <h1>Good stuff, second chances, zero surprises.</h1>
            <p class="lead">CampusLoop is built around a simple, safe exchange: you meet on campus, and money only moves after both of you have agreed.</p>
        </div>
    </div>

    <section class="section-tight">
        <div class="how-steps">
            <div class="card how-step"><h3>List or find an item</h3><p>Sellers post a title, description, price, condition and a real photo. Buyers search and filter by category and price.</p></div>
            <div class="card how-step"><h3>Propose the exchange</h3><p>From your cart, choose a campus location, a date and a time, and add a note so the seller can recognise you.</p></div>
            <div class="card how-step"><h3>Seller confirms</h3><p>The seller sees your request under Sales and confirms or declines it. Nothing is charged before confirmation.</p></div>
            <div class="card how-step"><h3>Pay, meet, review</h3><p>After confirmation you pay from your wallet in one secure step. Then collect the item and rate the seller.</p></div>
        </div>
    </section>

    <section class="section">
        <div class="grid grid-3">
            <div class="card card-pad">
                <span class="stat-icon navy"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2.5" y="6" width="19" height="13" rx="2.5"/><path d="M16 12.5h2"/><path d="M2.5 9.5h19"/></svg></span>
                <h3 class="mt-2">The CampusLoop wallet</h3>
                <p class="muted mb-0">Top up your wallet, then pay for confirmed exchanges in one click. The seller is credited instantly and every movement is listed in your wallet activity. Top-ups are simulated for academic use; no real money or payment gateway is involved.</p>
            </div>
            <div class="card card-pad">
                <span class="stat-icon green"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 11a8 8 0 0 0-14.3-4.9"/><polyline points="5 2.5 5.6 6.2 9.3 5.6"/><path d="M4 13a8 8 0 0 0 14.3 4.9"/><polyline points="19 21.5 18.4 17.8 14.7 18.4"/></svg></span>
                <h3 class="mt-2">Impact Points</h3>
                <p class="muted mb-0"><c:out value="${impactRule}" /> Points are awarded automatically in the same step as payment. They measure participation in reuse on campus, not carbon or waste savings.</p>
            </div>
            <div class="card card-pad">
                <span class="stat-icon amber"><svg viewBox="0 0 24 24" fill="currentColor"><polygon points="12,2.5 14.9,8.6 21.5,9.4 16.6,13.9 17.9,20.5 12,17.2 6.1,20.5 7.4,13.9 2.5,9.4 9.1,8.6"/></svg></span>
                <h3 class="mt-2">Honest seller ratings</h3>
                <p class="muted mb-0">Only the buyer of a completed, paid exchange can review the seller, once per purchase. Ratings are the average of those real reviews; sellers without reviews are shown as new.</p>
            </div>
        </div>
    </section>

    <section class="section-tight">
        <div class="card">
            <div class="card-head"><h2>What each status means</h2></div>
            <div class="table-wrap">
                <table class="table">
                    <thead><tr><th>Status</th><th>What it means</th><th>Next step</th></tr></thead>
                    <tbody>
                        <tr><td><span class="badge badge-warning">Awaiting seller</span></td><td>You proposed a meetup; the seller hasn't answered yet.</td><td>The seller confirms or declines under Sales.</td></tr>
                        <tr><td><span class="badge badge-info">Awaiting payment</span></td><td>The seller confirmed and the item is reserved for you.</td><td>Pay from your wallet on the exchange page.</td></tr>
                        <tr><td><span class="badge badge-success">Completed</span></td><td>Payment done; the item is sold and Impact Points are awarded.</td><td>Meet up, collect the item and review the seller.</td></tr>
                        <tr><td><span class="badge badge-danger">Rejected</span></td><td>The seller declined or released the reservation.</td><td>Nothing was charged. Try another item.</td></tr>
                        <tr><td><span class="badge badge-muted">Cancelled</span></td><td>You cancelled, or the item went to another buyer or was removed.</td><td>Nothing was charged.</td></tr>
                    </tbody>
                </table>
            </div>
        </div>
    </section>

    <section class="section">
        <div class="band">
            <div>
                <span class="eyebrow">Meet safely</span>
                <h2>Good exchanges happen in busy places.</h2>
                <p>Choose well-lit, public spots on campus such as the library entrance or the main canteen, check the item before paying, and keep all communication about the exchange on CampusLoop.</p>
            </div>
            <div>
                <div class="band-stat"><c:out value="${itemsRecirculated}" /></div>
                <p>items recirculated so far. <a href="${ctx}/market" style="color:#7FD8B0">Find your next one &rarr;</a></p>
            </div>
        </div>
    </section>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

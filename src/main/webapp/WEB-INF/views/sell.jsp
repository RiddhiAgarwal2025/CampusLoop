<%-- Sell an item. Model: see common/listing-form.jspf. --%>
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<div class="container">
    <div class="page-head">
        <div>
            <span class="eyebrow">Sell</span>
            <h1>Give something a second life</h1>
            <p class="lead">List it in a minute. You'll only meet buyers after you confirm their exchange request.</p>
        </div>
    </div>

    <div class="split mb-3">
        <%@ include file="/WEB-INF/views/common/listing-form.jspf" %>

        <aside class="stack">
            <div class="card card-pad">
                <h3>Tips for a quick sale</h3>
                <ul class="text-sm muted" style="padding-left:1.1rem;margin:0">
                    <li>Use a bright, real photo of the actual item. Photos are required.</li>
                    <li>Mention the edition, model or size in the title.</li>
                    <li>Be honest about wear; accurate listings earn better seller reviews.</li>
                    <li>Check similar items in the marketplace to price fairly.</li>
                </ul>
            </div>
            <div class="card card-pad">
                <h3>What happens next</h3>
                <ol class="text-sm muted" style="padding-left:1.1rem;margin:0">
                    <li>Buyers add your item to their cart and propose a meetup.</li>
                    <li>You confirm or decline the request under <a href="${ctx}/sales">Sales</a>.</li>
                    <li>After the buyer pays, your wallet is credited instantly and you both earn <c:out value="${pointsPerResale}" /> Impact Points.</li>
                </ol>
            </div>
        </aside>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

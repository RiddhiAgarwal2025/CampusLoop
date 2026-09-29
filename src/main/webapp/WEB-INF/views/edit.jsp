<%-- Edit a listing. Model: listing (owned, AVAILABLE) + see common/listing-form.jspf. --%>
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<div class="container">
    <div class="page-head">
        <div>
            <span class="eyebrow">My listings</span>
            <h1>Edit listing</h1>
            <p class="lead"><c:out value="${listing.title}" /></p>
        </div>
        <div class="page-actions">
            <a class="btn btn-outline" href="${ctx}/product?id=${listing.id}">View listing</a>
            <a class="btn btn-ghost" href="${ctx}/my-listings">Back to my listings</a>
        </div>
    </div>

    <div class="split mb-3">
        <%@ include file="/WEB-INF/views/common/listing-form.jspf" %>

        <aside class="stack">
            <div class="card card-pad">
                <div class="row-between">
                    <h3 class="mb-0">Status</h3>
                    <cl:statusBadge tone="${listing.status.tone}" label="${listing.status.label}" />
                </div>
                <p class="text-sm muted mt-1 mb-0">Listed <c:out value="${listing.createdAtLabel}" />. Price changes apply to new exchange requests; requests already sent keep the price the buyer agreed to.</p>
            </div>
            <div class="card card-pad">
                <h3>Remove this listing</h3>
                <p class="text-sm muted">It disappears from the marketplace and from every cart. Any pending requests are cancelled; nobody is charged.</p>
                <form method="post" action="${ctx}/listing/remove" data-confirm="Remove this listing from CampusLoop? Pending requests will be cancelled.">
                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                    <input type="hidden" name="id" value="${listing.id}">
                    <button type="submit" class="btn btn-danger btn-block">Remove listing</button>
                </form>
            </div>
        </aside>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

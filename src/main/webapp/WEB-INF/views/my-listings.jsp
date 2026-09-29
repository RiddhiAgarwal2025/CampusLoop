<%-- My listings. Model: listings, tab, countAll/Available/Reserved/Sold, pendingCounts (listingId -> n). --%>
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<div class="container">
    <div class="page-head">
        <div>
            <h1>My listings</h1>
            <p class="lead">Everything you're selling on CampusLoop, and what's already found a new home.</p>
        </div>
        <div class="page-actions">
            <a class="btn btn-ghost" href="${ctx}/dashboard">Back to Dashboard</a>
            <a class="btn btn-primary" href="${ctx}/sell">Sell an item</a>
        </div>
    </div>

    <nav class="tabs" aria-label="Filter listings">
        <a class="tab${tab == 'all' ? ' active' : ''}" href="${ctx}/my-listings">All<span class="count"><c:out value="${countAll}" /></span></a>
        <a class="tab${tab == 'available' ? ' active' : ''}" href="${ctx}/my-listings?status=available">Available<span class="count"><c:out value="${countAvailable}" /></span></a>
        <a class="tab${tab == 'reserved' ? ' active' : ''}" href="${ctx}/my-listings?status=reserved">Reserved<span class="count"><c:out value="${countReserved}" /></span></a>
        <a class="tab${tab == 'sold' ? ' active' : ''}" href="${ctx}/my-listings?status=sold">Sold<span class="count"><c:out value="${countSold}" /></span></a>
    </nav>

    <c:choose>
        <c:when test="${empty listings}">
            <div class="card empty mb-3">
                <div class="empty-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.6 13.4l-7.2 7.2a2 2 0 0 1-2.8 0L3 13V3h10l7.6 7.6a2 2 0 0 1 0 2.8z"/><circle cx="7.5" cy="7.5" r="1.5"/></svg></div>
                <h3><c:choose><c:when test="${tab == 'all'}">You haven't listed anything yet</c:when><c:otherwise>Nothing here right now</c:otherwise></c:choose></h3>
                <p>Textbooks from last semester, a calculator you no longer use, a lamp you're replacing: someone on campus needs it.</p>
                <a class="btn btn-primary" href="${ctx}/sell">Sell an item</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="list mb-3">
                <c:forEach var="item" items="${listings}">
                    <c:set var="pending" value="${pendingCounts[item.id]}" />
                    <div class="list-item${pending > 0 ? ' highlight' : ''}">
                        <img class="list-thumb" src="${ctx}/${fn:escapeXml(item.imageUrl)}" alt="" loading="lazy">
                        <div class="list-body">
                            <div class="row" style="gap:.5rem">
                                <cl:statusBadge tone="${item.status.tone}" label="${item.status.label}" />
                                <c:if test="${pending > 0}"><a class="badge badge-warning badge-plain" href="${ctx}/sales"><c:out value="${pending}" />${pending == 1 ? ' request' : ' requests'} waiting for you</a></c:if>
                            </div>
                            <p class="list-title"><a href="${ctx}/product?id=${item.id}"><c:out value="${item.title}" /></a></p>
                            <div class="list-meta">
                                <span><c:out value="${item.categoryName}" /></span>
                                <span><c:out value="${item.conditionLabel}" /></span>
                                <span>Listed <c:out value="${item.createdAtLabel}" /></span>
                            </div>
                        </div>
                        <div class="list-side">
                            <span class="list-price"><c:out value="${item.priceLabel}" /></span>
                            <div class="list-actions">
                                <a class="btn btn-outline btn-sm" href="${ctx}/product?id=${item.id}">View</a>
                                <c:choose>
                                    <c:when test="${item.editable}">
                                        <a class="btn btn-dark btn-sm" href="${ctx}/edit?id=${item.id}">Edit</a>
                                        <form class="inline-form" method="post" action="${ctx}/listing/remove" data-confirm="Remove &ldquo;${fn:escapeXml(item.title)}&rdquo; from CampusLoop?">
                                            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                            <input type="hidden" name="id" value="${item.id}">
                                            <button type="submit" class="btn btn-danger btn-sm">Remove</button>
                                        </form>
                                    </c:when>
                                    <c:when test="${item.status == 'RESERVED'}">
                                        <a class="btn btn-dark btn-sm" href="${ctx}/sales">Manage exchange</a>
                                    </c:when>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

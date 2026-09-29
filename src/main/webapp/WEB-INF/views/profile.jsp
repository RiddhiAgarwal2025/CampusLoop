<%-- Profile (own account only). Model: profile (ProfileView), errors, typedName. --%>
<c:set var="me" value="${profile.student}" />
<jsp:include page="/WEB-INF/views/common/head.jsp" />

<div class="container">
    <div class="page-head">
        <div>
            <h1>Your profile</h1>
            <p class="lead">How other students see you on CampusLoop, plus your private account details.</p>
        </div>
        <div class="page-actions">
            <a class="btn btn-ghost" href="${ctx}/dashboard">Back to Dashboard</a>
            <a class="btn btn-outline" href="${ctx}/seller?id=${me.id}">View public page</a>
        </div>
    </div>

    <div class="split mb-3">
        <div class="stack">
            <div class="card profile-card">
                <cl:avatar name="${me.name}" image="${me.profileImage}" size="xl" />
                <h2><c:out value="${me.name}" /></h2>
                <p class="email"><c:out value="${me.email}" /></p>
                <c:choose>
                    <c:when test="${profile.rating.rated}">
                        <div class="row" style="justify-content:center"><cl:stars rating="${profile.rating.average}" count="${profile.rating.count}" /><span class="text-sm muted"><c:out value="${profile.rating.countLabel}" /></span></div>
                    </c:when>
                    <c:otherwise><span class="badge badge-outline badge-plain">New seller &middot; no reviews yet</span></c:otherwise>
                </c:choose>
                <div class="profile-stats">
                    <div><strong><c:out value="${me.walletLabel}" /></strong><span>Wallet</span></div>
                    <div><strong><c:out value="${me.sustainabilityPoints}" /> &#9851;</strong><span>Impact Points</span></div>
                    <div><strong><c:choose><c:when test="${profile.rating.rated}"><c:out value="${profile.rating.averageLabel}" /> &#9733;</c:when><c:otherwise>&ndash;</c:otherwise></c:choose></strong><span>Seller rating</span></div>
                </div>
            </div>

            <div class="card" id="reviews">
                <div class="card-head"><h2>Reviews about you</h2><span class="muted text-sm"><c:out value="${profile.rating.countLabel}" /></span></div>
                <c:choose>
                    <c:when test="${empty profile.reviews}">
                        <div class="empty"><p>When buyers complete an exchange with you, their reviews appear here and on your public seller page.</p></div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="rev" items="${profile.reviews}">
                            <div class="review-item">
                                <div class="review-head">
                                    <cl:avatar name="${rev.reviewerName}" image="${rev.reviewerImage}" size="xs" />
                                    <strong><c:out value="${rev.reviewerFirstName}" /></strong>
                                    <cl:stars rating="${0.0 + rev.rating}" count="${1}" />
                                    <span class="when"><c:out value="${rev.createdAtLabel}" /></span>
                                </div>
                                <p><c:out value="${rev.comment}" /></p>
                                <p class="about">Bought: <c:out value="${rev.listingTitle}" /></p>
                            </div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <aside class="stack">
            <form class="card card-pad form" method="post" action="${ctx}/profile" enctype="multipart/form-data" data-validate>
                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                <input type="hidden" name="action" value="save">
                <h3 class="mb-0">Edit profile</h3>

                <div class="field${not empty errors.photo ? ' has-error' : ''}">
                    <span class="label">Profile photo <span class="optional">(optional)</span></span>
                    <label class="file-drop${not empty me.profileImage ? ' has-file' : ''}"${not empty me.profileImage ? ' data-has-existing' : ''} style="min-height:160px">
                        <input type="file" id="photo" name="photo" accept="image/jpeg,image/png,image/webp" data-max-bytes="${maxUploadMb * 1024 * 1024}">
                        <c:choose>
                            <c:when test="${not empty me.profileImage}"><img class="file-preview" src="${ctx}/${fn:escapeXml(me.profileImage)}" alt="Current photo"></c:when>
                            <c:otherwise><img class="file-preview" alt="Selected photo preview"></c:otherwise>
                        </c:choose>
                        <span class="file-prompt"><strong>Upload a photo</strong><br><span class="hint">JPG, PNG or WebP &middot; up to <c:out value="${maxUploadMb}" /> MB</span></span>
                    </label>
                    <p class="file-name" aria-live="polite"></p>
                    <c:if test="${not empty errors.photo}"><p class="field-error"><c:out value="${errors.photo}" /></p></c:if>
                </div>

                <div class="field${not empty errors.name ? ' has-error' : ''}">
                    <label class="label" for="name">Full name</label>
                    <input class="input" id="name" name="name" value="${fn:escapeXml(empty typedName ? me.name : typedName)}"
                           required minlength="2" maxlength="80" autocomplete="name" data-msg-required="Please enter your name.">
                    <c:if test="${not empty errors.name}"><p class="field-error"><c:out value="${errors.name}" /></p></c:if>
                </div>

                <button type="submit" class="btn btn-primary btn-block" data-busy="Saving…">Save changes</button>
            </form>
            <c:if test="${not empty me.profileImage}">
                <form method="post" action="${ctx}/profile" data-confirm="Remove your profile photo?">
                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                    <input type="hidden" name="action" value="removePhoto">
                    <button type="submit" class="btn btn-ghost btn-block btn-sm">Remove current photo</button>
                </form>
            </c:if>

            <div class="card card-pad">
                <h3>Account information</h3>
                <dl class="spec-list mb-0">
                    <dt>Email</dt><dd class="truncate"><c:out value="${me.email}" /></dd>
                    <dt>Member since</dt><dd><c:out value="${me.memberSinceLabel}" /></dd>
                    <dt>Wallet</dt><dd><a href="${ctx}/wallet"><c:out value="${me.walletLabel}" /></a></dd>
                    <dt>Impact Points</dt><dd><c:out value="${me.sustainabilityPoints}" /></dd>
                    <dt>Active listings</dt><dd><a href="${ctx}/my-listings"><c:out value="${profile.activeListings}" /></a></dd>
                    <dt>Purchases</dt><dd><a href="${ctx}/purchases"><c:out value="${profile.completedPurchases}" /> completed</a></dd>
                    <dt>Sales</dt><dd><a href="${ctx}/sales"><c:out value="${profile.completedSales}" /> completed</a></dd>
                </dl>
                <p class="fine-print mt-2">Your email and wallet are private. Other students only see your name, photo, rating and reviews.</p>
            </div>
        </aside>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

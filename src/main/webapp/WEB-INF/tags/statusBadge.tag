<%@ tag description="Coloured status pill (tone: success|warning|info|danger|neutral|muted)" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="tone" required="true" type="java.lang.String" %>
<%@ attribute name="label" required="true" type="java.lang.String" %>
<span class="badge badge-${tone}"><c:out value="${label}" /></span>

<!-- Navigation-->
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark fixed-top" id="mainNav">
    <a class="navbar-brand" href="/coop">
        <img src="<c:url value='/resources/images/coop-small.png'/>" height="25px" alt="co-op logo">
    </a>
    <button class="navbar-toggler navbar-toggler-right" type="button" data-toggle="collapse" data-target="#navbarResponsive" aria-controls="navbarResponsive" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarResponsive">
        <ul class="navbar-nav ml-auto">
            <c:if test="${sessionScope.user != null}">
                <li class="nav-item">
                    <a class="nav-link" href="/coop/user/${sessionScope.user.getId()}">
                        <i class="fa fa-fw fa-user"></i>${sessionScope.user}</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/coop/logout">
                        <i class="fa fa-fw fa-sign-out"></i>Logout</a>
                </li>
            </c:if>
            <c:if test="${sessionScope.user == null}">
                <li class="nav-item">
                    <a class="nav-link" href="/coop/login"><i class="fa fa-fw fa-sign-in"></i>Login</a>
                </li>
            </c:if>
        </ul>
    </div>
</nav>
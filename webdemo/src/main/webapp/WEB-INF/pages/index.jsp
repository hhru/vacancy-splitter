<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE HTML>

<html>

    <head>
        <title>hh vacancy snippets splitter</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <link rel="stylesheet" href="/resources/bootstrap.min.css">
        <link rel="stylesheet" href="/resources/snippets.css"/>
    </head>

    <body>

    <div class="container">
        <ul class="nav nav-tabs">
            <li class="active"><a href="/">Search</a></li>
        </ul>
    </div>

    <div class="container">
        <br/>
        <div class="row">
            <form action="/" role="form">
                <div class="col-md-10 col-lg-10">
                    <input class="form-control"
                           type="text" name="query" value="${query}" placeholder="Enter your query"/>
                </div>
                <div class="cold-md-1 cold-lg-1">
                    <input class="btn btn-default" type="submit" value="Find!"/>
                </div>
            </form>
        </div>
        <br/>

        <div class="row">
            <c:forEach items="${vacancies}" var="vacancy">
                <div class="col-lg-12">
                    <a href="/vacancy?vacancy_id=${vacancy.id}"><h4>${vacancy.name}</h4></a>
                    <c:if test="${not empty vacancy.salary}">
                        <p><c:out value="${vacancy.salary}"/></p>
                    </c:if>
                    <c:if test="${not empty vacancy.responsibilities}">
                        <p class="vac-block"><c:out value="${vacancy.responsibilities}"/></p>
                    </c:if>
                    <c:if test="${not empty vacancy.requirements}">
                        <p class="vac-block"><c:out value="${vacancy.requirements}"/></p>
                    </c:if>
                    <br/>
                </div>
            </c:forEach>
        </div>

        <hr/>

        <div class="row">
            <div class="col-md-12 col-sm-12">
                <c:forEach begin="0" end="${pages - 1}" varStatus="loop">
                    <c:set var="pageView" value="${loop.index + 1}"/>
                    <c:choose>
                        <c:when test="${loop.index == page}">
                            ${pageView}
                        </c:when>
                        <c:otherwise>
                            <a href="/?query=${encodedQuery}&page=${loop.index}">${pageView}</a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </div>
        </div>
    </div>
    </body>

</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
    <head>
        <title>hh vacancy snippets splitter</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="/resources/bootstrap.min.css">
        <link rel="stylesheet" href="/resources/snippets.css"/>
    </head>
    <body>

    <div class="container">
        <ul class="nav nav-tabs">
            <li><a href="/">Search</a></li>
            <li class="active"><a href="#">Vacancy</a></li>
        </ul>
    </div>

    <div class="container col-md-6">
        <h2><a href="http://hh.ru/vacancy/${vacancy.id}" target="_blank">${vacancy.name}</a></h2>
        <c:out value="${vacancy.description}" escapeXml="false"/>
    </div>

    <div class="container col-md-6">
        <h2>Snippets</h2>
        <c:if test="${not empty req}">
            <p>
                <c:out value="${req}"/>
            </p>
        </c:if>
        <c:if test="${not empty res}">
            <p>
                <c:out value="${res}"/>
            </p>
        </c:if>
    </div>

    <div class="container col-md-12">
        <hr/>
        legend:&nbsp;
        <span class="res">responsibilities</span>&nbsp;
        <span class="req">requirements</span>&nbsp;
        <span class="con">conditions</span>
    </div>

    </body>
</html>

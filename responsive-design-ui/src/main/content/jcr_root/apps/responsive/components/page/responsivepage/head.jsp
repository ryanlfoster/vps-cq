<%@include file="/libs/foundation/global.jsp"%>

<head>
    <meta charset="utf-8">
    
    <!-- disable iPhone inital scale -->
    <meta name="viewport" content="width=device-width; initial-scale=1.0">
    
    <title><%=currentPage.getTitle()%></title>
    
    <% currentDesign.writeCssIncludes(pageContext); %>
    
    <cq:include script="/libs/wcm/core/components/init/init.jsp"/>
    
    <!-- html5.js for IE less than 9 -->
    <!--[if lt IE 9]>
        <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
    
    <!-- css3-mediaqueries.js for IE less than 9 -->
    <!--[if lt IE 9]>
        <script src="http://css3-mediaqueries-js.googlecode.com/svn/trunk/css3-mediaqueries.js"></script>
    <![endif]-->
    
</head>
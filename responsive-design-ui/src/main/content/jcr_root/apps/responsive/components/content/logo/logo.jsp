<%--
  Copyright 1997-2008 Day Management AG
  Barfuesserplatz 6, 4001 Basel, Switzerland
  All Rights Reserved.

  This software is the confidential and proprietary information of
  Day Management AG, ("Confidential Information"). You shall not
  disclose such Confidential Information and shall use it only in
  accordance with the terms of the license agreement you entered into
  with Day.

  ==============================================================================

  Logo component

--%>
<%@include file="/libs/foundation/global.jsp"%>
<%@ page import="com.day.text.Text" %>
<hgroup>
    <h1 id="site-logo">
        <% String home = Text.getAbsoluteParent(currentPage.getPath(), 2); %>
        <a href="<%=home%>.html"><%=currentStyle.get("sitetitle","No site title defined")%></a>
    </h1>
    <h2 id="site-description">
        <%=currentStyle.get("subtitle","No site title defined")%>
    </h2>
</hgroup>
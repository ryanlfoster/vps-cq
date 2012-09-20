<%@ page import="com.day.cq.commons.Doctype,
        com.day.cq.wcm.api.PageFilter,
        com.day.cq.wcm.foundation.Navigation,
        com.day.text.Text" session="false" %>
        
<%@include file="/libs/foundation/global.jsp"%>        
        
<body>
    
    <div id="pagewrap">
    
        <header id="header">
    
            <cq:include path="articleTitle" resourceType="responsive/components/content/logo"/>  
    
            <%      
                String home = Text.getAbsoluteParent(currentPage.getPath(), 2);
                int absParent = currentStyle.get("absParent", 2);
            
                PageFilter filter = new PageFilter(request);
                Navigation nav = new Navigation(currentPage, absParent, filter, 3);
            %> 
            
            <nav>
                <ul id="main-nav" class="clearfix">
                     <%
                        for (Navigation.Element e: nav) {
                            switch (e.getType()) {
                                case ITEM_BEGIN:
                                    %><li><a href="<%= e.getPath() %>.html" ><%= e.getTitle() %></a><%
                                    break;
                                case ITEM_END:
                                    %></li><%
                                    break;
                            }
                        }
                    %>
                    
                </ul>
                <!-- /#main-nav --> 
            </nav>
    
            <form id="searchform">
                <input type="search" id="s" placeholder="Search">
            </form>
    
        </header>
        <!-- /#header -->
        
        <div id="content">
    
            <article class="post clearfix">
    
                <header>
                    <h1 class="post-title">
                        <cq:include path="articleTitle" resourceType="foundation/components/text"/>   
                    </h1>
                    <p class="post-meta">
                        <cq:include path="articleSubTitle" resourceType="foundation/components/text"/>
                    </p>
                </header>
                <figure class="post-image"> 
                    <cq:include path="articleImage" resourceType="foundation/components/image"/>
                </figure>
                <cq:include path="articleText" resourceType="foundation/components/text"/>
                <cq:include path="articlePar" resourceType="foundation/components/parsys"/>
                
    
            </article>
            <!-- /.post -->
  
        </div>
        <!-- /#content --> 
        
        
        <aside id="sidebar">
    
            <section class="widget">
                <h4 class="widgettitle">
                    <cq:include path="widget1title" resourceType="foundation/components/text"/>
                </h4>
                <cq:include path="widget1" resourceType="foundation/components/parsys"/>
            </section>
            <!-- /.widget -->
    
            <section class="widget clearfix">
                <h4 class="widgettitle">
                    <cq:include path="widget2title" resourceType="foundation/components/text"/>
                </h4>
                <cq:include path="widget2" resourceType="foundation/components/parsys"/>
            </section>
            <!-- /.widget -->
                            
        </aside>
        <!-- /#sidebar -->
    
        <footer id="footer">
        
    
        </footer>
        <!-- /#footer --> 
        
    </div>
    <!-- /#pagewrap -->
    
</body>
package com.vps.dispatcher;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.components.Component;

/**
 * Component filter which adds the components allowed selectors to the current SelectorManager.
 * This is specified by a multi-value String property 'allowedSelectors' on the component node.
 * @author tomblackford
 */
@SlingFilter (generateComponent=true,
			  description="Adds allowed selectors for the current component to the Selector Manager",
			  generateService=true,
			  name="Selector Component Filter",
			  order=100,
			  scope=SlingFilterScope.COMPONENT
)
public class SelectorComponentFilter implements Filter {
	private static final Logger LOG = LoggerFactory.getLogger(SelectorComponentFilter.class);
	
	@Reference
	private ResourceResolverFactory resourceResolverFactory;
	
	// We need an admin resolver to load the component node (to determine its allowed selectors)
	private ResourceResolver adminResolver;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		LOG.debug("init: Initialising SelectorComponentFilter");

		try {
			this.adminResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
		} catch (LoginException e){
			throw new ServletException("Error getting admin session : "+e.getMessage(), e);
		}
	
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
		String currentResourceType = slingRequest.getResource().getResourceType();
		
		LOG.trace("doFilter: current resource type = {}",currentResourceType);
		try {
		
			if(currentResourceType!=null){
				
				// Load the resource representing the component using the admin resolver
				Resource componentResource = adminResolver.getResource(currentResourceType);
				LOG.trace("doFilter: component resource = {}",componentResource);
				
				if(componentResource!=null){
					// Adapt the component resource to a component
					Component currentComponent = componentResource.adaptTo(Component.class);
					
					// Adapt the request to an instance of SelectorManager
					SelectorManager selectorManager = slingRequest.adaptTo(SelectorManager.class);
					
					LOG.error("doFilter: Registering allowed selectors of component {} against {} ",currentComponent,selectorManager);
					
					if(selectorManager!=null){
						selectorManager.registerAllowedSelectors(currentComponent);
					}
				}
			}
		} finally {
			chain.doFilter(slingRequest, response);
		}
	}

	@Override
	public void destroy() {
		LOG.debug("init: Destroying SelectorComponentFilter");
		
		if(adminResolver!=null){
			adminResolver.close();
		}
	}

}

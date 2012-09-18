package com.vps.dispatcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyOption;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vps.dispatcher.utils.NonCommittingSlingHttpResponseWrapper;

/**
 * Filter which prevents dispatcher caching if selectors are present on the request that 
 * are not in 'allowedSelectors' list (or manually allowed) by any components.
 * @author tomblackford
 */
@SlingFilter(description="Returns non-dispatcher cacheable response if selectors are present on the request that are not allowed.",
			generateComponent=true,
			label="Dispatcher Selector Filter",
			metatype=true,
			name="Dispatcher Selector Filter",
			generateService=true,
			scope=SlingFilterScope.REQUEST,
			order=100
)	
public class SelectorFilter implements Filter {
	private static final Logger LOG = LoggerFactory.getLogger(SelectorFilter.class);
	
	private static final String BEHAVIOUR_SEND_ERROR = "sendError";
	private static final String BEHAVIOUR_UPDATE_STATUS = "updateStatus";
	private static final String BEHAVIOUR_ADD_HEADER = "addHeader";
	
	private static final String HEADER_NAME = "Dispatcher";
	private static final String HEADER_VALUE = "no-cache";
	// Configurable properties
	
	@Property(label = "Default allowed selectors", description = "Selectors which are allowed for any request.", value={"infinity", "nav", "img","banner","segment","en","permissions"})
	private static final String CONFIG_DEFAULT_ALLOWED_SELECTORS = "defaultAllowedSelectors";
	
	@Property(label = "Unmatched selectors behaviour", description = "Behaviour when selectors unmatched", options = {
			@PropertyOption (name=BEHAVIOUR_SEND_ERROR, value="Redirect response to error page"),
			@PropertyOption (name=BEHAVIOUR_UPDATE_STATUS, value="Return the original response but update the status code"),
			@PropertyOption (name=BEHAVIOUR_ADD_HEADER, value="Add "+HEADER_NAME+"=\""+HEADER_VALUE+"\" header.")
		}, value=BEHAVIOUR_ADD_HEADER)
	private static final String CONFIG_UNMATCHED_SELECTORS_BEHAVIOUR = "unmatchedSelectorsBehaviour";
	
	@Property(label = "Error code", description = "Status code to send when selectors unmatched", options = {
			@PropertyOption (name="404", value="404"),
			@PropertyOption (name="403", value="403")
		}, value="404")
	private static final String CONFIG_ERROR_CODE = "errorCode";
	
	@Property(label = "Enabled RunModes", description = "Run modes on which the filter will be enabled", value={"publish","preview"})
	private static final String CONFIG_ENABLED_RUN_MODES = "runModes";
	
	// The configured default allowed selectors
	private String[] defaultSelectors;
	
	// The configured behaviour when selectors are unmatched
	private String unmatchedSelectorsBehaviour;
	
	// The configred status code to send when selectors are unmatched
	private int errorCode = 404;
	
	// The configured run modes where this filter will be active
	private String[] enabledRunModes;
	
	// SCR References
	@Reference
	SlingSettingsService settingsService;
	
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}
	
	protected void activate(ComponentContext componentContext){
		LOG.info("activate: Activating SelectorFilter");
	    configure(componentContext.getProperties());
	}
	
    protected void configure (Dictionary<?, ?> properties){

    	LOG.trace("configure: Got default allowed selectors "+properties.get(CONFIG_DEFAULT_ALLOWED_SELECTORS));
    	
    	defaultSelectors = (String[]) properties.get(CONFIG_DEFAULT_ALLOWED_SELECTORS);
    	unmatchedSelectorsBehaviour = (String) properties.get(CONFIG_UNMATCHED_SELECTORS_BEHAVIOUR);
    	errorCode = Integer.parseInt((String) properties.get(CONFIG_ERROR_CODE));
    	enabledRunModes = (String[]) properties.get(CONFIG_ENABLED_RUN_MODES);
    	
        LOG.info("configure: Configured:");
        LOG.info("configure:\tDefault selectors = {}",Arrays.toString(defaultSelectors));
        LOG.info("configure:\tUnmatched selectors behaviour = {}", unmatchedSelectorsBehaviour);
        LOG.info("configure:\tError code = {}",errorCode);
        LOG.info("configure:\tEnabled run modes = {}",Arrays.toString(enabledRunModes));
        
    }

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		if(isEnabledRunMode()){
			
			SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
			SlingHttpServletResponse slingResponse = (SlingHttpServletResponse) response;
			
			SelectorManager selectorManager = slingRequest.adaptTo(SelectorManager.class);
			
			// Always run the next filter - we need to decide after the page has rendered
			// as only then will we know the full set of 'allowedSelectors' for the current page.
			NonCommittingSlingHttpResponseWrapper wrappedResponse = new NonCommittingSlingHttpResponseWrapper(slingResponse);
			
			LOG.trace("doFilter: about to exectute rest of filter chain.");
			chain.doFilter(request, wrappedResponse);
			LOG.trace("doFilter: executed rest of filter chain.");
			
			if(selectorManager!=null && isDispatcherCacheableRequest(slingRequest, wrappedResponse)){
				// Add the default allowed selectors to the selector manager
				selectorManager.registerAllowedSelectors(defaultSelectors);
				
				Set<String> allowedSelectors = selectorManager.getAllowedSelectors();
				LOG.debug("doFilter: Allowed selectors : {}", allowedSelectors);
				
				List<String> requestSelectors = Arrays.asList(slingRequest.getRequestPathInfo().getSelectors());
				LOG.debug("doFilter: Request selectors : {}", requestSelectors);
				
				boolean allSelectorsAllowed = allowedSelectors.containsAll(requestSelectors);
				
				// If all the selectors are valid, allow the request to
				// continue. Otherwise send the specified error response.
				if(allSelectorsAllowed){
					LOG.debug("doFilter: All passed selectors matched - allowing request to continue.");
					wrappedResponse.writeToRealResponse();
					
				} else {
					handleUnmatchedRequest(wrappedResponse);
				}
			} else {
				
				// Default case - just pass back the original response, verbatim
				LOG.debug("doFilter: Non cacheable request - passing back original response verbatim.");
				wrappedResponse.writeToRealResponse();
			}
		} else {
			chain.doFilter(request, response);
		}
	}
	
	private boolean isEnabledRunMode(){
		boolean answer = false;
		
		Set<String> currentRunmodes = settingsService.getRunModes();
		for(String runMode : enabledRunModes){
			if(currentRunmodes.contains(runMode)){
				answer = true;
				break;
			}
		}
		
		LOG.debug("isEnabledRunMode: filter enabled is "+answer);
		
		return answer;
	}
	
	private void handleUnmatchedRequest(NonCommittingSlingHttpResponseWrapper wrappedResponse) throws IOException {
		if(unmatchedSelectorsBehaviour.equals(BEHAVIOUR_SEND_ERROR)){
			LOG.warn("doFilter: Some selectors unmatched - sending error {}", errorCode);
			wrappedResponse.sendError(errorCode,"Some of the passed selectors were not valid for this resource.");
			
		} else if (unmatchedSelectorsBehaviour.equals(BEHAVIOUR_UPDATE_STATUS)){
			LOG.warn("doFilter: Some selectors unmatched - sending original response with status {}", errorCode);
			
			// Write the buffered response to the real response and set status
			wrappedResponse.setStatus(errorCode);	
			wrappedResponse.writeToRealResponse();
			

		} else if (unmatchedSelectorsBehaviour.equals(BEHAVIOUR_ADD_HEADER)){
			LOG.warn("doFilter: Some selectors unmatched - setting "+HEADER_NAME+"=\""+HEADER_VALUE+"\" header");
			
			// Write the buffered response to the real response and set header
			wrappedResponse.setHeader(HEADER_NAME, HEADER_VALUE);
			wrappedResponse.writeToRealResponse();
			
		}	
	}
	
	/**
	 * Answer whether the dispatcher could cache the result of this request. This is based on the nature of 
	 * the request (ie it's a GET with no params) and the status of the response (ie it's 200).
	 * @param slingRequest
	 * @param slingResponse
	 * @return
	 */
	private boolean isDispatcherCacheableRequest(SlingHttpServletRequest slingRequest, NonCommittingSlingHttpResponseWrapper slingResponse){
		 LOG.debug("isDispatcherCacheableRequest: Response code = {}", slingResponse.getStatus());
		 LOG.debug("isDispatcherCacheableRequest: Request method = {}", slingRequest.getMethod());
		 LOG.debug("isDispatcherCacheableRequest: No request params = {}", slingRequest.getParameterMap().isEmpty());
		 LOG.debug("isDispatcherCacheableRequest: Dispatcher header ={}", slingRequest.getHeader(HEADER_NAME));
		 
		 boolean answer = slingResponse.getStatus() == 200 
				 			&& slingRequest.getMethod().equals("GET") 
				 			&& slingRequest.getParameterMap().isEmpty()
				 			&& !HEADER_VALUE.equals(slingRequest.getHeader(HEADER_NAME));
		 
		 LOG.debug("isDispatcherCacheableRequest: answering "+answer);
		 
		 return answer;
		
	}
	
	@Override
	public void destroy() {

	}

}

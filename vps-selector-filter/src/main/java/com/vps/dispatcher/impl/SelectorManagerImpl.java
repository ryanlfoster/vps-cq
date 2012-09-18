package com.vps.dispatcher.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.components.Component;
import com.vps.dispatcher.SelectorManager;

/**
 *
 * @author tomblackford
 */
public class SelectorManagerImpl implements SelectorManager {
	private static final Logger LOG = LoggerFactory.getLogger(SelectorManagerImpl.class);
	
	// Request attribute against which this object will be cached
	private static final String REQUEST_ATTR_SELECTOR_MANAGER = "com.vps.dispatcher.impl.SelectorManagerImpl";
	
	// Component node property describing which selectors are allowed for that component
	private static final String PROPERTY_ALLOWED_SELECTORS = "allowedSelectors";
	
	// Overall set of allowed selectors for the current request
	private Set<String> allowedSelectors;

	public SelectorManagerImpl() {
		super();
		this.allowedSelectors = new HashSet<String>();
		
		// Add default selectors - TODO: Make these configurable
		this.allowedSelectors.add("img");
		this.allowedSelectors.add("banner");
	}

	@Override
	public void registerAllowedSelectors(Component currentComponent) {
			
			// Get the allowed selectors from the component and add to the overall set
			if(currentComponent!=null){
				String[] componentAllowedSelectors = currentComponent.getProperties().get(PROPERTY_ALLOWED_SELECTORS, new String[0]);
				LOG.debug("registerAllowedSelectors: Adding allowed selectors {} for component {}",Arrays.toString(componentAllowedSelectors), currentComponent.getName());
				for(String selector : componentAllowedSelectors){
					this.allowedSelectors.add(selector);
				}
			}
	}

	@Override
	public void registerAllowedSelector(String selector) {
		LOG.debug("registerAllowedSelector: Adding allowed selectors {}", selector);
		this.allowedSelectors.add(selector);
		
	}

	@Override
	public void registerAllowedSelectors(String[] selectors) {
		LOG.debug("registerAllowedSelectors: Adding allowed selectors {}", Arrays.toString(selectors));
		for(String selector : selectors){
			this.allowedSelectors.add(selector);
		}	
	}

	@Override
	public Set<String> getAllowedSelectors() {
		LOG.trace("getAllowedSelectors: Answering allowed selectors {}", allowedSelectors);
		return Collections.unmodifiableSet(allowedSelectors);
	}
	
	/**
	 * Cache the passed SelectorManagerImpl against the passed request
	 * @param request
	 * @param selectorManager
	 */
	public static void toRequest(HttpServletRequest request, SelectorManager selectorManager){
		request.setAttribute(REQUEST_ATTR_SELECTOR_MANAGER, selectorManager);
	}
	
	/**
	 * Answer an instance of SelectorManager from the passed request (if one exists)
	 * @param request
	 * @return
	 */
	public static SelectorManager fromRequest(HttpServletRequest request){
		return (SelectorManager) request.getAttribute(REQUEST_ATTR_SELECTOR_MANAGER);
	}

}

package com.vps.dispatcher;

import java.util.Set;
import com.day.cq.wcm.api.components.Component;

public interface SelectorManager {
	/**
	 * Add the current component's allowed selectors to
	 * the overall set of permissable selectors for this request.
	 * @param currentComponent
	 */
	void registerAllowedSelectors(Component currentComponent);
	
	/**
	 * Add the passed selector to the overall set of 
	 * permissable selectors for this request.
	 * @param currentComponent
	 */
	void registerAllowedSelector(String selector);
	
	/**
	 * Add the passed selectors to the overall set of 
	 * permissable selectors for this request.
	 * @param currentComponent
	 */
	void registerAllowedSelectors(String[] selectors);
		
	/**
	 * Answer the total Set of allowed selectors for the passed request.
	 * @return
	 */
	Set<String> getAllowedSelectors();
}

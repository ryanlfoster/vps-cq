package com.vps.dispatcher;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.AdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vps.dispatcher.impl.SelectorManagerImpl;

@Service (value=AdapterFactory.class) 
@Component
@Properties ({
	@Property (name="adaptables", value={"org.apache.sling.api.SlingHttpServletRequest"}),
	@Property (name="adapters", value={"com.vps.dispatcher.SelectorManager"})
})
public class AdapterFactoryImpl implements AdapterFactory {
	private static final Logger LOG = LoggerFactory.getLogger(AdapterFactoryImpl.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public <AdapterType> AdapterType getAdapter(Object adaptable, Class<AdapterType> type) {
		SelectorManager answer = null;
		
		LOG.trace("getAdapter : attempt to adapt {} to {}", adaptable, type);
		
		if(adaptable instanceof SlingHttpServletRequest && type==SelectorManager.class){
			SlingHttpServletRequest request = (SlingHttpServletRequest) adaptable;
			
			// Try to get an existing SelectorManager from the request
			answer = SelectorManagerImpl.fromRequest(request);
			if(answer == null){
				answer = new SelectorManagerImpl();
				SelectorManagerImpl.toRequest(request, answer);
			}
		}	
		
		return (AdapterType) answer;
	}

}

package com.mldm.rest.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class HWDRWebConfiguration extends WebMvcConfigurerAdapter  {
/*	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new AuthCodeInterceptor());
		super.addInterceptors(registry);
	}*/

	private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
			"classpath:/static/",
	        "classpath:/META-INF/resources/", "classpath:/resources/","classpath:/resources/static"};

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
	    registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
	}


	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addRedirectViewController("/", "index.html");
	}
}

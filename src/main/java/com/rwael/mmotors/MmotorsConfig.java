package com.rwael.mmotors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;

@Configuration 
public class MmotorsConfig {
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
	{
		var cfg = new PropertySourcesPlaceholderConfigurer();
		cfg.setLocation(new FileSystemResource(".env"));
		return cfg;
	}
}

package com.echovault.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Map clean URL aliases to static HTML resources
        registry.addViewController("/vault/record-voice").setViewName("forward:/record-voice.html");
        registry.addViewController("/vault/ghost-chat").setViewName("forward:/ghost-chat.html");
        registry.addViewController("/vault/write-letter").setViewName("forward:/write-letter.html");
        registry.addViewController("/vault/emergency-unlock").setViewName("forward:/emergency-unlock.html");
        registry.addViewController("/vault/memorial").setViewName("forward:/memorial.html");
    }
}

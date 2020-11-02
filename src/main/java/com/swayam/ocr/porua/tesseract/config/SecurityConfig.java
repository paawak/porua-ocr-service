package com.swayam.ocr.porua.tesseract.config;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
	http.cors().and().addFilterAfter(authenticationFilter(), BasicAuthenticationFilter.class)
		.authorizeRequests(a -> a.antMatchers("/", "/error", "/webjars/**").permitAll().anyRequest().authenticated())
		.exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
	UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
	corsConfiguration.addAllowedOrigin("http://localhost:3000");
	corsConfiguration.addAllowedMethod("*");
	source.registerCorsConfiguration("/**/**", corsConfiguration);
	return source;
    }

    @Bean
    public AuthenticationConverter authenticationConverter() {
	return (HttpServletRequest request) -> new UsernamePasswordAuthenticationToken("Hardcoded Full Name", "", new ArrayList<>());
    }

    @Bean
    public AuthenticationFilter authenticationFilter() {
	AuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler() {

	    @Override
	    protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		// do nothing
	    }

	};
	AuthenticationFilter filter = new AuthenticationFilter(authenticationManager, authenticationConverter());
	filter.setSuccessHandler(successHandler);
	return filter;
    }

}

package com.swayam.ocr.porua.tesseract.config;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String[] URLS_TO_ALLOW_WITHOUT_AUTH = { "/ocr/train/query/word/image", "/v2/api-docs", "/configuration/**", "/swagger-ui.html", "/swagger*/**", "/webjars/**" };

    private final AuthenticationConverter authenticationConverter;

    public SecurityConfig(AuthenticationConverter authenticationConverter) {
	this.authenticationConverter = authenticationConverter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
	http.cors(config -> config.configurationSource(corsConfigurationSource())).authorizeRequests().antMatchers(URLS_TO_ALLOW_WITHOUT_AUTH).permitAll().anyRequest().authenticated().and()
		.addFilterBefore(authenticationFilter(), BasicAuthenticationFilter.class).csrf().disable();
    }

    @Override
    protected AuthenticationManager authenticationManager() {
	return new GoogleAuthenticationManager();
    }

    private CorsConfigurationSource corsConfigurationSource() {
	UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
	corsConfiguration.addAllowedOrigin("http://localhost:3000");
	corsConfiguration.addAllowedMethod(HttpMethod.GET);
	corsConfiguration.addAllowedMethod(HttpMethod.PUT);
	corsConfiguration.addAllowedMethod(HttpMethod.POST);
	corsConfiguration.addAllowedMethod(HttpMethod.DELETE);
	corsConfiguration.addAllowedMethod(HttpMethod.OPTIONS);
	corsConfiguration.addAllowedHeader("X-Requested-With");
	corsConfiguration.addAllowedHeader("Content-Type");
	corsConfiguration.addAllowedHeader("Accept");
	corsConfiguration.addAllowedHeader("Origin");
	corsConfiguration.addAllowedHeader("Authorization");
	source.registerCorsConfiguration("/**/**", corsConfiguration);
	return source;
    }

    private AuthenticationFilter authenticationFilter() {
	AuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler() {
	    @Override
	    protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		// do nothing
	    }
	};
	AuthenticationFilter filter = new AuthenticationFilter(authenticationManager(), authenticationConverter);
	RequestMatcher requestMatcher =
		new NegatedRequestMatcher(new OrRequestMatcher(Arrays.stream(URLS_TO_ALLOW_WITHOUT_AUTH).map(pattern -> new AntPathRequestMatcher(pattern)).collect(Collectors.toList())));
	filter.setRequestMatcher(requestMatcher);
	filter.setSuccessHandler(successHandler);
	return filter;
    }

}

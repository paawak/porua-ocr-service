package com.swayam.ocr.porua.tesseract.config;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.swayam.ocr.porua.tesseract.repo.UserDetailsRepository;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String OCR_TRAIN_QUERY_WORD_IMAGE = "/ocr/train/query/word/image";
    private static final String[] URLS_TO_ALLOW_WITHOUT_AUTH = { "/v2/api-docs", "/configuration/**", "/swagger-ui.html", "/swagger*/**", "/webjars/**", "/ocr/train/user/register", "/actuator/**" };

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
	http.cors(config -> config.configurationSource(corsConfigurationSource())).authorizeRequests().antMatchers(URLS_TO_ALLOW_WITHOUT_AUTH).permitAll().anyRequest().authenticated().and()
		.addFilterBefore(authenticationFilter(), BasicAuthenticationFilter.class).csrf().disable();
    }

    @Override
    protected AuthenticationManager authenticationManager() {
	return new GoogleAuthenticationManager(userDetailsRepository);
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
	source.registerCorsConfiguration("/ocr/**", corsConfiguration);
	return source;
    }

    private AuthenticationFilter authenticationFilter() {
	AuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler() {
	    @Override
	    protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		// do nothing
	    }
	};
	AuthenticationConverter authenticationConverter = new AuthenticationTokenExtractor(new AntPathRequestMatcher(OCR_TRAIN_QUERY_WORD_IMAGE));
	AuthenticationFilter filter = new AuthenticationFilter(authenticationManager(), authenticationConverter);
	RequestMatcher requestMatcher =
		new NegatedRequestMatcher(new OrRequestMatcher(Arrays.stream(URLS_TO_ALLOW_WITHOUT_AUTH).map(pattern -> new AntPathRequestMatcher(pattern)).collect(Collectors.toList())));
	filter.setRequestMatcher(requestMatcher);
	filter.setSuccessHandler(successHandler);
	return filter;
    }

}

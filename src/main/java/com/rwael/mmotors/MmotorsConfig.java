package com.rwael.mmotors;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import com.rwael.mmotors.Database.User;
import com.rwael.mmotors.Database.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Configuration 
public class MmotorsConfig
{
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
	{
		var cfg = new PropertySourcesPlaceholderConfigurer();
		cfg.setLocation(new FileSystemResource(".env"));
		return cfg;
	}
}

@Configuration
@EnableWebSecurity 
class WebSecurityConfig
{
	private final UserRepository users;

	public WebSecurityConfig(UserRepository users)
	{
		this.users = users;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationConfiguration config)
	{
		SecurityContextRepository ctxRepo = new HttpSessionSecurityContextRepository();
		UserAuthFilter filter = new UserAuthFilter();
		filter.setAuthenticationSuccessHandler(new UserAuthSuccessHandler());
		filter.setAuthenticationFailureHandler(new UserAuthFailHandler(users));
		filter.setAuthenticationManager(config.getAuthenticationManager());
		filter.setSecurityContextRepository(ctxRepo);
		filter.setFilterProcessesUrl("/api/login");

		http.securityContext((c) -> c
			.securityContextRepository(ctxRepo)
		).authorizeHttpRequests((c) -> c
			.requestMatchers("/private/**").authenticated()
			.requestMatchers("/admin/**").hasRole("ADMIN")
			.requestMatchers("/api/private/**").authenticated()
			.requestMatchers("/api/admin/**").hasRole("ADMIN")
			.anyRequest().permitAll()
		).addFilterAt(filter, UsernamePasswordAuthenticationFilter.class)
		.logout((c) -> c.logoutUrl("/api/logout"));

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}

	@Bean
	UserDetailsService userDetailsService()
	{
		return new UserAuthService(users);
	}

	// @Bean
	// static RoleHierarchy roleHierarchy()
	// {
	// 	return RoleHierarchyImpl.withDefaultRolePrefix()
	// 		.role("ADMIN").implies("USER")
	// 		.build();
	// }
}

class UserAuthService implements UserDetailsService
{
	private final UserRepository users;

	public UserAuthService(UserRepository repo)
	{
		users = repo;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = users.findByEmail(username);

		if(user == null)
			throw new UsernameNotFoundException("User " + username + " does not exist");

		return user;
	}
}

record Login(String username, String password) {}

class UserAuthFilter extends UsernamePasswordAuthenticationFilter
{
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException
	{
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			Login creds = mapper.readValue(request.getReader(), Login.class);
			var token = new UsernamePasswordAuthenticationToken(creds.username(), creds.password());

			setDetails(request, token);

			var auth = getAuthenticationManager().authenticate(token);

			return auth;
		}
		catch(IOException e)
		{
			throw new AuthenticationServiceException("Failed to read login data");
		}
	}
}

record LoginResult(boolean success, String message) {}

class UserAuthSuccessHandler implements AuthenticationSuccessHandler
{
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException
	{
		response.setStatus(HttpServletResponse.SC_OK);
		new ObjectMapper().writeValue(response.getWriter(), new LoginResult(true, ""));
	}
}

class UserAuthFailHandler implements AuthenticationFailureHandler
{
	private final UserRepository users;

	public UserAuthFailHandler(UserRepository users)
	{
		this.users = users;
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException
	{
		String reason = "Erreur lors de la connexion";

		if(exception instanceof BadCredentialsException)
		{
			if(users.findByEmail(exception.getAuthenticationRequest().getName()) == null)
				reason = "Adresse e-mail invalide";
			else
				reason = "Mot de passe incorrecte";
		}

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		new ObjectMapper().writeValue(response.getWriter(), new LoginResult(false, reason));
	}
}
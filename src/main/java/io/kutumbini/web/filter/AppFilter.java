package io.kutumbini.web.filter;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppFilter implements Filter {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		
		LOGGER.debug("requestURI: " + req.getRequestURI());
		if (req.getCookies() != null) {
			LOGGER.debug("request cookies:");
			Arrays.asList(req.getCookies()).forEach(c -> LOGGER.debug("---" + c.getName() + ":" + c.getValue()));
		}
		if (!req.getParameterMap().isEmpty()) {
			LOGGER.debug("request paramters:");
			req.getParameterMap().forEach((k,v) -> LOGGER.debug("---" + k + ": " + v[0]));
		}
				
		chain.doFilter(request, response);

		if (resp.getHeaderNames() != null) {
			LOGGER.debug("response headers:");
			resp.getHeaderNames().forEach(h -> LOGGER.debug("---" + h + ":" + resp.getHeader(h)));
		}
}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

}
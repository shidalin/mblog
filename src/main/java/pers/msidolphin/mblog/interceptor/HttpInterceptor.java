package pers.msidolphin.mblog.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import pers.msidolphin.mblog.helper.JsonHelper;
import pers.msidolphin.mblog.helper.PropertiesHelper;
import pers.msidolphin.mblog.helper.RedisHelper;
import pers.msidolphin.mblog.helper.RequestHolder;
import pers.msidolphin.mblog.object.po.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by msidolphin on 2018/3/31.
 */
public class HttpInterceptor implements HandlerInterceptor {

	@Autowired
	private RedisHelper redisHelper;

	@Autowired
	private PropertiesHelper propertiesHelper;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String requestURI = request.getRequestURI();
		//判断请求资源是属于前台还是后台
		if(requestURI.startsWith("/api/admin") || requestURI.startsWith("/api/upload")) {
			Cookie[] cookies = request.getCookies();
			String key = null;
			if(cookies != null) {
				for(Cookie cookie : cookies) {
					if(propertiesHelper.getValue("blog.admin.user.token.key").equals(cookie.getName()))
						key = cookie.getValue();
				}
			}
			if(key != null) {
				//当cookie中携带了session key
				String userJson = redisHelper.getValue(key);
				RequestHolder.addAdminUser(JsonHelper.string2Object(userJson, User.class));
			}
		}else if(requestURI.startsWith("/api")) {
			//检查cookie中是否携带session key
			Cookie[] cookies = request.getCookies();
			String key = null;
			if (cookies != null) {
				for(Cookie cookie : cookies) {
					if (propertiesHelper.getValue("blog.user.session.key").equals(cookie.getName()))
						key = cookie.getValue();
				}
				if (key != null) {
					//当cookie中携带了session key
					String userJson = redisHelper.getValue(key);
					RequestHolder.add(JsonHelper.string2Object(userJson, User.class));
				}
			}
		}else {
			return false;
		}
		RequestHolder.add(request);
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
		RequestHolder.removeAll();
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
		//请求完成，移除requestHolder中的缓存
		RequestHolder.removeAll();
	}
}

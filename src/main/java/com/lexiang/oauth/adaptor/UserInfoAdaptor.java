package com.lexiang.oauth.adaptor;


import com.lexiang.oauth.WLUser;
import com.lexiang.oauth.annotation.CheckUser;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

public interface UserInfoAdaptor {

   void  userHandler(HttpServletRequest request, Method method, CheckUser checkUser, Object loginVO);

}


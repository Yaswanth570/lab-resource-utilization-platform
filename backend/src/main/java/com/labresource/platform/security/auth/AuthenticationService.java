package com.labresource.platform.security.auth;

import com.labresource.platform.security.dto.LoginRequest;
import com.labresource.platform.security.dto.LoginResponse;
import com.labresource.platform.security.dto.RegisterRequest;
import com.labresource.platform.user.web.UserResponse;

public interface AuthenticationService {

    LoginResponse authenticate(LoginRequest request);

    UserResponse register(RegisterRequest request);
}


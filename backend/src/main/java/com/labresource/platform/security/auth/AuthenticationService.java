package com.labresource.platform.security.auth;

import com.labresource.platform.security.dto.LoginRequest;
import com.labresource.platform.security.dto.LoginResponse;

public interface AuthenticationService {

    LoginResponse authenticate(LoginRequest request);
}

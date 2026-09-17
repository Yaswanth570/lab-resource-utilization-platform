package com.labresource.platform.user.service;

import com.labresource.platform.user.Role;
import com.labresource.platform.user.UserRoleType;

import java.util.List;

public interface RoleService {

    Role getRoleById(Long id);

    Role getRoleByName(UserRoleType name);

    List<Role> listRoles();

    Role createRole(Role role);
}

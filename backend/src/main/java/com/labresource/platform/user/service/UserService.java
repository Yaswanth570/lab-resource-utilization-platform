package com.labresource.platform.user.service;

import com.labresource.platform.user.User;
import com.labresource.platform.user.UserRoleType;

import java.util.List;

public interface UserService {

    User createUser(User user, Long institutionId, Long departmentId);

    User getUserById(Long id);

    User getUserByEmail(String email);

    List<User> listUsersByInstitution(Long institutionId);

    List<User> listUsersByDepartment(Long departmentId);

    User updateUser(Long id, User updatedData, Long departmentId);

    void activateUser(Long id);

    void deactivateUser(Long id);

    User assignRole(Long userId, Long roleId);

    User assignRole(Long userId, UserRoleType roleType);

    User removeRole(Long userId, Long roleId);

    User removeRole(Long userId, UserRoleType roleType);
}

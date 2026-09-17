package com.labresource.platform.user.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import com.labresource.platform.user.Role;
import com.labresource.platform.user.User;
import com.labresource.platform.user.UserRoleType;
import com.labresource.platform.user.UserStatus;
import com.labresource.platform.user.repository.RoleRepository;
import com.labresource.platform.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository,
                           InstitutionRepository institutionRepository,
                           DepartmentRepository departmentRepository,
                           RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public User createUser(User user, Long institutionId, Long departmentId) {
        if (user == null) {
            throw new InvalidOperationException("User payload cannot be null");
        }
        if (institutionId == null) {
            throw new InvalidOperationException("Institution ID is required to create a user");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new InvalidOperationException("User email is required");
        }

        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", institutionId));

        if (departmentId != null) {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));

            if (!department.getInstitution().getId().equals(institutionId)) {
                throw new InvalidOperationException(String.format(
                        "Department with id %d does not belong to institution with id %d", departmentId, institutionId));
            }
            user.setDepartment(department);
        }

        String normalizedEmail = user.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("User", "email", normalizedEmail);
        }

        user.setEmail(normalizedEmail);
        user.setInstitution(institution);
        if (user.getStatus() == null) {
            user.setStatus(UserStatus.ACTIVE);
        }

        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        if (id == null) {
            throw new InvalidOperationException("User ID cannot be null");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        initializeUser(user);
        return user;
    }

    @Override
    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidOperationException("User email cannot be null or blank");
        }
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email.trim().toLowerCase()));
        initializeUser(user);
        return user;
    }

    @Override
    public List<User> listUsersByInstitution(Long institutionId) {
        if (institutionId == null) {
            throw new InvalidOperationException("Institution ID cannot be null");
        }
        if (!institutionRepository.existsById(institutionId)) {
            throw new ResourceNotFoundException("Institution", "id", institutionId);
        }
        List<User> users = userRepository.findByInstitutionId(institutionId);
        users.forEach(this::initializeUser);
        return users;
    }

    @Override
    public List<User> listUsersByDepartment(Long departmentId) {
        if (departmentId == null) {
            throw new InvalidOperationException("Department ID cannot be null");
        }
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", "id", departmentId);
        }
        List<User> users = userRepository.findByDepartmentId(departmentId);
        users.forEach(this::initializeUser);
        return users;
    }

    private void initializeUser(User user) {
        if (user != null && user.getRoles() != null) {
            user.getRoles().size();
        }
    }

    @Override
    @Transactional
    public User updateUser(Long id, User updatedData, Long departmentId) {
        if (updatedData == null) {
            throw new InvalidOperationException("Updated user data cannot be null");
        }
        User existing = getUserById(id);
        Long institutionId = existing.getInstitution().getId();

        // Reject cross-institution transfer
        if (updatedData.getInstitution() != null && updatedData.getInstitution().getId() != null
                && !updatedData.getInstitution().getId().equals(institutionId)) {
            throw new InvalidOperationException("Cross-institution user transfer is not permitted");
        }

        if (updatedData.getEmail() != null && !updatedData.getEmail().trim().isEmpty()) {
            String newEmail = updatedData.getEmail().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(existing.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new DuplicateResourceException("User", "email", newEmail);
            }
            existing.setEmail(newEmail);
        }

        if (departmentId != null) {
            Department newDepartment = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));

            if (!newDepartment.getInstitution().getId().equals(institutionId)) {
                throw new InvalidOperationException(String.format(
                        "Department with id %d does not belong to the user's institution (id %d)", departmentId, institutionId));
            }
            existing.setDepartment(newDepartment);
        }

        if (updatedData.getFirstName() != null) {
            existing.setFirstName(updatedData.getFirstName());
        }
        if (updatedData.getLastName() != null) {
            existing.setLastName(updatedData.getLastName());
        }
        if (updatedData.getPhone() != null) {
            existing.setPhone(updatedData.getPhone());
        }

        User saved = userRepository.save(existing);
        initializeUser(saved);
        return saved;
    }

    @Override
    @Transactional
    public void activateUser(Long id) {
        User user = getUserById(id);
        user.setStatus(UserStatus.ACTIVE);
        user.setDeletedAt(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        User user = getUserById(id);
        user.setStatus(UserStatus.DEACTIVATED);
        user.setDeletedAt(Instant.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User assignRole(Long userId, Long roleId) {
        if (roleId == null) {
            throw new InvalidOperationException("Role ID cannot be null");
        }
        User user = getUserById(userId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        user.getRoles().add(role);
        User saved = userRepository.save(user);
        initializeUser(saved);
        return saved;
    }

    @Override
    @Transactional
    public User assignRole(Long userId, UserRoleType roleType) {
        if (roleType == null) {
            throw new InvalidOperationException("Role type cannot be null");
        }
        User user = getUserById(userId);
        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleType.name()));

        user.getRoles().add(role);
        User saved = userRepository.save(user);
        initializeUser(saved);
        return saved;
    }

    @Override
    @Transactional
    public User removeRole(Long userId, Long roleId) {
        if (roleId == null) {
            throw new InvalidOperationException("Role ID cannot be null");
        }
        User user = getUserById(userId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        user.getRoles().remove(role);
        User saved = userRepository.save(user);
        initializeUser(saved);
        return saved;
    }

    @Override
    @Transactional
    public User removeRole(Long userId, UserRoleType roleType) {
        if (roleType == null) {
            throw new InvalidOperationException("Role type cannot be null");
        }
        User user = getUserById(userId);
        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleType.name()));

        user.getRoles().remove(role);
        User saved = userRepository.save(user);
        initializeUser(saved);
        return saved;
    }
}

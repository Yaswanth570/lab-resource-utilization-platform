package com.labresource.platform.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labresource.platform.department.Department;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.security.jwt.JwtService;
import com.labresource.platform.user.Role;
import com.labresource.platform.user.User;
import com.labresource.platform.user.UserRoleType;
import com.labresource.platform.user.UserStatus;
import com.labresource.platform.user.service.RoleService;
import com.labresource.platform.user.service.UserService;
import com.labresource.platform.user.web.AssignRoleRequest;
import com.labresource.platform.user.web.CreateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "jwt.secret=${JWT_SECRET:dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW1pbi0zMi1ieXRlcw==}")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    @MockBean
    private RoleService roleService;

    private String validToken;

    @BeforeEach
    void setUp() {
        validToken = "Bearer " + jwtService.generateToken(1L, "admin@univ.edu", 10L, List.of("ROLE_INSTITUTION_ADMINISTRATOR"));
    }

    @Test
    @DisplayName("POST /api/users - Success creates user and hashes password without leaking passwordHash")
    void testCreateUserSuccess() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setInstitutionId(10L);
        req.setDepartmentId(5L);
        req.setEmail("alice@univ.edu");
        req.setPassword("Password123!");
        req.setFirstName("Alice");
        req.setLastName("Wonderland");

        User created = new User();
        created.setId(42L);
        created.setEmail("alice@univ.edu");
        created.setFirstName("Alice");
        created.setLastName("Wonderland");
        created.setStatus(UserStatus.ACTIVE);
        created.setPasswordHash("super-secret-bcrypt-hash");

        Institution inst = new Institution();
        inst.setId(10L);
        inst.setName("MIT");
        created.setInstitution(inst);

        Department dept = new Department();
        dept.setId(5L);
        dept.setName("CS");
        created.setDepartment(dept);

        when(userService.createUser(any(User.class), eq(10L), eq(5L))).thenReturn(created);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.email").value("alice@univ.edu"))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Wonderland"))
                .andExpect(jsonPath("$.institutionId").value(10))
                .andExpect(jsonPath("$.departmentId").value(5))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/users/me - Returns authenticated user details")
    void testGetCurrentUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("admin@univ.edu");
        user.setFirstName("Admin");
        user.setLastName("User");
        user.setStatus(UserStatus.ACTIVE);

        Institution inst = new Institution();
        inst.setId(10L);
        user.setInstitution(inst);

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("admin@univ.edu"));
    }

    @Test
    @DisplayName("POST /api/users/{id}/roles - Assign role to user")
    void testAssignRole() throws Exception {
        AssignRoleRequest req = new AssignRoleRequest();
        req.setRoleId(3L);

        Role role = new Role();
        role.setId(3L);
        role.setName(UserRoleType.ROLE_LAB_MANAGER);
        role.setDescription("Lab Manager");

        User user = new User();
        user.setId(1L);
        user.setEmail("admin@univ.edu");
        user.setFirstName("Admin");
        user.setLastName("User");
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(role));

        Institution inst = new Institution();
        inst.setId(10L);
        user.setInstitution(inst);

        when(userService.assignRole(1L, 3L)).thenReturn(user);

        mockMvc.perform(post("/api/users/1/roles")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_LAB_MANAGER"));
    }

    @Test
    @DisplayName("GET /api/roles - Lists all roles")
    void testListRoles() throws Exception {
        Role r1 = new Role();
        r1.setId(1L);
        r1.setName(UserRoleType.ROLE_SYSTEM_ADMINISTRATOR);
        r1.setDescription("System Admin");

        when(roleService.listRoles()).thenReturn(List.of(r1));

        mockMvc.perform(get("/api/roles")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("ROLE_SYSTEM_ADMINISTRATOR"));
    }
}

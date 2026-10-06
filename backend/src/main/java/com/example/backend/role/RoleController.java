package com.example.backend.role;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final CareerRoleRepository roles;

    public RoleController(CareerRoleRepository roles) { this.roles = roles; }

    @GetMapping
    public List<CareerRole> all() { return roles.findAll(); }
}
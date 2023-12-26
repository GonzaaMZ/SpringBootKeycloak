package com.api.rest.controller;

import com.api.rest.dto.UserDto;
import com.api.rest.service.IKeycloakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/keycloak/user")
@PreAuthorize("hasRole('admin_client_role')")
public class KeycloakController {

    @Autowired
    private IKeycloakService keycloakService;


    @GetMapping("/search")
    public ResponseEntity<?> findAll(){
        return ResponseEntity.ok(keycloakService.findAllUsers());
    }
    @GetMapping("/search/{username}")
    public ResponseEntity<?> findAll(@PathVariable String username){
        return ResponseEntity.ok(keycloakService.searchUserByUsername(username));
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody UserDto userDto) throws URISyntaxException {
        String response = keycloakService.createUser(userDto);
        return ResponseEntity.created(new URI("/keycloak/user/create")).body(response);
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<?> update(@PathVariable String userId, @RequestBody UserDto userDto){
        keycloakService.updateUser(userId, userDto);
        return ResponseEntity.ok("User updated Successfully");
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> delete(@PathVariable String userId){
        keycloakService.deleteUser(userId);
        return  ResponseEntity.noContent().build();
    }


}

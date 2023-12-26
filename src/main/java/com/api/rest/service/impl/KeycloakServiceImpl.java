package com.api.rest.service.impl;

import com.api.rest.dto.UserDto;
import com.api.rest.service.IKeycloakService;
import com.api.rest.util.KeycloakProvider;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class KeycloakServiceImpl implements IKeycloakService {


    /**
     * Metodo para listar todos los usuarios registrados en Keycloak
     * @return List<UserRepresentation>
     * */
    @Override
    public List<UserRepresentation> findAllUsers() {
        return KeycloakProvider.realmResource().users().list();
    }

    /**
     * Metodo para buscar un usuario por username en Keycloak
     * @return List<UserRepresentation>
     * */
    @Override
    public List<UserRepresentation> searchUserByUsername(String username) {
        return KeycloakProvider.realmResource().users().searchByUsername(username, true);
    }

    /**
     * Metodo para crear un usuario nuevo en Keycloak
     * @return String
     * */
    @Override
    public String createUser(@NonNull UserDto userDto) {
        int status = 0;
        UsersResource usersResource = KeycloakProvider.getUserResource();

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEmail(userDto.getEmail());
        userRepresentation.setFirstName(userDto.getFirstName());
        userRepresentation.setLastName(userDto.getLastName());
        userRepresentation.setUsername(userDto.getUsername());
        userRepresentation.setEmailVerified(true);
        userRepresentation.setEnabled(true);

        Response response = usersResource.create(userRepresentation);
        status = response.getStatus();
        if(status == 201){
            String path = response.getLocation().getPath();
            String userId = path.substring(path.lastIndexOf("/") + 1);

            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setTemporary(false);
            credentialRepresentation.setType(OAuth2Constants.PASSWORD);
            credentialRepresentation.setValue(userDto.getPassword());

            usersResource.get(userId).resetPassword(credentialRepresentation);

            RealmResource realmResource = KeycloakProvider.realmResource();

            List<RoleRepresentation> roleRepresentations = null;

            if(userDto.getRoles() == null || userDto.getRoles().isEmpty()){
                roleRepresentations = List.of(realmResource.roles().get("user").toRepresentation());
            } else {
                roleRepresentations = realmResource.roles()
                        .list()
                        .stream()
                        .filter(role -> userDto.getRoles()
                                .stream()
                                .anyMatch(roleName -> roleName.equalsIgnoreCase(role.getName())))
                        .toList();
            }
            realmResource.users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .add(roleRepresentations);

            return "User created successfully";
        } else if (status == 409) {
            log.error("User exist already");
            return "User exist already";
        } else {
            log.error("Server internal Error");
            return "Server internal Error, check logs";
        }
    }
    /**
     * Metodo para eliminar un usuario en Keycloak
     * */
    @Override
    public void deleteUser(String userId) {
        KeycloakProvider.getUserResource().get(userId).remove();
    }


    /**
     * Metodo para actualizar un usuario en Keycloak
     * @return void
     * */
    @Override
    public void updateUser(String userId, @NonNull UserDto userDto) {

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(OAuth2Constants.PASSWORD);
        credentialRepresentation.setValue(userDto.getPassword());

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEmail(userDto.getEmail());
        userRepresentation.setFirstName(userDto.getFirstName());
        userRepresentation.setLastName(userDto.getLastName());
        userRepresentation.setUsername(userDto.getUsername());
        userRepresentation.setEmailVerified(true);
        userRepresentation.setEnabled(true);

        userRepresentation.setCredentials(Collections.singletonList(credentialRepresentation));

        UserResource usersResource = KeycloakProvider.getUserResource().get(userId);
        usersResource.update(userRepresentation);
    }
}

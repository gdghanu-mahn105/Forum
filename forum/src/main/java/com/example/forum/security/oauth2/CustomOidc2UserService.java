package com.example.forum.security.oauth2;

import com.example.forum.common.constant.AppConstants;
import com.example.forum.common.constant.MessageConstants;
import com.example.forum.entity.Role;
import com.example.forum.entity.UserEntity;
import com.example.forum.repository.RoleRepository;
import com.example.forum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CustomOidc2UserService extends OidcUserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        OidcUser oidcUser= super.loadUser(userRequest);

        String provider= userRequest.getClientRegistration().getRegistrationId();

        String providerId= oidcUser.getName();
        String email= oidcUser.getAttribute("email");
        String name = oidcUser.getAttribute("name");
        String avatarUrl = oidcUser.getAttribute("picture");

        Optional<UserEntity> optionalUser = userRepo.findByProviderAndProviderId(provider,providerId);
        // UserOauth login, save user information
        if(optionalUser.isEmpty()) {
            Role userRole = roleRepo.findByName(AppConstants.ROLE_USER)
                    .orElseThrow(()-> new RuntimeException(MessageConstants.ROLE_NOT_FOUND));

            UserEntity newUser = new UserEntity();
            newUser.setUserName(name);
            newUser.setEmail(email);
            newUser.setIsVerified(true);
            newUser.setUserPassword(null);
            newUser.setProvider(provider);
            newUser.setProviderId(providerId);
            newUser.setAvatarUrl(avatarUrl);
            newUser.setRoles(Set.of(userRole));
            userRepo.save(newUser);
        } else{
            return oidcUser;
        }
        UserEntity user = userRepo.findByEmail(email)
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity();
                    newUser.setEmail(email);
                    newUser.setUserName(name);
                    newUser.setProvider(provider);
                    newUser.setProviderId(providerId);
                    return userRepo.save(newUser);
                });


        return oidcUser;

    }
}

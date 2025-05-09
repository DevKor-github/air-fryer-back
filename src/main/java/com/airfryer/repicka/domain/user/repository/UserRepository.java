package com.airfryer.repicka.domain.user.repository;

import com.airfryer.repicka.domain.user.entity.LoginMethod;
import com.airfryer.repicka.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOauthIdAndLoginMethod(String oauthId, LoginMethod loginMethod);

}

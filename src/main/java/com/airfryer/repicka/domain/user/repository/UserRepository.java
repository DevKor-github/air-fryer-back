package com.airfryer.repicka.domain.user.repository;

import com.airfryer.repicka.domain.user.entity.LoginMethod;
import com.airfryer.repicka.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>
{
    // oauthId와 로그인 방식으로 사용자 찾기
    Optional<User> findByEmailAndLoginMethod(String email, LoginMethod loginMethod);
}

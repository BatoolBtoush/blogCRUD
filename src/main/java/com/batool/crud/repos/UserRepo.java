package com.batool.crud.repos;

import com.batool.crud.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long> {
    User findByEmail(String email);

    boolean existsByEmail(String email);

    User findByEmailIgnoreCase(String email);

}

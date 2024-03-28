package com.dfs.distributedfilesharing;

import com.dfs.distributedfilesharing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}

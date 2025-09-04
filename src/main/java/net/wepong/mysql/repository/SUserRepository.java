package net.wepong.mysql.repository;

import net.wepong.mysql.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SUserRepository extends JpaRepository<UserEntity, String> {
    // 이름과 전화번호로 조회
    Optional<UserEntity> findByUserNameAndPhoneNumber(String userName, String phoneNumber);
}

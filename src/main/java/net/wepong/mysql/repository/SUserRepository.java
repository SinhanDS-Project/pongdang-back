package net.wepong.mysql.repository;

import net.wepong.mysql.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SUserRepository extends JpaRepository<UserEntity, String> {
    // 이름과 전화번호로 조회
    Optional<UserEntity> findByUserNameAndPhoneNumber(String userName, String phoneNumber);

    // 보유 포인트에서 차감 (원자적 업데이트)
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE BettingUser u " +
            "SET u.pointBalance = u.pointBalance - :amount " +
            "WHERE u.uid = :uid AND u.pointBalance >= :amount")
    int tryDeductPoint(@Param("uid") String uid, @Param("amount") int amount);

    // 포인트 추가 (보상 처리용)
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE BettingUser u " +
            "SET u.pointBalance = u.pointBalance + :amount " +
            "WHERE u.uid = :uid")
    int addPoint(@Param("uid") String uid, @Param("amount") int amount);
}


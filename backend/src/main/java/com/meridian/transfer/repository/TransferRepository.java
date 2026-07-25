package com.meridian.transfer.repository;

import com.meridian.transfer.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    Optional<Transfer> findTopByOrderByCreatedAtDesc();
    Optional<Transfer> findTopByOwnerUsernameOrderByCreatedAtDesc(String ownerUsername);
    Optional<Transfer> findByIdAndOwnerUsername(Long id, String ownerUsername);
    List<Transfer> findByOwnerUsernameOrderByCreatedAtDesc(String ownerUsername);

    /** Sum of amountSent for everything this account has sent since a given instant — used to
     *  enforce daily/monthly cumulative caps (in the account's own currency). */
    @Query("SELECT COALESCE(SUM(t.amountSent), 0) FROM Transfer t " +
            "WHERE t.ownerUsername = :ownerUsername AND t.createdAt >= :since")
    BigDecimal sumAmountSentSince(@Param("ownerUsername") String ownerUsername, @Param("since") Instant since);
}

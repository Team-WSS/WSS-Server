package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.WithdrawalReason;

@Repository
public interface WithdrawalReasonRepository extends JpaRepository<WithdrawalReason, Long> {
}

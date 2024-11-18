package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawalReason {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long withdrawalReasonId;

    @Column(columnDefinition = "varchar(80)", nullable = false)
    private String withdrawalReasonContent;

    private WithdrawalReason(String withdrawalReasonContent) {
        this.withdrawalReasonContent = withdrawalReasonContent;
    }

    public static WithdrawalReason create(String withdrawalReasonContent) {
        return new WithdrawalReason(withdrawalReasonContent);
    }
}

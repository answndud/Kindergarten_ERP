package com.erp.domain.member.entity;

import com.erp.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "member_social_account",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_social_account_provider_provider_id", columnNames = {"provider", "provider_id"}),
                @UniqueConstraint(name = "uk_member_social_account_member_provider", columnNames = {"member_id", "provider"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSocialAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private MemberAuthProvider provider;

    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    public static MemberSocialAccount create(Member member, MemberAuthProvider provider, String providerId) {
        MemberSocialAccount socialAccount = new MemberSocialAccount();
        socialAccount.member = member;
        socialAccount.provider = provider;
        socialAccount.providerId = providerId;
        return socialAccount;
    }

    public boolean isSameProvider(MemberAuthProvider provider) {
        return this.provider == provider;
    }

    public boolean matches(MemberAuthProvider provider, String providerId) {
        return this.provider == provider && this.providerId.equals(providerId);
    }
}

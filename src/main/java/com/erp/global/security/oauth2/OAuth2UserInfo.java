package com.erp.global.security.oauth2;

import com.erp.domain.member.entity.MemberAuthProvider;

public interface OAuth2UserInfo {
    MemberAuthProvider getProvider();

    String getProviderId();

    String getEmail();

    String getName();
}

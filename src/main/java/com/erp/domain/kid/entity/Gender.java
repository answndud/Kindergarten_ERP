package com.erp.domain.kid.entity;

import lombok.Getter;

/**
 * 성별 enum
 */
@Getter
public enum Gender {
    MALE("남자"),
    FEMALE("여자");

    private final String description;

    Gender(String description) {
        this.description = description;
    }
}

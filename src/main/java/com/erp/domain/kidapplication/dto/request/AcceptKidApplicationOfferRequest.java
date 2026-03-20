package com.erp.domain.kidapplication.dto.request;

import com.erp.domain.kid.entity.Relationship;

public record AcceptKidApplicationOfferRequest(
        Relationship relationship
) {
    public Relationship relationshipOrDefault() {
        return relationship == null ? Relationship.FATHER : relationship;
    }
}

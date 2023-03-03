package com.study.codingswamp.application.auth;

import com.study.codingswamp.domain.member.entity.Role;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class MemberPayload {

    private final Long id;
    private final Role role;

    public MemberPayload(Long id, Role role) {
        this.id = id;
        this.role = role;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}

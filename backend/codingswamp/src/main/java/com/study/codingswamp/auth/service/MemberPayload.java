package com.study.codingswamp.auth.service;

import com.study.codingswamp.member.domain.Role;
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

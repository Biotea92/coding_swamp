package com.study.codingswamp.domain.study.dto.response;

import com.study.codingswamp.domain.study.dto.request.CursorRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageCursor<T> {

    private final CursorRequest nextCursorRequest;
    private final List<T> body;
}

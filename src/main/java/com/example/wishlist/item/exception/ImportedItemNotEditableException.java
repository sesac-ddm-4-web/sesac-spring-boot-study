package com.example.wishlist.item.exception;

import com.example.wishlist.common.exception.ConflictException;

public class ImportedItemNotEditableException extends ConflictException {

    public ImportedItemNotEditableException(Long id) {
        super("외부에서 가져온 아이템의 이름·가격은 수정할 수 없습니다. id=" + id);
    }
}

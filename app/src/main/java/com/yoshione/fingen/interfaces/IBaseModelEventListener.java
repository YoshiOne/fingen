package com.yoshione.fingen.interfaces;

import com.yoshione.fingen.model.BaseModel;

public interface IBaseModelEventListener {
    void onItemClick(BaseModel item);
    void onSelectionChange(int selectedCount);
}

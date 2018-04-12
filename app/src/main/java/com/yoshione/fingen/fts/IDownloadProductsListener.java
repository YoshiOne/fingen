package com.yoshione.fingen.fts;

import com.yoshione.fingen.model.ProductEntry;

import java.util.List;

/**
 * Created by slv on 31.01.2018.
 */

public interface IDownloadProductsListener {
    public void onDownload(List<ProductEntry> productEntries);

    public void onFailure(Throwable t);
}

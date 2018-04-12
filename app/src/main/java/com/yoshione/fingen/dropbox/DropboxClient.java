package com.yoshione.fingen.dropbox;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slv on 01.11.2016.
 *
 */

public class DropboxClient {

    public static DbxClientV2 getClient(String ACCESS_TOKEN) {
        // Create Dropbox client
        DbxRequestConfig config = new DbxRequestConfig("dropbox/sample-app");
        return new DbxClientV2(config, ACCESS_TOKEN);
    }

    public static List<Metadata> getListFiles(DbxClientV2 client) {
        ListFolderResult result = null;
        List<Metadata> metadataList = new ArrayList<>();
        try {
            result = client.files().listFolder("");
        } catch (DbxException e) {
            return metadataList;
        }

        while (true) {
            metadataList.addAll(result.getEntries());

            if (!result.getHasMore()) {
                break;
            }

            try {
                result = client.files().listFolderContinue(result.getCursor());
            } catch (DbxException e) {
                break;
            }
        }
        return metadataList;
    }
}


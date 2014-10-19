package com.protegra.sdecdemo.helpers;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class SQLiteHelper {
    private static final String DATABASE_NAME = "sdec_demo_sync";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_SPEAKERS  = "speakers";

    private static final String SPEAKERS_KEY_ID = "_id";
    private static final String SPEAKERS_KEY_NAME = "name";
    private static final String SPEAKERS_KEY_PHOTO_SMALL = "photo_small";
    private static final String SPEAKERS_KEY_VERSION = "__version";
    private static final String SPEAKERS_KEY_DELETED = "__deleted";

    public static void createLocalStore(MobileServiceClient client) throws MobileServiceLocalStoreException, InterruptedException, ExecutionException {
        SQLiteLocalStore localStore = new SQLiteLocalStore(client.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        defineSpeakerTable(localStore);

        SimpleSyncHandler handler = new SimpleSyncHandler();
        MobileServiceSyncContext syncContext = client.getSyncContext();
        syncContext.initialize(localStore, handler).get();
    }

    public static void defineSpeakerTable(SQLiteLocalStore localStore) throws MobileServiceLocalStoreException {
        Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();

        tableDefinition.put(SPEAKERS_KEY_ID, ColumnDataType.String);
        tableDefinition.put(SPEAKERS_KEY_NAME, ColumnDataType.String);
        tableDefinition.put(SPEAKERS_KEY_PHOTO_SMALL, ColumnDataType.String);
        tableDefinition.put(SPEAKERS_KEY_VERSION, ColumnDataType.String);
        tableDefinition.put(SPEAKERS_KEY_DELETED, ColumnDataType.Boolean);

        localStore.defineTable(TABLE_SPEAKERS , tableDefinition);
    }
}

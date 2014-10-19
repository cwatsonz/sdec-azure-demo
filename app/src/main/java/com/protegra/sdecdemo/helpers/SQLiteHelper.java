package com.protegra.sdecdemo.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.protegra.sdecdemo.data.Speaker;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static SQLiteHelper sInstance;
    private SQLiteDatabase mDatabase;
    private final AtomicInteger mOpenCounter = new AtomicInteger();

    private static final String DATABASE_NAME = "sdec_demo";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_SPEAKERS = "speakers";

    private static final String SPEAKERS_KEY_ID = "_id";
    private static final String SPEAKERS_KEY_NAME = "name";
    private static final String SPEAKERS_KEY_PHOTO_SMALL = "photo_small";

    private static final String CREATE_SPEAKERS_TABLE = "CREATE TABLE " + TABLE_SPEAKERS + "("
            + SPEAKERS_KEY_ID + " TEXT PRIMARY KEY,"
            + SPEAKERS_KEY_NAME + " TEXT,"
            + SPEAKERS_KEY_PHOTO_SMALL + " TEXT,"
            + ")";

    public static synchronized SQLiteHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new SQLiteHelper(context.getApplicationContext());
        }

        return sInstance;
    }

    public synchronized SQLiteDatabase openDatabase() {
        if (mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            mDatabase = sInstance.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        if (mOpenCounter.decrementAndGet() == 0) {
            // Closing database
            mDatabase.close();
        }
    }

    private SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_SPEAKERS_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPEAKERS);

            onCreate(db);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addSpeaker(Speaker speaker) {
        SQLiteDatabase db = this.openDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(SPEAKERS_KEY_ID, speaker.id);
            values.put(SPEAKERS_KEY_NAME, speaker.name);
            values.put(SPEAKERS_KEY_PHOTO_SMALL, speaker.photo_small);

            db.insertOrThrow(TABLE_SPEAKERS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.closeDatabase();
        }
    }

    public void bulkAddSpeakers(Speaker... speakers) {
        SQLiteDatabase db = this.openDatabase();

        try {
            String sql = "INSERT INTO " + TABLE_SPEAKERS
                    + "("
                    + SPEAKERS_KEY_ID + ", "
                    + SPEAKERS_KEY_NAME + ", "
                    + SPEAKERS_KEY_PHOTO_SMALL
                    + ")"
                    + " VALUES (?, ?, ?)";
            SQLiteStatement statement = db.compileStatement(sql);

            db.beginTransaction();

            for (Speaker speaker : speakers) {
                statement.clearBindings();
                statement.bindString(1, speaker.id);
                statement.bindString(2, speaker.name);
                statement.bindString(3, speaker.photo_small != null ? speaker.photo_small : "");
                statement.execute();
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            this.closeDatabase();
        }
    }

    public Speaker getSpeaker(String id) {
        SQLiteDatabase db = this.openDatabase();
        Speaker speaker = new Speaker();

        try {
            Cursor cursor = db.query(TABLE_SPEAKERS, new String[]{
                            SPEAKERS_KEY_ID,
                            SPEAKERS_KEY_NAME,
                            SPEAKERS_KEY_PHOTO_SMALL,
                    }, SPEAKERS_KEY_ID + "=?",
                    new String[]{id}, null, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();
                speaker = getSpeakerFromCursor(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.closeDatabase();
        }

        return speaker;
    }

    private Speaker getSpeakerFromCursor(Cursor cursor) {
        Speaker speaker = new Speaker();

        try {
            if (cursor != null) {

                speaker.id = cursor.getString(cursor.getColumnIndexOrThrow(SPEAKERS_KEY_ID));
                speaker.name = cursor.getString(cursor.getColumnIndexOrThrow(SPEAKERS_KEY_NAME));
                speaker.photo_small = cursor.getString(cursor.getColumnIndexOrThrow(SPEAKERS_KEY_PHOTO_SMALL));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return speaker;
    }

    public List<Speaker> getAllSpeakers() {
        SQLiteDatabase db = this.openDatabase();
        List<Speaker> list = new ArrayList<Speaker>();
        String selectQuery = "SELECT  * FROM " + TABLE_SPEAKERS
                + " ORDER BY " + SPEAKERS_KEY_NAME + " ASC";

        try {
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    Speaker speaker = getSpeakerFromCursor(cursor);
                    list.add(speaker);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.closeDatabase();
        }

        return list;
    }

    public int updateSpeaker(Speaker speaker) {
        SQLiteDatabase db = this.openDatabase();
        int result = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(SPEAKERS_KEY_NAME, speaker.name);
            values.put(SPEAKERS_KEY_PHOTO_SMALL, speaker.photo_small);

            result = db.update(TABLE_SPEAKERS, values, SPEAKERS_KEY_ID + " = ?",
                    new String[]{String.valueOf(speaker.id)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.closeDatabase();
        }

        return result;
    }

    public void deleteSpeaker(Speaker speaker) {
        SQLiteDatabase db = this.openDatabase();

        try {
            db.delete(TABLE_SPEAKERS, SPEAKERS_KEY_ID + " = ?",
                    new String[]{String.valueOf(speaker.id)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.closeDatabase();
        }
    }

    public void removeAllSpeakers() {
        SQLiteDatabase db = this.openDatabase();

        try {
            db.delete(TABLE_SPEAKERS, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.closeDatabase();
        }
    }

    public int getSpeakersCount() {
        SQLiteDatabase db = this.openDatabase();
        int count = 0;
        String countQuery = "SELECT  * FROM " + TABLE_SPEAKERS;

        try {
            Cursor cursor = db.rawQuery(countQuery, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.closeDatabase();
        }

        return count;
    }
}
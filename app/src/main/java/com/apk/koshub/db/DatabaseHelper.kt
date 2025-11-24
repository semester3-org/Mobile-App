package com.apk.koshub.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.apk.koshub.models.User

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "koshub.db"
        private const val DATABASE_VERSION = 3

        // USER TABLE
        private const val TABLE_USER = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_FULLNAME = "full_name"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_USERTYPE = "user_type"
        private const val COLUMN_PROFILE_IMAGE = "profile_image"
    }

    override fun onCreate(db: SQLiteDatabase) {

        val createUserTable = """
            CREATE TABLE $TABLE_USER (
                $COLUMN_ID INTEGER PRIMARY KEY,
                $COLUMN_USERNAME TEXT,
                $COLUMN_EMAIL TEXT UNIQUE,
                $COLUMN_FULLNAME TEXT,
                $COLUMN_PHONE TEXT,
                $COLUMN_USERTYPE TEXT,
                $COLUMN_PROFILE_IMAGE TEXT
            );
        """.trimIndent()

        db.execSQL(createUserTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_USER ADD COLUMN $COLUMN_PROFILE_IMAGE TEXT;")
        }
    }

    // ====================== USER FUNCTIONS =======================

    fun insertUser(user: User): Boolean {
        val db = writableDatabase

        // Hapus user lama biar tidak duplicate
        db.delete(TABLE_USER, null, null)

        val values = ContentValues().apply {
            put(COLUMN_ID, user.id)
            put(COLUMN_USERNAME, user.username)
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_FULLNAME, user.full_name)
            put(COLUMN_PHONE, user.phone)
            put(COLUMN_USERTYPE, user.user_type)
            put(COLUMN_PROFILE_IMAGE, user.profile_image)
        }

        val result = db.insert(TABLE_USER, null, values)
        db.close()
        return result != -1L
    }

    fun getUser(): User? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USER LIMIT 1", null)

        return if (cursor.moveToFirst()) {
            User(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                full_name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULLNAME)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                user_type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERTYPE)),
                profile_image = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_IMAGE))
            )
        } else null
    }

    fun updateUser(user: User): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_USERNAME, user.username)
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_FULLNAME, user.full_name)
            put(COLUMN_PHONE, user.phone)
            put(COLUMN_PROFILE_IMAGE, user.profile_image)
        }

        val result = db.update(
            TABLE_USER,
            values,
            "$COLUMN_ID = ?",
            arrayOf(user.id.toString())
        )

        db.close()
        return result
    }

    fun logoutUser(): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_USER, null, null)
        db.close()
        return result > 0
    }
}

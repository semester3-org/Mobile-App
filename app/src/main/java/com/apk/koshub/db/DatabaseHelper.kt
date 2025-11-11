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
        private const val DATABASE_VERSION = 2 // ✅ Naikkan versi database

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
        // ✅ Hanya tambahkan kolom baru, tidak drop tabel agar data tidak hilang
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_USER ADD COLUMN $COLUMN_PROFILE_IMAGE TEXT;")
        }
    }

    /** Simpan user hasil login (hanya satu yang aktif) */
    fun insertUser(user: User) {
        writableDatabase.use { db ->
            db.delete(TABLE_USER, null, null) // hapus data lama

            val values = ContentValues().apply {
                put(COLUMN_ID, user.id)
                put(COLUMN_USERNAME, user.username)
                put(COLUMN_EMAIL, user.email)
                put(COLUMN_FULLNAME, user.full_name)
                put(COLUMN_PHONE, user.phone)
                put(COLUMN_USERTYPE, user.user_type)
                put(COLUMN_PROFILE_IMAGE, user.profile_image)
            }

            db.insert(TABLE_USER, null, values)
        }
    }

    /** Update data user (misal dari profil) */
    fun updateUser(user: User): Int {
        return writableDatabase.use { db ->
            val values = ContentValues().apply {
                put(COLUMN_USERNAME, user.username)
                put(COLUMN_EMAIL, user.email)
                put(COLUMN_FULLNAME, user.full_name)
                put(COLUMN_PHONE, user.phone)
                put(COLUMN_USERTYPE, user.user_type)
                put(COLUMN_PROFILE_IMAGE, user.profile_image)
            }
            db.update(TABLE_USER, values, "$COLUMN_ID = ?", arrayOf(user.id.toString()))
        }
    }

    /** Ambil data user aktif */
    fun getUser(): User? {
        readableDatabase.use { db ->
            db.rawQuery("SELECT * FROM $TABLE_USER LIMIT 1", null).use { cursor ->
                if (cursor.moveToFirst()) {
                    return User(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                        email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                        full_name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULLNAME)),
                        phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                        user_type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERTYPE)),
                        profile_image = cursor.getString(
                            cursor.getColumnIndexOrThrow(COLUMN_PROFILE_IMAGE)
                        )
                    )
                }
            }
        }
        return null
    }

    /** Hapus semua user (logout) */
    fun logoutUser() {
        writableDatabase.use { db ->
            db.delete(TABLE_USER, null, null)
        }
    }

    /** Cek apakah user sudah login (tersimpan di SQLite) */
    fun isUserLoggedIn(): Boolean {
        readableDatabase.use { db ->
            db.rawQuery("SELECT COUNT(*) FROM $TABLE_USER", null).use { cursor ->
                return if (cursor.moveToFirst()) cursor.getInt(0) > 0 else false
            }
        }
    }
}

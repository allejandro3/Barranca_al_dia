package com.example.reportesciudadano.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.reportesciudadano.model.Comment
import com.example.reportesciudadano.model.NotificationItem
import com.example.reportesciudadano.model.Report
import com.example.reportesciudadano.model.User

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "reportes_ciudadano.db"
        private const val DATABASE_VERSION = 6

        private const val TABLE_REPORTS = "reports"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_USER = "user_name"
        private const val COLUMN_TIME_AGO = "time_ago"
        private const val COLUMN_SEED = "seed"
        private const val COLUMN_HEIGHT = "height"
        private const val COLUMN_LAT = "lat"
        private const val COLUMN_LNG = "lng"
        private const val COLUMN_PHOTO = "photo"
        private const val COLUMN_MINE = "mine"
        private const val COLUMN_STATUS = "status"
        private const val COLUMN_TIMESTAMP = "timestamp"
        private const val COLUMN_REASON = "reason"
        private const val COLUMN_HOURS = "resolution_hours"
        private const val COLUMN_SUPPORTS = "supports"
        private const val COLUMN_HAS_PHOTO = "has_photo"
        private const val COLUMN_IS_SUPPORTED = "is_supported"
        private const val COLUMN_DISLIKES = "dislikes"
        private const val COLUMN_IS_DISLIKED = "is_disliked"

        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_EMAIL = "email"
        private const val COLUMN_USER_NAME = "name"
        private const val COLUMN_USER_PASS = "password"
        private const val COLUMN_USER_CREATED = "created_at"

        private const val TABLE_NOTIFICATIONS = "notifications"
        private const val COLUMN_NOTIF_ID = "id"
        private const val COLUMN_NOTIF_TITLE = "title"
        private const val COLUMN_NOTIF_MESSAGE = "message"
        private const val COLUMN_NOTIF_TYPE = "type"
        private const val COLUMN_NOTIF_TIME = "timestamp"

        private const val TABLE_COMMENTS = "comments"
        private const val COLUMN_COMMENT_ID = "comment_id"
        private const val COLUMN_COMMENT_REPORT = "report_id"
        private const val COLUMN_COMMENT_USER = "comment_user"
        private const val COLUMN_COMMENT_TEXT = "comment_text"
        private const val COLUMN_COMMENT_TIME = "comment_time"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createReportsTable = """
            CREATE TABLE $TABLE_REPORTS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_USER TEXT NOT NULL,
                $COLUMN_TIME_AGO TEXT,
                $COLUMN_SEED TEXT,
                $COLUMN_HEIGHT INTEGER,
                $COLUMN_LAT REAL,
                $COLUMN_LNG REAL,
                $COLUMN_PHOTO TEXT,
                $COLUMN_MINE INTEGER,
                $COLUMN_STATUS TEXT,
                $COLUMN_TIMESTAMP INTEGER,
                $COLUMN_REASON TEXT,
                $COLUMN_HOURS INTEGER,
                $COLUMN_SUPPORTS INTEGER,
                $COLUMN_HAS_PHOTO INTEGER,
                $COLUMN_IS_SUPPORTED INTEGER DEFAULT 0,
                $COLUMN_DISLIKES INTEGER DEFAULT 0,
                $COLUMN_IS_DISLIKED INTEGER DEFAULT 0
            )
        """.trimIndent()

        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_EMAIL TEXT PRIMARY KEY,
                $COLUMN_USER_NAME TEXT NOT NULL,
                $COLUMN_USER_PASS TEXT NOT NULL,
                $COLUMN_USER_CREATED INTEGER
            )
        """.trimIndent()

        val createNotifTable = """
            CREATE TABLE $TABLE_NOTIFICATIONS (
                $COLUMN_NOTIF_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOTIF_TITLE TEXT,
                $COLUMN_NOTIF_MESSAGE TEXT NOT NULL,
                $COLUMN_NOTIF_TYPE TEXT NOT NULL,
                $COLUMN_NOTIF_TIME INTEGER
            )
        """.trimIndent()

        val createCommentsTable = """
            CREATE TABLE $TABLE_COMMENTS (
                $COLUMN_COMMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_COMMENT_REPORT TEXT NOT NULL,
                $COLUMN_COMMENT_USER TEXT NOT NULL,
                $COLUMN_COMMENT_TEXT TEXT NOT NULL,
                $COLUMN_COMMENT_TIME INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(createReportsTable)
        db.execSQL(createUsersTable)
        db.execSQL(createNotifTable)
        db.execSQL(createCommentsTable)

        insertDefaultNotifications(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            val createUsersTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                    $COLUMN_USER_EMAIL TEXT PRIMARY KEY,
                    $COLUMN_USER_NAME TEXT NOT NULL,
                    $COLUMN_USER_PASS TEXT NOT NULL,
                    $COLUMN_USER_CREATED INTEGER
                )
            """.trimIndent()
            db.execSQL(createUsersTable)
        }
        if (oldVersion < 3) {
            val createNotifTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_NOTIFICATIONS (
                    $COLUMN_NOTIF_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_NOTIF_TITLE TEXT,
                    $COLUMN_NOTIF_MESSAGE TEXT NOT NULL,
                    $COLUMN_NOTIF_TYPE TEXT NOT NULL,
                    $COLUMN_NOTIF_TIME INTEGER
                )
            """.trimIndent()
            db.execSQL(createNotifTable)
            insertDefaultNotifications(db)
        }
        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE $TABLE_REPORTS ADD COLUMN $COLUMN_IS_SUPPORTED INTEGER DEFAULT 0")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (oldVersion < 5) {
            try {
                db.execSQL("ALTER TABLE $TABLE_REPORTS ADD COLUMN $COLUMN_DISLIKES INTEGER DEFAULT 0")
                db.execSQL("ALTER TABLE $TABLE_REPORTS ADD COLUMN $COLUMN_IS_DISLIKED INTEGER DEFAULT 0")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (oldVersion < 6) {
            val createCommentsTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_COMMENTS (
                    $COLUMN_COMMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_COMMENT_REPORT TEXT NOT NULL,
                    $COLUMN_COMMENT_USER TEXT NOT NULL,
                    $COLUMN_COMMENT_TEXT TEXT NOT NULL,
                    $COLUMN_COMMENT_TIME INTEGER NOT NULL
                )
            """.trimIndent()
            db.execSQL(createCommentsTable)
        }
    }

    private fun insertDefaultNotifications(db: SQLiteDatabase) {
        val now = System.currentTimeMillis()
        val defaultNotifs = listOf(
            Triple("Atendido", "Tu reporte “Bache profundo” fue atendido.", "ATTENDED"),
            Triple("Apoyo", "3 vecinos apoyaron tu reporte.", "SUPPORT"),
            Triple("Aviso", "Nuevo reporte cerca de ti.", "NEARBY")
        )
        for ((title, message, type) in defaultNotifs) {
            val values = ContentValues().apply {
                put(COLUMN_NOTIF_TITLE, title)
                put(COLUMN_NOTIF_MESSAGE, message)
                put(COLUMN_NOTIF_TYPE, type)
                put(COLUMN_NOTIF_TIME, now)
            }
            db.insert(TABLE_NOTIFICATIONS, null, values)
        }
    }

    fun insertNotification(title: String, message: String, type: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOTIF_TITLE, title)
            put(COLUMN_NOTIF_MESSAGE, message)
            put(COLUMN_NOTIF_TYPE, type)
            put(COLUMN_NOTIF_TIME, System.currentTimeMillis())
        }
        db.insert(TABLE_NOTIFICATIONS, null, values)
    }

    fun getAllNotifications(): List<NotificationItem> {
        val list = mutableListOf<NotificationItem>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NOTIFICATIONS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_NOTIF_TIME DESC"
        )
        var supportCount = 0
        var latestSupportTime = 0L

        cursor.use { c ->
            while (c.moveToNext()) {
                val type = c.getString(c.getColumnIndexOrThrow(COLUMN_NOTIF_TYPE))
                if (type == "SUPPORT") {
                    supportCount++
                    val t = c.getLong(c.getColumnIndexOrThrow(COLUMN_NOTIF_TIME))
                    if (t > latestSupportTime) latestSupportTime = t
                } else {
                    val item = NotificationItem(
                        id = c.getLong(c.getColumnIndexOrThrow(COLUMN_NOTIF_ID)),
                        title = c.getString(c.getColumnIndexOrThrow(COLUMN_NOTIF_TITLE)) ?: "Aviso",
                        message = c.getString(c.getColumnIndexOrThrow(COLUMN_NOTIF_MESSAGE)),
                        type = type,
                        timestamp = c.getLong(c.getColumnIndexOrThrow(COLUMN_NOTIF_TIME))
                    )
                    list.add(item)
                }
            }
        }

        if (supportCount > 0) {
            val aggregatedItem = NotificationItem(
                id = -999L,
                title = "Apoyos Comunitarios",
                message = "Tienes $supportCount interacciones de apoyo acumuladas en tus reportes.",
                type = "SUPPORT",
                timestamp = latestSupportTime
            )
            list.add(aggregatedItem)
        }

        return list.sortedByDescending { it.timestamp }
    }

    fun registerUser(name: String, email: String, pass: String): Boolean {
        if (userExists(email)) return false

        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_EMAIL, email.trim().lowercase())
            put(COLUMN_USER_NAME, name.trim())
            put(COLUMN_USER_PASS, pass)
            put(COLUMN_USER_CREATED, System.currentTimeMillis())
        }
        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    fun loginUser(email: String, pass: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_NAME, COLUMN_USER_EMAIL),
            "$COLUMN_USER_EMAIL = ? AND $COLUMN_USER_PASS = ?",
            arrayOf(email.trim().lowercase(), pass),
            null,
            null,
            null
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                val name = c.getString(c.getColumnIndexOrThrow(COLUMN_USER_NAME))
                val userEmail = c.getString(c.getColumnIndexOrThrow(COLUMN_USER_EMAIL))
                return User(name = name, email = userEmail)
            }
        }
        return null
    }

    fun userExists(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_EMAIL),
            "$COLUMN_USER_EMAIL = ?",
            arrayOf(email.trim().lowercase()),
            null,
            null,
            null
        )
        cursor.use { c ->
            return c.count > 0
        }
    }

    fun getPasswordForEmail(email: String): String? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_PASS),
            "$COLUMN_USER_EMAIL = ?",
            arrayOf(email.trim().lowercase()),
            null,
            null,
            null
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                return c.getString(c.getColumnIndexOrThrow(COLUMN_USER_PASS))
            }
        }
        return null
    }

    fun insertReport(report: Report): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, report.id)
            put(COLUMN_TITLE, report.t)
            put(COLUMN_CATEGORY, report.c)
            put(COLUMN_DESCRIPTION, report.d)
            put(COLUMN_USER, report.u)
            put(COLUMN_TIME_AGO, report.p)
            put(COLUMN_SEED, report.s)
            put(COLUMN_HEIGHT, report.h)
            put(COLUMN_LAT, report.lat)
            put(COLUMN_LNG, report.lng)
            put(COLUMN_PHOTO, report.photo)
            put(COLUMN_MINE, if (report.mine) 1 else 0)
            put(COLUMN_STATUS, report.st)
            put(COLUMN_TIMESTAMP, report.ts)
            put(COLUMN_REASON, report.rs)
            put(COLUMN_HOURS, report.hrs)
            put(COLUMN_SUPPORTS, report.sup)
            put(COLUMN_HAS_PHOTO, if (report.photo != null) 1 else 0)
            put(COLUMN_IS_SUPPORTED, if (report.isSupported) 1 else 0)
            put(COLUMN_DISLIKES, report.dislikes)
            put(COLUMN_IS_DISLIKED, if (report.isDisliked) 1 else 0)
        }
        val result = db.insertWithOnConflict(TABLE_REPORTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        if (result != -1L) {
            insertNotification(
                title = "Nuevo Reporte",
                message = "Tu reporte '${report.t}' fue registrado en la base de datos.",
                type = "NEARBY"
            )
        }
        return result != -1L
    }

    fun getAllMineReports(): List<Report> {
        val list = mutableListOf<Report>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_REPORTS,
            null,
            "$COLUMN_MINE = ?",
            arrayOf("1"),
            null,
            null,
            "$COLUMN_TIMESTAMP DESC"
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                val r = Report(
                    id = c.getString(c.getColumnIndexOrThrow(COLUMN_ID)),
                    t = c.getString(c.getColumnIndexOrThrow(COLUMN_TITLE)),
                    c = c.getString(c.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                    d = c.getString(c.getColumnIndexOrThrow(COLUMN_DESCRIPTION)) ?: "",
                    u = c.getString(c.getColumnIndexOrThrow(COLUMN_USER)),
                    p = c.getString(c.getColumnIndexOrThrow(COLUMN_TIME_AGO)) ?: "Hace un momento",
                    s = c.getString(c.getColumnIndexOrThrow(COLUMN_SEED)) ?: "mine",
                    h = c.getInt(c.getColumnIndexOrThrow(COLUMN_HEIGHT)),
                    lat = c.getDouble(c.getColumnIndexOrThrow(COLUMN_LAT)),
                    lng = c.getDouble(c.getColumnIndexOrThrow(COLUMN_LNG)),
                    photo = if (c.isNull(c.getColumnIndexOrThrow(COLUMN_PHOTO))) null else c.getString(c.getColumnIndexOrThrow(COLUMN_PHOTO)),
                    mine = true,
                    st = c.getString(c.getColumnIndexOrThrow(COLUMN_STATUS)) ?: "Pendiente",
                    ts = c.getLong(c.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                    rs = c.getString(c.getColumnIndexOrThrow(COLUMN_REASON)) ?: "Riesgo de accidente",
                    hrs = if (c.isNull(c.getColumnIndexOrThrow(COLUMN_HOURS))) null else c.getInt(c.getColumnIndexOrThrow(COLUMN_HOURS)),
                    sup = c.getInt(c.getColumnIndexOrThrow(COLUMN_SUPPORTS)),
                    ph = c.getInt(c.getColumnIndexOrThrow(COLUMN_HAS_PHOTO)) == 1,
                    isSupported = c.getInt(c.getColumnIndexOrThrow(COLUMN_IS_SUPPORTED)) == 1,
                    dislikes = c.getInt(c.getColumnIndexOrThrow(COLUMN_DISLIKES)),
                    isDisliked = c.getInt(c.getColumnIndexOrThrow(COLUMN_IS_DISLIKED)) == 1
                )
                list.add(r)
            }
        }
        return list
    }

    fun updateReportState(id: String, newSupports: Int, isSupported: Boolean, newDislikes: Int, isDisliked: Boolean, newStatus: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SUPPORTS, newSupports)
            put(COLUMN_IS_SUPPORTED, if (isSupported) 1 else 0)
            put(COLUMN_DISLIKES, newDislikes)
            put(COLUMN_IS_DISLIKED, if (isDisliked) 1 else 0)
            put(COLUMN_STATUS, newStatus)
        }
        db.update(TABLE_REPORTS, values, "$COLUMN_ID = ?", arrayOf(id))
        insertNotification(
            title = "Actualización Comunitaria",
            message = "Un reporte recibió una nueva interacción de apoyo/rechazo.",
            type = "SUPPORT"
        )
    }

    fun insertComment(reportId: String, userName: String, text: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_COMMENT_REPORT, reportId)
            put(COLUMN_COMMENT_USER, userName.ifBlank { "Usuario" })
            put(COLUMN_COMMENT_TEXT, text)
            put(COLUMN_COMMENT_TIME, System.currentTimeMillis())
        }
        return db.insert(TABLE_COMMENTS, null, values) != -1L
    }

    fun getCommentsForReport(reportId: String): List<Comment> {
        val list = mutableListOf<Comment>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_COMMENTS,
            null,
            "$COLUMN_COMMENT_REPORT = ?",
            arrayOf(reportId),
            null,
            null,
            "$COLUMN_COMMENT_TIME ASC"
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    Comment(
                        id = c.getLong(c.getColumnIndexOrThrow(COLUMN_COMMENT_ID)),
                        reportId = c.getString(c.getColumnIndexOrThrow(COLUMN_COMMENT_REPORT)),
                        userName = c.getString(c.getColumnIndexOrThrow(COLUMN_COMMENT_USER)),
                        text = c.getString(c.getColumnIndexOrThrow(COLUMN_COMMENT_TEXT)),
                        timestamp = c.getLong(c.getColumnIndexOrThrow(COLUMN_COMMENT_TIME))
                    )
                )
            }
        }
        return list
    }
}

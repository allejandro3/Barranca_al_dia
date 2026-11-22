package com.example.reportesciudadano.data

import android.content.Context
import android.content.SharedPreferences
import com.example.reportesciudadano.model.Comment
import com.example.reportesciudadano.model.NotificationItem
import com.example.reportesciudadano.model.Report
import com.example.reportesciudadano.model.SimReport
import com.example.reportesciudadano.model.SimulatedData
import com.example.reportesciudadano.model.User
import com.example.reportesciudadano.utils.NotificationHelper
import org.json.JSONArray
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class ReportsRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val notifier = NotificationHelper(context)
    private val prefs: SharedPreferences = context.getSharedPreferences("rc_prefs", Context.MODE_PRIVATE)
    private val MINE_KEY = "rc_mine"
    private val NOTIFIED_KEY = "rc_notified_nearby"

    private val SESSION_EMAIL = "rc_session_email"
    private val SESSION_NAME = "rc_session_name"
    private val SESSION_ACTIVE = "rc_session_active"

    private val _reports = mutableListOf<Report>()
    val reports: List<Report> get() = _reports.toList()

    val simulationData: List<SimReport> by lazy {
        SimulatedData.generateSimulation()
    }

    init {
        migrateSharedPreferencesToDatabase()
        loadReports()
    }

    fun loadReports() {
        _reports.clear()
        val dbMineList = dbHelper.getAllMineReports()
        _reports.addAll(dbMineList)
        _reports.addAll(SimulatedData.INITIAL_REPORTS)
    }

    fun addReport(report: Report) {
        _reports.add(0, report)
        dbHelper.insertReport(report)
        notifier.sendNotification(
            title = "Reporte Registrado",
            message = "Tu reporte '${report.t}' se guardó con éxito en la comunidad."
        )
    }

    fun toggleSupport(id: String) {
        val index = _reports.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = _reports[index]
            val newIsSupported = !item.isSupported
            val newSup = if (newIsSupported) item.sup + 1 else (item.sup - 1).coerceAtLeast(0)

            val newIsDisliked = if (newIsSupported) false else item.isDisliked
            val newDislikes = if (newIsSupported && item.isDisliked) (item.dislikes - 1).coerceAtLeast(0) else item.dislikes

            val netScore = newSup - newDislikes
            val newSt = if (netScore >= 5) "Verificado" else "Pendiente"

            _reports[index] = item.copy(
                sup = newSup,
                isSupported = newIsSupported,
                dislikes = newDislikes,
                isDisliked = newIsDisliked,
                st = newSt
            )
            dbHelper.updateReportState(id, newSup, newIsSupported, newDislikes, newIsDisliked, newSt)
        }
    }

    fun toggleDislike(id: String) {
        val index = _reports.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = _reports[index]
            val newIsDisliked = !item.isDisliked
            val newDislikes = if (newIsDisliked) item.dislikes + 1 else (item.dislikes - 1).coerceAtLeast(0)

            val newIsSupported = if (newIsDisliked) false else item.isSupported
            val newSup = if (newIsDisliked && item.isSupported) (item.sup - 1).coerceAtLeast(0) else item.sup

            val netScore = newSup - newDislikes
            val newSt = if (netScore >= 5) "Verificado" else "Pendiente"

            _reports[index] = item.copy(
                sup = newSup,
                isSupported = newIsSupported,
                dislikes = newDislikes,
                isDisliked = newIsDisliked,
                st = newSt
            )
            dbHelper.updateReportState(id, newSup, newIsSupported, newDislikes, newIsDisliked, newSt)
        }
    }

    fun getComments(reportId: String): List<Comment> {
        return dbHelper.getCommentsForReport(reportId)
    }

    fun addComment(reportId: String, userName: String, text: String): Boolean {
        return dbHelper.insertComment(reportId, userName, text)
    }

    fun registerUser(name: String, email: String, pass: String): Boolean {        val success = dbHelper.registerUser(name, email, pass)
        if (success) {
            notifier.sendNotification(
                title = "Bienvenido a Barranca al Día",
                message = "Tu cuenta ($email) se registró correctamente."
            )
        }
        return success
    }

    fun loginUser(email: String, pass: String): User? {
        return dbHelper.loginUser(email, pass)
    }

    fun saveSession(email: String, name: String) {
        prefs.edit()
            .putString(SESSION_EMAIL, email)
            .putString(SESSION_NAME, name)
            .putBoolean(SESSION_ACTIVE, true)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove(SESSION_EMAIL)
            .remove(SESSION_NAME)
            .putBoolean(SESSION_ACTIVE, false)
            .apply()
    }

    fun getLoggedInSession(): Pair<String, String>? {
        val active = prefs.getBoolean(SESSION_ACTIVE, false)
        if (active) {
            val email = prefs.getString(SESSION_EMAIL, null)
            val name = prefs.getString(SESSION_NAME, null)
            if (email != null && name != null) {
                return Pair(email, name)
            }
        }
        return null
    }

    fun recoverPassword(email: String): String? {
        val pass = dbHelper.getPasswordForEmail(email)
        if (pass != null) {
            notifier.sendNotification(
                title = "Recuperación de Contraseña",
                message = "La contraseña vinculada a $email es: $pass"
            )
        }
        return pass
    }

    fun getNotifications(): List<NotificationItem> {
        return dbHelper.getAllNotifications()
    }

    fun checkNearbyReports(userLat: Double, userLng: Double) {
        val notifiedSet = prefs.getStringSet(NOTIFIED_KEY, emptySet()) ?: emptySet()

        val nearbyUnnotifiedVerified = reports.filter { r ->
            val isVerified = r.st.equals("Atendido", true) || r.st.equals("Verificado", true)
            isVerified && !notifiedSet.contains(r.id) && calculateDistanceKm(userLat, userLng, r.lat, r.lng) < 0.4
        }

        if (nearbyUnnotifiedVerified.isNotEmpty()) {
            val closest = nearbyUnnotifiedVerified.minByOrNull { calculateDistanceKm(userLat, userLng, it.lat, it.lng) }
            if (closest != null) {
                val distMeters = (calculateDistanceKm(userLat, userLng, closest.lat, closest.lng) * 1000).toInt()
                notifier.sendNotification(
                    title = "¡Noticia Verificada Cerca de Ti!",
                    message = "Hay un reporte verificado ('${closest.t}') a solo ${distMeters}m de tu ubicación."
                )
                dbHelper.insertNotification(
                    title = "Noticia Verificada Cercana",
                    message = "Noticia verificada cerca de ti: '${closest.t}' (${distMeters}m).",
                    type = "ATTENDED"
                )

                val updatedSet = notifiedSet.toMutableSet()
                updatedSet.add(closest.id)
                prefs.edit().putStringSet(NOTIFIED_KEY, updatedSet).apply()
            }
        }
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    private fun migrateSharedPreferencesToDatabase() {
        val jsonStr = prefs.getString(MINE_KEY, null)
        if (!jsonStr.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val r = Report(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        t = obj.optString("t", ""),
                        c = obj.optString("c", "Otro"),
                        d = obj.optString("d", ""),
                        u = obj.optString("u", "Usuario"),
                        p = "Hace un momento",
                        s = obj.optString("s", "mine"),
                        h = obj.optInt("h", 300),
                        lat = obj.optDouble("lat", SimulatedData.FALLBACK_LAT),
                        lng = obj.optDouble("lng", SimulatedData.FALLBACK_LNG),
                        photo = if (obj.has("photo") && !obj.isNull("photo")) obj.getString("photo") else null,
                        mine = true,
                        st = obj.optString("st", "Pendiente"),
                        ts = obj.optLong("ts", System.currentTimeMillis()),
                        rs = obj.optString("rs", "Riesgo de accidente"),
                        hrs = if (obj.has("hrs")) obj.optInt("hrs") else null,
                        sup = obj.optInt("sup", 0),
                        ph = obj.optBoolean("ph", false)
                    )
                    dbHelper.insertReport(r)
                }
                prefs.edit().remove(MINE_KEY).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

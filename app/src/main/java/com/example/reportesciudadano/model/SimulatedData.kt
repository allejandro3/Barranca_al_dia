package com.example.reportesciudadano.model

import java.util.Calendar
import kotlin.math.pow

object SimulatedData {
    val FALLBACK_LAT = 7.0653
    val FALLBACK_LNG = -73.8547

    val CATEGORIES = listOf("Vías", "Alumbrado", "Aseo", "Agua", "Tránsito", "Ambiente", "Seguridad", "Otro")
    val STATUSES = listOf("Atendido", "En proceso", "Pendiente", "Rechazado")
    val REASONS = listOf("Riesgo de accidente", "Inseguridad", "Salubridad", "Afecta la movilidad", "Daño a la propiedad", "Molestia a vecinos")
    val DAYS = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val BANDS = listOf("0–3 h", "4–7 h", "8–11 h", "12–15 h", "16–19 h", "20–23 h")

    val ZONES = listOf(
        ZoneInfo("Comuna 1", 7.0700, -73.8490, 9, 0.8),
        ZoneInfo("Comuna 2", 7.0830, -73.8560, 10, 0.9),
        ZoneInfo("Comuna 3", 7.0600, -73.8480, 17, 1.0),
        ZoneInfo("Comuna 4", 7.0560, -73.8700, 16, 1.3),
        ZoneInfo("Comuna 5", 7.0760, -73.8700, 14, 1.1),
        ZoneInfo("Comuna 6", 7.0900, -73.8700, 12, 1.1),
        ZoneInfo("Comuna 7", 7.0450, -73.8650, 22, 1.5)
    )

    val INITIAL_REPORTS = mutableListOf(
        Report(
            id = "1",
            t = "Bache profundo en la vía",
            c = "Vías",
            u = "María G.",
            p = "Hace 10 min",
            s = "road",
            h = 300,
            lat = 7.0683,
            lng = -73.8527,
            sup = 14
        ),
        Report(
            id = "2",
            t = "Luminaria dañada en el parque",
            c = "Alumbrado",
            u = "Carlos R.",
            p = "Hace 35 min",
            s = "streetlight",
            h = 220,
            lat = 7.0628,
            lng = -73.8577,
            sup = 8
        ),
        Report(
            id = "3",
            t = "Basura acumulada en la esquina",
            c = "Aseo",
            u = "Laura P.",
            p = "Hace 1 h",
            s = "garbage",
            h = 360,
            lat = 7.0613,
            lng = -73.8512,
            sup = 22
        ),
        Report(
            id = "4",
            t = "Fuga de agua en la calle",
            c = "Agua",
            u = "Andrés M.",
            p = "Hace 2 h",
            s = "water",
            h = 250,
            lat = 7.0698,
            lng = -73.8567,
            sup = 19
        ),
        Report(
            id = "5",
            t = "Semáforo sin funcionar",
            c = "Tránsito",
            u = "Sofía L.",
            p = "Hace 3 h",
            s = "traffic",
            h = 320,
            lat = 7.0663,
            lng = -73.8497,
            sup = 31
        ),
        Report(
            id = "6",
            t = "Árbol caído bloquea el andén",
            c = "Ambiente",
            u = "Juan D.",
            p = "Ayer",
            s = "tree",
            h = 280,
            lat = 7.0643,
            lng = -73.8602,
            sup = 11
        )
    )

    private val CATEGORY_REASONS = mapOf(
        "Vías" to listOf(0, 3, 4),
        "Alumbrado" to listOf(1, 0),
        "Aseo" to listOf(2, 5),
        "Agua" to listOf(4, 2, 3),
        "Tránsito" to listOf(0, 3),
        "Ambiente" to listOf(2, 5, 0),
        "Seguridad" to listOf(1, 5),
        "Otro" to listOf(5, 4, 2)
    )

    private val BASE_HOURS = mapOf(
        "Vías" to 96, "Alumbrado" to 30, "Aseo" to 20, "Agua" to 14,
        "Tránsito" to 12, "Ambiente" to 60, "Seguridad" to 40, "Otro" to 50
    )

    private val TITLES = mapOf(
        "Vías" to listOf("Bache profundo en la vía", "Hundimiento en la calzada"),
        "Alumbrado" to listOf("Luminaria dañada en el parque", "Poste sin luz en la esquina"),
        "Aseo" to listOf("Basura acumulada en la esquina", "Escombros sobre el andén"),
        "Agua" to listOf("Fuga de agua en la calle", "Alcantarilla desbordada"),
        "Tránsito" to listOf("Semáforo sin funcionar", "Señal de pare caída"),
        "Ambiente" to listOf("Árbol caído bloquea el andén", "Quema de residuos en un lote"),
        "Seguridad" to listOf("Zona oscura con robos recientes", "Cámara de vigilancia dañada"),
        "Otro" to listOf("Mobiliario público vandalizado", "Ruido excessive en la noche")
    )

    fun generateSimulation(): List<SimReport> {
        var seed = 2026L
        fun nextRandom(): Double {
            seed = (seed xor (seed shl 13))
            seed = (seed xor (seed ushr 17))
            seed = (seed xor (seed shl 5))
            val positive = if (seed < 0) -seed else seed
            return (positive % 10000) / 10000.0
        }

        fun pick(weights: List<Double>): Int {
            var r = nextRandom() * weights.sum()
            for (i in weights.indices) {
                r -= weights[i]
                if (r < 0) return i
            }
            return weights.size - 1
        }

        val catWeights = listOf(24.0, 15.0, 16.0, 12.0, 11.0, 7.0, 10.0, 5.0)
        val zoneWeights = ZONES.map { it.weight.toDouble() }
        val statusWeights = listOf(40.0, 25.0, 30.0, 5.0)

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val m0 = cal.timeInMillis

        val list = mutableListOf<SimReport>()
        var count = 0
        while (count < 520) {
            val cIdx = pick(catWeights)
            val zIdx = pick(zoneWeights)
            val dayOffset = (90 * nextRandom().pow(1.25)).toInt()
            val hour = (nextRandom() * 24).toInt()

            var ts = m0 - dayOffset * 86400000L + hour * 3600000L + (nextRandom() * 60).toLong() * 60000L
            if (ts > System.currentTimeMillis()) {
                ts -= 86400000L
            }

            val category = CATEGORIES[cIdx]
            val zone = ZONES[zIdx]
            val status = STATUSES[pick(statusWeights)]
            val reasonIndices = CATEGORY_REASONS[category] ?: listOf(0)
            val reason = REASONS[reasonIndices[(nextRandom() * reasonIndices.size).toInt()]]
            val baseH = BASE_HOURS[category] ?: 40
            val hrs = if (status == "Atendido") (baseH * (0.4 + nextRandom() * 1.4) * zone.factor).toInt() else null
            val sup = (nextRandom() * nextRandom() * 70).toInt()
            val ph = nextRandom() < 0.62
            val titleList = TITLES[category] ?: listOf("Reporte ciudadano")
            val title = titleList[(nextRandom() * titleList.size).toInt()]

            list.add(
                SimReport(
                    ts = ts,
                    c = category,
                    z = zone.name,
                    st = status,
                    rs = reason,
                    hrs = hrs,
                    sup = sup,
                    ph = ph,
                    t = title
                )
            )
            count++
        }
        return list
    }

    fun getZoneNameFor(lat: Double, lng: Double): String {
        var minDistance = Double.MAX_VALUE
        var bestZone = ZONES[0].name
        for (z in ZONES) {
            val d = (z.lat - lat).pow(2) + (z.lng - lng).pow(2)
            if (d < minDistance) {
                minDistance = d
                bestZone = z.name
            }
        }
        return bestZone
    }
}

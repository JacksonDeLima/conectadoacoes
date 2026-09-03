package br.com.unisinos.conectadoacoes.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

/**
 * Utilitário de apoio logístico para planejamento e abertura de rotas de coleta
 * no Google Maps / Waze sem necessidade de chaves de API pagas.
 */
object LogisticsRouteHelper {

    /**
     * Abre o Google Maps com uma rota traçada até o endereço do doador ou destino da sede.
     */
    fun openNavigationToLocation(context: Context, locationQuery: String) {
        try {
            val encodedQuery = URLEncoder.encode(locationQuery, "UTF-8")
            val gmmIntentUri = Uri.parse("geo:0,0?q=$encodedQuery")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            // Fallback via navegador caso não haja o app do Maps instalado
            try {
                val encodedQuery = URLEncoder.encode(locationQuery, "UTF-8")
                val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedQuery")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Não foi possível abrir o aplicativo de mapas.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Traça rota de coleta com múltiplos pontos de parada (waypoints) no Google Maps.
     */
    fun openMultiStopRoute(context: Context, destinationAddress: String, stops: List<String>) {
        try {
            val encodedDest = URLEncoder.encode(destinationAddress, "UTF-8")
            val waypointsStr = if (stops.isNotEmpty()) {
                val encodedWaypoints = stops.joinToString("|") { URLEncoder.encode(it, "UTF-8") }
                "&waypoints=$encodedWaypoints"
            } else ""

            val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$encodedDest$waypointsStr&travelmode=driving")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Não foi possível traçar a rota no aplicativo de mapas.", Toast.LENGTH_SHORT).show()
        }
    }
}

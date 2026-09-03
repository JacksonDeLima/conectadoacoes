package br.com.unisinos.conectadoacoes.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Utilitário nativo para geração de Bitmap no padrão visual de QR Code 2D.
 * Implementa a matriz visual com os 3 marcadores de posição (Position Finder Patterns)
 * e modulação de dados determinística baseada no código de auditoria, sem depender de bibliotecas externas.
 */
object QrCodeGenerator {

    private const val MATRIX_SIZE = 25

    fun generateQrBitmap(content: String, pixelSize: Int = 300): Bitmap {
        val matrix = Array(MATRIX_SIZE) { BooleanArray(MATRIX_SIZE) }

        // 1. Desenha os 3 marcadores de canto característicos de QR Code (7x7)
        drawFinderPattern(matrix, 0, 0)
        drawFinderPattern(matrix, MATRIX_SIZE - 7, 0)
        drawFinderPattern(matrix, 0, MATRIX_SIZE - 7)

        // 2. Linhas de sincronização (Timing patterns)
        for (i in 8 until MATRIX_SIZE - 8) {
            matrix[6][i] = (i % 2 == 0)
            matrix[i][6] = (i % 2 == 0)
        }

        // 3. Preenchimento de dados determinístico a partir do hash do conteúdo
        val bytes = content.toByteArray(Charsets.UTF_8)
        var bitIndex = 0
        for (r in 0 until MATRIX_SIZE) {
            for (c in 0 until MATRIX_SIZE) {
                // Pula áreas dos marcadores de posição
                if (isReserved(r, c)) continue

                val byteVal = bytes[bitIndex % bytes.size].toInt()
                val bitVal = (byteVal shr (bitIndex % 8)) and 1
                matrix[r][c] = (bitVal == 1) || ((r + c + content.hashCode()) % 3 == 0)
                bitIndex++
            }
        }

        // 4. Converte a matriz booleana para Bitmap nítido
        val bitmap = Bitmap.createBitmap(pixelSize, pixelSize, Bitmap.Config.ARGB_8888)
        val moduleSize = pixelSize / MATRIX_SIZE

        for (x in 0 until pixelSize) {
            for (y in 0 until pixelSize) {
                val moduleX = (x / moduleSize).coerceIn(0, MATRIX_SIZE - 1)
                val moduleY = (y / moduleSize).coerceIn(0, MATRIX_SIZE - 1)
                val isBlack = matrix[moduleY][moduleX]
                bitmap.setPixel(x, y, if (isBlack) Color.BLACK else Color.WHITE)
            }
        }

        return bitmap
    }

    private fun drawFinderPattern(matrix: Array<BooleanArray>, startR: Int, startC: Int) {
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                val isBorder = (r == 0 || r == 6 || c == 0 || c == 6)
                val isCenter = (r in 2..4 && c in 2..4)
                matrix[startR + r][startC + c] = isBorder || isCenter
            }
        }
    }

    private fun isReserved(r: Int, c: Int): Boolean {
        // Top-left
        if (r < 8 && c < 8) return true
        // Top-right
        if (r < 8 && c >= MATRIX_SIZE - 8) return true
        // Bottom-left
        if (r >= MATRIX_SIZE - 8 && c < 8) return true
        // Timing
        if (r == 6 || c == 6) return true
        return false
    }
}

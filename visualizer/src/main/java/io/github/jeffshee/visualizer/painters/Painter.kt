package io.github.jeffshee.visualizer.painters

import android.graphics.Canvas
import io.github.jeffshee.visualizer.utils.VisualizerHelper
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import kotlin.math.cos
import kotlin.math.sin

abstract class Painter {

    private val li = LinearInterpolator()
    private val sp = AkimaSplineInterpolator()

    abstract fun draw(canvas: Canvas, helper: VisualizerHelper)

    fun interpolateFftBar(yRaw: DoubleArray, barNum: Int, interpolator: String): PolynomialSplineFunction {
        val nRaw = yRaw.size
        val xRaw = DoubleArray(nRaw) { (it * barNum).toDouble() / (nRaw - 1) }
        val psf: PolynomialSplineFunction
        psf = when (interpolator) {
            "li" -> li.interpolate(xRaw, yRaw)
            "sp" -> sp.interpolate(xRaw, yRaw)
            else -> li.interpolate(xRaw, yRaw)
        }
        return psf
    }

    fun interpolateFftWave(
        gravityModels: Array<GravityModel>,
        sliceNum: Int,
        interpolator: String
    ): PolynomialSplineFunction {
        val nRaw = gravityModels.size
        val xRaw = DoubleArray(nRaw) { (it * sliceNum).toDouble() / (nRaw - 1) }
        val yRaw = DoubleArray(nRaw)
        gravityModels.forEachIndexed { index, bar -> yRaw[index] = bar.height.toDouble() }
        val psf: PolynomialSplineFunction
        psf = when (interpolator) {
            "li" -> li.interpolate(xRaw, yRaw)
            "sp" -> sp.interpolate(xRaw, yRaw)
            else -> li.interpolate(xRaw, yRaw)
        }
        return psf
    }

    fun getEnergy(doubleArray: DoubleArray): Double {
        var energy = 0.0
        doubleArray.forEach { energy += it }
        energy /= doubleArray.size
        return energy
    }

    fun isQuiet(doubleArray: DoubleArray): Boolean {
        val threshold = 5f
        doubleArray.forEach { if (it > threshold) return false }
        return true
    }

    fun toCartesian(radius: Float, theta: Float): FloatArray {
        val x = radius * cos(theta)
        val y = radius * sin(theta)
        return floatArrayOf(x, y)
    }

    fun patchCircle(fft: DoubleArray): DoubleArray {
        val patched = DoubleArray(fft.size)
        fft.forEachIndexed { index, d ->
            if (index == fft.lastIndex) patched[index] = fft[0]
            else patched[index] = d
        }
        return patched
    }

    class GravityModel(var height: Float) {
        var dy: Float = 0f
        var ay: Float = 2f

        fun update(h: Float) {
            if (h > height) {
                height = h
                dy = 0f
            }
            height -= dy
            dy += ay
            if (height < 0) {
                height = 0f
                dy = 0f
            }
        }
    }
}
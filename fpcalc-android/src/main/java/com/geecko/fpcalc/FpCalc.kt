package com.geecko.fpcalc

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.getkeepsafe.relinker.ReLinker

/**
 * Created by Sergey Chuprin on 07.05.2019.
 *
 * This class responsible for loading fpcalc native library which is used for audio fingerprinting.
 */
class FpCalc private constructor(private val context: Context) {

    companion object {

        private const val LIBRARY_NAME = "fpcalc"

        @SuppressLint("StaticFieldLeak")
        private lateinit var instance: FpCalc

        /**
         * Class should be a singleton because [relinker]
         * instance contains loaded libraries list.
         *
         * It makes possible to not load library many times.
         */
        @Synchronized
        fun get(context: Context): FpCalc {
            if (::instance.isInitialized) {
                return instance
            }
            instance = FpCalc(context)
            return instance
        }

    }

    private val relinker = ReLinker.log { Log.d("FpCalc", it) }

    fun loadLibraryAsync(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        relinker.loadLibrary(
            context,
            LIBRARY_NAME,
            object : ReLinker.LoadListener {

                override fun success() = onSuccess()

                override fun failure(t: Throwable) = onError(t)

            }
        )
    }

    fun makeFingerprint(filepath: String): String {
        return if (loadLibrary()) {
            runCatching {
                fpCalc(arrayOf("-json", filepath))
            }.getOrElse { e ->
                Log.e("FpCalc", "error", e)
                ""
            }
        } else {
            ""
        }
    }

    private fun loadLibrary(): Boolean {
        return try {
            relinker.loadLibrary(context, LIBRARY_NAME)
            true
        } catch (e: Throwable) {
            Log.e("FpCalc", "Unable to load fpcalc library", e)
            false
        }
    }

    external fun fpCalc(args: Array<String>): String

}
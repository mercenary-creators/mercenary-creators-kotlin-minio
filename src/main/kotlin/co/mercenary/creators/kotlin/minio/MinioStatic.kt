/*
 * Copyright (c) 2019, Mercenary Creators Company. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.mercenary.creators.kotlin.minio

import co.mercenary.creators.kotlin.util.toAtomic
import com.google.common.escape.Escaper
import com.google.common.net.UrlEscapers

object MinioStatic {

    private val path: Escaper by lazy {
        UrlEscapers.urlPathSegmentEscaper()
    }

    private val list = ArrayList<Replacer>(16)

    init {
        list += Replacer(Regex("!"), "%21")
        list += Replacer(Regex("\\$"), "%24")
        list += Replacer(Regex("&"), "%26")
        list += Replacer(Regex("'"), "%27")
        list += Replacer(Regex("\\("), "%28")
        list += Replacer(Regex("\\)"), "%29")
        list += Replacer(Regex("\\*"), "%2A")
        list += Replacer(Regex("\\+"), "%2B")
        list += Replacer(Regex(","), "%2C")
        list += Replacer(Regex("/"), "%2F")
        list += Replacer(Regex(":"), "%3A")
        list += Replacer(Regex(";"), "%3B")
        list += Replacer(Regex("="), "%3D")
        list += Replacer(Regex("@"), "%40")
        list += Replacer(Regex("\\["), "%5B")
        list += Replacer(Regex("]"), "%5D")
    }

    @JvmStatic
    fun escape(data: Any): String {
        return path.escape(data.toString()).let { self ->
            var text = self
            list.forEach {
                text = it.replace(text)
            }
            text
        }
    }

    @JvmStatic
    fun hashOf(vararg args: Any?, accumulator: (Int) -> Unit) {
        args.forEach { each ->
            val hash = when (each) {
                null -> 0
                is Array<*> -> each.contentDeepHashCode()
                is IntArray -> each.contentHashCode()
                is ByteArray -> each.contentHashCode()
                is CharArray -> each.contentHashCode()
                is LongArray -> each.contentHashCode()
                is ShortArray -> each.contentHashCode()
                is FloatArray -> each.contentHashCode()
                is DoubleArray -> each.contentHashCode()
                is BooleanArray -> each.contentHashCode()
                else -> each.hashCode()
            }
            accumulator(hash)
        }
    }

    @JvmStatic
    fun hashOf(vararg args: Any?): Int {
        val hash = 1.toAtomic()
        hashOf(*args) {
            hash * 31 + it
        }
        return hash.get()
    }

    @JvmStatic
    fun same(value: Any?, other: Any?): Boolean {
        if (value === other) {
            return true
        }
        return when (value) {
            null -> other == null
            is Array<*> -> if (other is Array<*>) value contentDeepEquals other else false
            is IntArray -> if (other is IntArray) value contentEquals other else false
            is ByteArray -> if (other is ByteArray) value contentEquals other else false
            is CharArray -> if (other is CharArray) value contentEquals other else false
            is LongArray -> if (other is LongArray) value contentEquals other else false
            is ShortArray -> if (other is ShortArray) value contentEquals other else false
            is DoubleArray -> if (other is DoubleArray) value contentEquals other else false
            is BooleanArray -> if (other is BooleanArray) value contentEquals other else false
            else -> value == other
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private class Replacer(private val spec: Regex, private val with: String) {

        inline fun replace(text: String): String = spec.replace(text, with)
    }
}
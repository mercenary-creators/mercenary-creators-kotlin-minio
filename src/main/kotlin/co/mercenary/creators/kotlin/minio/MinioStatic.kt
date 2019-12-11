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

    @Suppress("NOTHING_TO_INLINE")
    private class Replacer(private val spec: Regex, private val with: String) {

        inline fun replace(text: String): String = spec.replace(text, with)
    }
}
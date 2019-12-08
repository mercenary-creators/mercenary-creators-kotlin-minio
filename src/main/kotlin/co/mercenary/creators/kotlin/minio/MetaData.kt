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

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class MetaData private constructor(private val hash: LinkedHashMap<String, String>) : MinioDataAware<MetaData>, Map<String, String> by hash {

    constructor(args: Map<String, Any>) : this(LinkedHashMap(args.size)) {
        for ((k, v) in args) {
            hash[meta(k)] = code(v)
        }
    }

    constructor(vararg args: Pair<String, Any>) : this(LinkedHashMap(args.size)) {
        for ((k, v) in args) {
            hash[meta(k)] = code(v)
        }
    }

    operator fun plus(data: MetaData) = MetaData(LinkedHashMap(hash + data.hash))

    override fun toString() = toJSONString()

    override fun equals(other: Any?) = when (other) {
        is MetaData -> other.hash == hash
        else -> false
    }

    override fun hashCode() = hash.hashCode()

    operator fun set(k: String, v: Any) {
        hash[meta(k)] = code(v)
    }

    override fun clone() = copyOf()

    override fun copyOf() = MetaData(LinkedHashMap(hash))

    companion object {

        @JvmStatic
        fun nake(args: Map<String, List<String>>?): Map<String, String> {
            if (args.isNullOrEmpty()) {
                return emptyMap()
            }
            return LinkedHashMap<String, String>(args.size).also { hash ->
                for ((k, v) in args) {
                    if (test(k)) {
                        if (!v.isNullOrEmpty()) {
                            hash[meta(k)] = code(v[0])
                        }
                    }
                }
            }
        }

        private fun code(data: Any): String {
            return data.toString()
        }

        private fun meta(meta: String): String {
            return if (test(meta)) meta.toLowerCase() else X_AMAZON_META_HEADER_START.plus(meta).toLowerCase()
        }

        private fun test(meta: String) = meta.toLowerCase().regionMatches(0, X_AMAZON_META_HEADER_START, 0, X_AMAZON_META_HEADER_START.length)
    }


}
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

import co.mercenary.creators.kotlin.json.base.JSONStatic

class MetaData : MinioDataAware<MetaData> {
    constructor(data: HeadData) {
        data?.keys?.forEach { meta ->
            if (test(meta)) {
                val list = data[meta]
                if (!list.isNullOrEmpty()) {
                    hash[meta.toLowerCase()] = list[0]
                }
            }
        }
    }

    constructor(vararg args: Pair<String, Any>) {
        for ((k, v) in args) {
            hash[meta(k)] = v.toString()
        }
    }

    private val hash = LinkedHashMap<String, String>()

    fun toHash(): Map<String, String> = hash.toMap()

    operator fun plus(data: MetaData) = hash.putAll(data.hash).let { this }

    override fun toString() = toJSONString()

    override fun toByteArray() = JSONStatic.toByteArray(hash)

    override fun toJSONString(pretty: Boolean) = JSONStatic.toJSONString(hash, pretty)

    override fun equals(other: Any?) = when(other) {
        is MetaData -> other.hash == hash
        else -> false
    }

    override fun hashCode() = hash.hashCode()

    operator fun set(k: String, v: Any) {
        hash[meta(k)] = v.toString()
    }

    companion object {

        private fun meta(meta: String): String {
            return if (test(meta)) meta.toLowerCase() else X_AMAZON_META_HEADER_START.plus(meta).toLowerCase()
        }

        private fun test(meta: String) = meta.toLowerCase().regionMatches(0, X_AMAZON_META_HEADER_START, 0, X_AMAZON_META_HEADER_START.length)
    }
}
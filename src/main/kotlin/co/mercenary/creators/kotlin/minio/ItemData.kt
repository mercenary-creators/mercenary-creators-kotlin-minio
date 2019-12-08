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

import co.mercenary.creators.kotlin.util.io.*
import com.fasterxml.jackson.annotation.*
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
class ItemData(val name: String, val bucket: String, val etag: String?, val storageClass: String?, val file: Boolean, val contentSize: Long, val lastModified: Date?, @JsonIgnore val operations: MinioOperations) : MinioDataAware<ItemData>, InputStreamSupplier {

    private val self: ByteArray by lazy {
        toByteArray()
    }

    @JsonIgnore
    override fun getInputStream() = if (file) operations.stream(name, bucket) else EmptyInputStream

    override fun toString() = toJSONString()

    override fun hashCode() = self.contentHashCode()

    override fun equals(other: Any?) = when (other) {
        is ItemData -> this === other || self contentEquals other.self
        else -> false
    }

    override fun clone() = copyOf()

    override fun copyOf() = ItemData(name, bucket, etag, storageClass, file, contentSize, lastModified, operations)
}
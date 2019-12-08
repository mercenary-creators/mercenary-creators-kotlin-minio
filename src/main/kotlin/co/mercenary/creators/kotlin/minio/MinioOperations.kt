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
import java.io.*
import java.net.URL
import java.nio.file.Path

interface MinioOperations {
    fun traced(data: OutputStream? = System.out)
    fun register(prefix: String = "minio:"): Boolean
    fun exists(bucket: String): Boolean
    fun create(bucket: String): Boolean
    fun buckets(): Sequence<BucketData>
    fun buckets(name: String): Sequence<BucketData>
    fun buckets(list: Iterable<String>): Sequence<BucketData>
    fun buckets(test: (String) -> Boolean): Sequence<BucketData>
    fun buckets(test: Regex): Sequence<BucketData> = buckets { test.matches(it) }
    fun buckets(vararg list: String): Sequence<BucketData> = buckets(list.toList())
    fun exists(name: String, bucket: String): Boolean
    fun delete(name: String, bucket: String): Boolean
    fun delete(args:  Iterable<String>, bucket: String): Boolean
    fun delete(args:  Sequence<String>, bucket: String): Boolean = delete(args.toList(), bucket)
    fun items(bucket: String, recursive: Boolean = false, prefix: String? = null): Sequence<ItemData>
    fun stat(name: String, bucket: String): StatusData
    fun stream(name: String, bucket: String): InputStream
    fun meta(name: String, bucket: String): MetaData
    fun meta(name: String, bucket: String, meta: MetaData, merge: Boolean = false): Boolean
    fun save(name: String, bucket: String, data: ContentResource, meta: MetaData? = null): Boolean
    fun save(name: String, bucket: String, data: URL, meta: MetaData? = null): Boolean = save(name, bucket, URLContentResource(data), meta)
    fun save(name: String, bucket: String, data: File, meta: MetaData? = null): Boolean = save(name, bucket, FileContentResource(data), meta)
    fun save(name: String, bucket: String, data: Path, meta: MetaData? = null): Boolean = save(name, bucket, FileContentResource(data.toFile()), meta)
    fun copy(name: String, bucket: String, dest: String = name, target: String = bucket, meta: MetaData? = null): Boolean
}
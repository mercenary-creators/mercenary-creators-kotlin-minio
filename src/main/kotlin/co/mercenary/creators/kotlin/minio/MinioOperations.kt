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
import co.mercenary.creators.kotlin.util.toContentResource
import java.io.*
import java.net.URL
import java.nio.file.Path

interface MinioOperations {
    fun nameOf(): String
    fun serverOf(): String
    fun regionOf(): String
    fun traceOff(): Boolean
    fun traceOn(data: OutputStream = System.out): Boolean
    fun loaderOn(prefix: String = MINIO_RESOURCE_PREFIX): Boolean
    fun isBucket(bucket: String): Boolean
    fun isBucket(bucket: BucketData) = isBucket(bucket.name)
    fun ensureBucket(bucket: String, lock: Boolean = false): Boolean
    fun bucketOf(bucket: String): BucketData?
    fun buckets(): Sequence<BucketData>
    fun buckets(args: Iterable<String>): Sequence<BucketData>
    fun buckets(test: (String) -> Boolean): Sequence<BucketData>
    fun buckets(test: Regex): Sequence<BucketData> = buckets { test matches it }
    fun buckets(vararg list: String): Sequence<BucketData> = buckets(list.toList())
    fun isItem(name: String, bucket: String): Boolean
    fun delete(name: String, bucket: String): Boolean
    fun delete(args:  Iterable<String>, bucket: String): Boolean
    fun delete(args:  Sequence<String>, bucket: String): Boolean = delete(args.toList(), bucket)
    fun itemsOf(bucket: String, recursive: Boolean = false, prefix: String? = null): Sequence<ItemData>
    fun statusOf(name: String, bucket: String): StatusData
    fun streamOf(name: String, bucket: String): InputStream
    fun metaDataOf(name: String, bucket: String): MetaData
    fun metaDataOf(name: String, bucket: String, meta: MetaData, merge: Boolean = false): Boolean
    fun save(name: String, bucket: String, data: ContentResource, meta: MetaData? = null): Boolean
    fun save(name: String, bucket: String, data: URL, meta: MetaData? = null): Boolean = save(name, bucket, data.toContentResource(), meta)
    fun save(name: String, bucket: String, data: File, meta: MetaData? = null): Boolean = save(name, bucket, data.toContentResource(), meta)
    fun save(name: String, bucket: String, data: Path, meta: MetaData? = null): Boolean = save(name, bucket, data.toContentResource(), meta)
    fun copyOf(name: String, bucket: String, dest: String = name, target: String = bucket, meta: MetaData? = null): Boolean
}
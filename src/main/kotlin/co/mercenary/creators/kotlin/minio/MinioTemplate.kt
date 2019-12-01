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

import co.mercenary.creators.kotlin.util.*
import co.mercenary.creators.kotlin.util.io.*
import co.mercenary.creators.kotlin.util.logging.ILogging
import io.minio.*
import io.minio.messages.Item
import java.io.*

class MinioTemplate @JvmOverloads constructor(private val server: CharSequence, private val access: CharSequence, private val secret: CharSequence, private val region: CharSequence? = X_AMAZON_REGION_USA_EAST_1) : MinioOperations {

    private val placed = false.toAtomic()

    private val logger: ILogging by lazy {
        LoggingFactory.logger(this)
    }

    private val client: MinioClient by lazy {
        MinioClient(server.toString(), access.toString(), secret.toString(), region.orElse { X_AMAZON_REGION_USA_EAST_1 }.toString())
    }

    override fun create(bucket: String): Boolean {
        if (exists(bucket)) {
            return true
        }
        try {
            client.makeBucket(bucket)
            return true
        }
        catch (cause: Throwable) {
            Throwables.thrown(cause)
            logger.error(cause) {
                "bucket = $bucket"
            }
        }
        return false
    }

    override fun save(name: String, bucket: String, data: ContentResource, meta: MetaData?): Boolean {
        try {
            if (data.isContentThere().and(create(bucket))) {
                val type = data.getContentType()
                client.putObject(bucket, name, data.toInputStream(), data.getContentSize(), null, null, type)
                val hash = meta?.toHash()
                if (hash.isNullOrEmpty()) {
                    return true
                }
                else {
                    client.copyObject(bucket, name, mutableMapOf("Content-Type" to type) + hash, null, bucket, name, null, MinioCopyConditions)
                }
                return true
            }
        }
        catch (cause: Throwable) {
            Throwables.thrown(cause)
            logger.error(cause) {
                "bucket = $bucket"
            }
        }
        return false
    }

    override fun exists(bucket: String): Boolean {
        return try {
            client.bucketExists(bucket)
        }
        catch (cause: Throwable) {
            Throwables.thrown(cause)
            logger.error(cause) {
                "bucket = $bucket"
            }
            false
        }
    }

    override fun delete(name: String, bucket: String): Boolean {
        try {
            if (exists(name, bucket)) {
                client.removeObject(bucket, name)
                return true
            }
        }
        catch (cause: Throwable) {
            Throwables.thrown(cause)
            logger.error(cause) {
                "bucket = $bucket"
            }
        }
        return false
    }

    override fun delete(args: Iterable<String>, bucket: String): Boolean {
        return args.distinct().let { list ->
            try {
                val good = mutableSetOf<String>()
                client.removeObjects(bucket, list).forEach {
                    try {
                        good += it.get().name
                    }
                    catch (cause: Throwable) {
                        Throwables.thrown(cause)
                    }
                }
                good.containsAll(list)
            }
            catch (cause: Throwable) {
                Throwables.thrown(cause)
                logger.error(cause) {
                    "bucket = $bucket"
                }
            }
            false
        }
    }

    override fun traced(data: OutputStream?) {
        if (data != null) {
            client.traceOn(data)
        }
        else {
            client.traceOff()
        }
    }

    override fun loader(name: String): Boolean {
        if (placed.compareAndSet(false, true)) {
            val head = name.trim().replace(IO.PREFIX_COLON, EMPTY_STRING).trim().plus(IO.PREFIX_COLON)
            contentResourceLoader += object : ContentProtocolResolver {
                override fun resolve(path: String, load: ContentResourceLoader): ContentResource? {
                    if (path.startsWith(head)) {
                        val norm = getPathNormalizedNoTail(path.removePrefix(head), true).orElse { EMPTY_STRING }
                        val look = norm.indexOf(IO.SINGLE_SLASH)
                        if (look != IS_NOT_FOUND) {
                            val base = norm.substring(0, look)
                            val file = norm.substring(look.inc())
                            if (exists(file, base)) {
                                 try {
                                    return load[client.presignedGetObject(base, file)].toContentCache()
                                }
                                catch (cause: Throwable) {
                                    Throwables.thrown(cause)
                                    logger.error(cause) {
                                        "path = $path"
                                    }
                                }
                            }
                        }
                    }
                    return null
                }
            }
            return true
        }
        return false
    }

    override fun exists(name: String, bucket: String): Boolean {
        return try {
            client.statObject(bucket, name).createdTime() != null
        }
        catch (cause: Throwable) {
            Throwables.thrown(cause)
            logger.error(cause) {
                "bucket = $bucket"
            }
            false
        }
    }

    override fun stream(name: String, bucket: String): InputStream = client.getObject(bucket, name)

    override fun buckets() = client.listBuckets().toSequence().map { BucketData(it.name(), it.creationDate()) }

    override fun buckets(name: String) = client.listBuckets().toSequence().filter { name == it.name() }.map { BucketData(name, it.creationDate()) }

    override fun buckets(test: (String) -> Boolean) = client.listBuckets().toSequence().filter { test(it.name()) }.map { BucketData(it.name(), it.creationDate()) }

    override fun buckets(list: Iterable<String>): Sequence<BucketData> {
        val look = list.distinct()
        return when (look.size) {
            0 -> emptySequence()
            1 -> look[0].let { name -> client.listBuckets().toSequence().filter { name == it.name() }.map { BucketData(name, it.creationDate()) } }
            else -> client.listBuckets().toSequence().filter { it.name() in look }.map { BucketData(it.name(), it.creationDate()) }
        }
    }

    override fun items(bucket: String, prefix: String?, recursive: Boolean) = client.listObjects(bucket, prefix, recursive).toSequence().map { data ->
        data.get().let { ItemData(it.objectName(), bucket, it.etag, it.storageClass(), it.file, it.objectSize(), if (it.file) it.lastModified() else null, this) }
    }

    override fun head(name: String, bucket: String): HeadData {
        return client.statObject(bucket, name).httpHeaders()
    }

    override fun stat(name: String, bucket: String): StatusData {
        return try {
            client.statObject(bucket, name).let { StatusData(name, bucket, it.etag, true, it.length(), it.createdTime(), MetaData(it.httpHeaders()).toHash(), it.contentType()) }
        }
        catch (cause: Throwable) {
            Throwables.thrown(cause)
            StatusData(name, bucket, null, false, 0, null, emptyMap(), DEFAULT_CONTENT_TYPE)
        }
    }

    override fun meta(name: String, bucket: String) = MetaData(client.statObject(bucket, name).httpHeaders())

    override fun meta(name: String, bucket: String, meta: MetaData, merge: Boolean): Boolean {
        if (exists(name, bucket)) {
            try {
                val maps = mutableMapOf("Content-Type" to stat(name, bucket).contentType)
                if (merge) {
                    client.copyObject(bucket, name, maps + meta(name, bucket).plus(meta).toHash(), null, bucket, name, null, MinioCopyConditions)
                }
                else {
                    client.copyObject(bucket, name, maps + meta.toHash(), null, bucket, name, null, MinioCopyConditions)
                }
                return true
            }
            catch (cause: Throwable) {
                Throwables.thrown(cause)
                logger.error(cause) {
                    "bucket = $bucket"
                }
            }
        }
        return false
    }

    override fun copy(name: String, bucket: String, dest: String, target: String, meta: MetaData?): Boolean {
        return when (exists(name, bucket)) {
            true -> true.also {
                val hash = meta?.toHash()
                when (hash.isNullOrEmpty()) {
                    true -> client.copyObject(target, dest, null, null, bucket, name, null, null)
                    else -> client.copyObject(target, dest, hash, null, bucket, name, null, MinioCopyConditions)
                }
            }
            else -> false
        }
    }

    private object MinioCopyConditions : CopyConditions() {
        init {
            setReplaceMetadataDirective()
        }
    }

    companion object {
        private val Item.file: Boolean
            get() = isDir.not()

        private val Item.etag: String?
            get() = etag()?.replace("\"", EMPTY_STRING)

        private val ObjectStat.etag: String?
            get() = etag()?.replace("\"", EMPTY_STRING)
    }
}
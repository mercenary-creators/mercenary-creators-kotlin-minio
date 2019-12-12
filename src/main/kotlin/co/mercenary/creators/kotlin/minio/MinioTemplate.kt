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
import io.minio.*
import java.io.*

class MinioTemplate @JvmOverloads constructor(private val server: String, private val access: String, private val secret: String, private val region: String? = X_AMAZON_REGION_USA_EAST_1, private val name: String? = null) : MinioOperations, Logging() {

    private val placed = false.toAtomic()

    private val client: MinioClient by lazy {
        MinioClient(serverOf(), access, secret, regionOf())
    }

    private val uuid: String by lazy {
        uuid()
    }

    override fun nameOf() = name.orElse { uuid }

    override fun serverOf() = server.trim()

    override fun regionOf() = toTrimOrElse(region, X_AMAZON_REGION_USA_EAST_1)

    override fun ensureBucket(bucket: String, lock: Boolean): Boolean {
        if (isBucket(bucket)) {
            return true
        }
        try {
            client.makeBucket(bucket, null, lock)
            return true
        }
        catch (cause: Throwable) {
            Throwables.thrown(cause)
        }
        return false
    }

    override fun save(name: String, bucket: String, data: ContentResource, meta: MetaData?): Boolean {
        try {
            if (data.isContentThere().and(ensureBucket(bucket))) {
                val type = data.getContentType()
                client.putObject(bucket, name, data.toInputStream(), data.getContentSize(), null, null, type)
                if (!meta.isNullOrEmpty()) {
                    client.copyObject(bucket, name, mutableMapOf("Content-Type" to type) + meta, null, bucket, name, null, MinioCopyConditions)
                }
                return true
            }
        }
        catch (cause: Throwable) {
            Throwables.thrown(cause)
        }
        return false
    }

    override fun isBucket(bucket: String) = try {
        client.bucketExists(bucket)
    }
    catch (cause: Throwable) {
        Throwables.thrown(cause)
        false
    }


    override fun delete(name: String, bucket: String) = try {
        client.removeObject(bucket, name)
        true
    }
    catch (cause: Throwable) {
        Throwables.thrown(cause)
        false
    }

    override fun delete(args: Iterable<String>, bucket: String): Boolean {
        return args.uniqueOf().let { list ->
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
            }
            false
        }
    }

    override fun traceOff() = try {
        client.traceOff()
        true
    }
    catch (cause: Throwable) {
        Throwables.thrown(cause)
        false
    }

    override fun traceOn(data: OutputStream) = try {
        client.traceOn(data)
        true
    }
    catch (cause: Throwable) {
        Throwables.thrown(cause)
        false
    }

    override fun loaderOn(prefix: String): Boolean {
        return if (placed.compareAndSet(false, true)) {
            val head = prefix.trim().replace(IO.PREFIX_COLON, EMPTY_STRING).trim().plus(IO.PREFIX_COLON)
            contentResourceLoader += object : ContentProtocolResolver {
                override fun resolve(path: String, load: ContentResourceLoader): ContentResource? {
                    if (path.startsWith(head)) {
                        val norm = getPathNormalizedNoTail(path.removePrefix(head), true).orElse { EMPTY_STRING }
                        val look = norm.indexOf(IO.SINGLE_SLASH)
                        if (look != IS_NOT_FOUND) {
                            val base = norm.substring(0, look)
                            val file = norm.substring(look + 1)
                            val stat = statusOf(file, base)
                            if (stat.file) {
                                return try {
                                    MinioContentResource(streamOf(file, base).toByteArray(), path, stat.contentType.orElse { resolveContentType(file) }, stat.creationTime.copyOf().toLong())
                                }
                                catch (cause: Throwable) {
                                    Throwables.thrown(cause)
                                    null
                                }
                            }
                        }
                    }
                    return null
                }
            }
            true
        }
        else false
    }

    override fun isItem(name: String, bucket: String): Boolean {
        return try {
            client.statObject(bucket, name).createdTime() != null
        }
        catch (cause: Throwable) {
            Throwables.thrown(cause)
            false
        }
    }

    override fun streamOf(name: String, bucket: String): InputStream {
        return try {
            client.getObject(bucket, name)
        }
        catch (cause: Throwable) {
            Throwables.thrown(cause)
            EmptyInputStream
        }
    }

    override fun bucketOf(bucket: String): BucketData? {
        return try {
            client.listBuckets().firstOrNull { it.name() == bucket }.let {
                if (it == null) null else  BucketData(it.name(), it.creationDate())
            }
        }
        catch (cause: Throwable) {
            Throwables.thrown(cause)
            null
        }
    }

    override fun buckets() = client.listBuckets().asSequence().map { BucketData(it.name(), it.creationDate()) }

    override fun buckets(test: (String) -> Boolean) = client.listBuckets().asSequence().filter { test(it.name()) }.map { BucketData(it.name(), it.creationDate()) }

    override fun buckets(args: Iterable<String>): Sequence<BucketData> {
        val look = args.uniqueOf()
        return when (look.size) {
            0 -> emptySequence()
            1 -> look[0].let { name -> client.listBuckets().asSequence().filter { name == it.name() }.map { BucketData(name, it.creationDate()) } }
            else -> client.listBuckets().asSequence().filter { it.name() in look }.map { BucketData(it.name(), it.creationDate()) }
        }
    }

    override fun itemsOf(bucket: String, recursive: Boolean, prefix: String?) = client.listObjects(bucket, prefix, recursive).asSequence().map { data ->
        val item = data.get()
        val file = item.isDir.not()
        ItemData(item.objectName(), bucket, item.etag()?.replace("\"", EMPTY_STRING), item.storageClass(), file, if (file) item.objectSize() else null, if (file) item.lastModified() else null, this)
    }

    override fun statusOf(name: String, bucket: String): StatusData {
        return try {
            client.statObject(bucket, name).let { StatusData(name, bucket, it.etag()?.replace("\"", EMPTY_STRING), true, it.length(), it.createdTime(), MetaData.create(it.httpHeaders()), it.contentType()) }
        }
        catch (cause: Throwable) {
            Throwables.thrown(cause)
            StatusData(name, bucket)
        }
    }

    override fun metaDataOf(name: String, bucket: String) = try {
        MetaData(MetaData.create(client.statObject(bucket, name).httpHeaders()))
    }
    catch (cause: Throwable) {
        Throwables.thrown(cause)
        throw MinioFatalExceptiion(cause)
    }

    override fun metaDataOf(name: String, bucket: String, meta: MetaData, merge: Boolean): Boolean {
        if (isItem(name, bucket)) {
            try {
                val maps = mutableMapOf("Content-Type" to statusOf(name, bucket).contentType)
                if (merge) {
                    client.copyObject(bucket, name, maps + metaDataOf(name, bucket) + meta, null, bucket, name, null, MinioCopyConditions)
                }
                else {
                    client.copyObject(bucket, name, maps + meta, null, bucket, name, null, MinioCopyConditions)
                }
                return true
            }
            catch (cause: Throwable) {
                Throwables.thrown(cause)
            }
        }
        return false
    }

    override fun copyOf(name: String, bucket: String, dest: String, target: String, meta: MetaData?): Boolean {
        return when (isItem(name, bucket)) {
            true -> true.also {
                when (meta.isNullOrEmpty()) {
                    true -> client.copyObject(target, dest, null, null, bucket, name, null, null)
                    else -> client.copyObject(target, dest, meta, null, bucket, name, null, MinioCopyConditions)
                }
            }
            else -> false
        }
    }

    override fun hashCode() = uuid.hashCode()

    override fun equals(other: Any?) = when (other) {
        is MinioTemplate -> other === this || uuid == other.uuid
        else -> false
    }

    override fun toString(): String {
        return "${javaClass.name}(${nameOf()}, ${serverOf()}, ${regionOf()})"
    }

    private object MinioCopyConditions : CopyConditions() {
        init {
            setReplaceMetadataDirective()
        }
    }
}
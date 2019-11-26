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
import com.fasterxml.jackson.annotation.*
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class StatusData(val name: String, val bucket: String, val etag: String?, val file: Boolean, val contentSize: Long, val creationTime: Date?, @JsonInclude(JsonInclude.Include.NON_EMPTY) val metaData: Map<String, String>, private val type: String) : MinioDataAware<StatusData> {

    val contentType: String
        get() = if (file) if (isDefaultContentType(type)) getDefaultContentTypeProbe().getContentType(name, type) else type.toLowerTrim() else DEFAULT_CONTENT_TYPE

    override fun toString() = toJSONString()
}
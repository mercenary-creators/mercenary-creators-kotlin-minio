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

@file:kotlin.jvm.JvmName("TestsKt")

package co.mercenary.creators.kotlin.minio

const val MAIN_TEST_PROPERTIES = "file:/opt/development/properties/mercenary-creators-minio/minio-test.properties"

const val AWS3_TEST_PROPERTIES = "file:/opt/development/properties/mercenary-creators-minio/minio-aws3.properties"

typealias TodoData = co.mercenary.creators.kotlin.json.util.typicode.TypicodeTodoData

typealias KotlinTest =  co.mercenary.creators.kotlin.minio.test.util.AbstractKotlinMinioTest



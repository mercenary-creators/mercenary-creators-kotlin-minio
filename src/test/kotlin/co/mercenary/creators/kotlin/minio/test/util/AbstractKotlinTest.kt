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

package co.mercenary.creators.kotlin.minio.test.util

import co.mercenary.creators.kotlin.util.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.function.Executable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

abstract class AbstractKotlinTest : Logging() {

    private val conf: Properties by lazy {
        getConfigPropertiesBuilder().invoke()
    }

    protected open fun getConfigPropertiesBuilder(): () -> Properties = { Properties() }

    @JvmOverloads
    fun getConfigProperty(name: String, other: String = EMPTY_STRING): String = conf.getProperty(name, other)

    fun assertEach(vararg list: Executable) {
        if (list.isNotEmpty()) {
            Assertions.assertAll(*list)
        }
    }

    fun assertEach(list: List<Executable>) {
        if (list.isNotEmpty()) {
            Assertions.assertAll(list)
        }
    }

    fun assumeThat(condition: Boolean, executable: Executable) {
        Assumptions.assumingThat(condition, executable)
    }

    fun assertTrue(condition: Boolean, block: () -> Any?) {
        Assertions.assertTrue(condition, toSafeString(block))
    }

    fun assertFalse(condition: Boolean, block: () -> Any?) {
        Assertions.assertFalse(condition, toSafeString(block))
    }

    fun assertEquals(expected: Any?, actual: Any?, block: () -> Any?) {
        Assertions.assertEquals(expected, actual, toSafeString(block))
    }

    fun assertEquals(expected: Any?, actual: Any?) {
        Assertions.assertEquals(expected, actual)
    }

    fun assertNotEquals(expected: Any?, actual: Any?) {
        Assertions.assertNotEquals(expected, actual)
    }

    fun assertEquals(expected: ByteArray?, actual: ByteArray?, block: () -> Any?) {
        Assertions.assertArrayEquals(expected, actual, toSafeString(block))
    }

    fun assertNotEquals(expected: Any?, actual: Any?, block: () -> Any?) {
        Assertions.assertNotEquals(expected, actual, toSafeString(block))
    }

    inline fun <reified T : Throwable, R> assumeThat(block: () -> R): R? {
        return try {
            block()
        }
        catch (cause: Throwable) {
            val good = when (cause) {
                is T -> true
                else -> false
            }
            if (good.not()) {
                throw AssertionError(null, cause)
            }
            null
        }
    }

    infix fun <T : Any?> Iterable<T>?.shouldBe(value: Iterable<*>?) = assertEquals(value?.toList(), this?.toList())

    infix fun <T : Any> T?.shouldBe(value: Any?) = assertEquals(value, this)

    infix fun AtomicInteger.shouldBe(value: Int) = assertEquals(value, this.get())

    infix fun Int.shouldBe(value: AtomicInteger) = assertEquals(value.get(), this)

    infix fun AtomicInteger.shouldBe(value: AtomicInteger) = assertEquals(value.get(), this.get())

    infix fun AtomicInteger.shouldNotBe(value: Int) = assertNotEquals(value, this.get())

    infix fun Int.shouldNotBe(value: AtomicInteger) = assertNotEquals(value.get(), this)

    infix fun AtomicInteger.shouldNotBe(value: AtomicInteger) = assertNotEquals(value.get(), this.get())

    fun <T : Any?> List<T>.shouldBe(value: Iterable<*>?, block: () -> Any?) = assertEquals(value?.toList(), this, block)

    fun <T : Any> T?.shouldBe(value: Any?, block: () -> Any?) = assertEquals(value, this, block)

    fun <T : Any> (() -> T?).shouldBe(value: Any?, block: () -> Any?) = assertEquals(value, this.invoke(), block)

    fun <T : Any> (() -> T?).shouldBe(value: () -> Any?, block: () -> Any?) = assertEquals(value.invoke(), this.invoke(), block)

    fun ByteArray?.shouldBe(value: ByteArray?, block: () -> Any?) = assertEquals(value, this, block)

    fun <T : Any?> List<T>.shouldNotBe(value: Iterable<*>?, block: () -> Any?) = assertNotEquals(value?.toList(), this, block)

    fun <T : Any> T?.shouldNotBe(value: Any?, block: () -> Any?) = assertNotEquals(value, this, block)

    fun <T : Any> (() -> T?).shouldNotBe(value: Any?, block: () -> Any?) = assertNotEquals(value, this.invoke(), block)

    fun <T : Any> (() -> T?).shouldNotBe(value: () -> Any?, block: () -> Any?) = assertNotEquals(value.invoke(), this.invoke(), block)
}
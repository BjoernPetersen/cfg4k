/*
 * Copyright 2015-2016 Javier Díaz-Cano Martín-Albo (javierdiazcanom@gmail.com)
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

package com.jdiazcano.cfg4k

import com.jdiazcano.cfg4k.loaders.PropertyConfigLoader
import com.jdiazcano.cfg4k.providers.CachedConfigProvider
import com.jdiazcano.cfg4k.providers.ProxyConfigProvider
import com.jdiazcano.cfg4k.providers.bind
import com.jdiazcano.cfg4k.providers.get
import com.jdiazcano.cfg4k.providers.getOrNull
import com.jdiazcano.cfg4k.sources.ClasspathConfigSource
import com.winterbe.expekt.should
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class CachedConfigProviderTest : Spek({

    val loaders = listOf(
            PropertyConfigLoader(ClasspathConfigSource("/test.properties"))
    )
    loaders.forEach { loader ->
        describe("a property config loader [${loader.javaClass.simpleName}]") {
            val providers = listOf(
                    ProxyConfigProvider(loader),
                    CachedConfigProvider(ProxyConfigProvider(loader))
            )
            providers.forEach { provider ->
                it("primitive properties [${provider.javaClass.simpleName}]") {
                    provider.get<Int>("integerProperty").should.be.equal(1)
                    provider.get<Integer>("integerProperty").should.be.equal(Integer(1))
                    provider.get<Long>("longProperty").should.be.equal(2)
                    provider.get<Short>("shortProperty").should.be.equal(1)
                    provider.get<Float>("floatProperty").should.be.equal(2.1F)
                    provider.get<Double>("doubleProperty").should.be.equal(1.1)
                    provider.get<Byte>("byteProperty").should.be.equal(2)
                    provider.get<Boolean>("booleanProperty").should.be.`true`
                }

                it("primitive nullable properties [${provider.javaClass.simpleName}]") {
                    provider.getOrNull<Int?>("integerProperty").should.be.equal(1)
                    provider.getOrNull<Integer?>("integerProperty").should.be.equal(Integer(1))
                    provider.getOrNull<Long?>("longProperty").should.be.equal(2)
                    provider.getOrNull<Short?>("shortProperty").should.be.equal(1)
                    provider.getOrNull<Float?>("floatProperty").should.be.equal(2.1F)
                    provider.getOrNull<Double?>("doubleProperty").should.be.equal(1.1)
                    provider.getOrNull<Byte?>("byteProperty").should.be.equal(2)
                    provider.getOrNull<Boolean?>("booleanProperty").should.be.`true`
                    provider.getOrNull<Boolean?>("randomNonExistent").should.be.`null`
                }

                it("binding test [${provider.javaClass.simpleName}]") {
                    val testBinder = provider.bind<TestBinder>("")
                    testBinder.booleanProperty().should.be.`true`
                    testBinder.integerProperty().should.be.equal(1)
                    testBinder.longProperty().should.be.equal(2)
                    testBinder.shortProperty().should.be.equal(1)
                    testBinder.floatProperty().should.be.equal(2.1F)
                    testBinder.doubleProperty().should.be.equal(1.1)
                    testBinder.byteProperty().should.be.equal(2)
                    testBinder.a().should.be.equal("b")
                    testBinder.c().should.be.equal("d")

                    val secondTestBinder = provider.bind<TestBinder>("")
                    when (provider) {
                        // When we have a cached object it must be the same object, but when it's not cached values must
                        is CachedConfigProvider -> testBinder.should.be.identity(secondTestBinder)
                        else -> {
                            with (testBinder) {
                                booleanProperty().should.be.equal(secondTestBinder.booleanProperty())
                                integerProperty().should.be.equal(secondTestBinder.integerProperty())
                                longProperty().should.be.equal(secondTestBinder.longProperty())
                                shortProperty().should.be.equal(secondTestBinder.shortProperty())
                                floatProperty().should.be.equal(secondTestBinder.floatProperty())
                                doubleProperty().should.be.equal(secondTestBinder.doubleProperty())
                                byteProperty().should.be.equal(secondTestBinder.byteProperty())
                                a().should.be.equal(secondTestBinder.a())
                                c().should.be.equal(secondTestBinder.c())
                            }
                        }
                    }
                }
            }
        }
    }
})

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

import com.jdiazcano.cfg4k.core.toConfig
import com.jdiazcano.cfg4k.loaders.PropertyConfigLoader
import com.jdiazcano.cfg4k.parsers.CalendarParser
import com.jdiazcano.cfg4k.parsers.DateParser
import com.jdiazcano.cfg4k.parsers.LocalDateParser
import com.jdiazcano.cfg4k.parsers.LocalDateTimeParser
import com.jdiazcano.cfg4k.parsers.OffsetDateTimeParser
import com.jdiazcano.cfg4k.parsers.OffsetTimeParser
import com.jdiazcano.cfg4k.parsers.Parsers
import com.jdiazcano.cfg4k.parsers.ZonedDateTimeParser
import com.jdiazcano.cfg4k.providers.DefaultConfigProvider
import com.jdiazcano.cfg4k.providers.Providers.proxy
import com.jdiazcano.cfg4k.providers.bind
import com.jdiazcano.cfg4k.providers.cache
import com.jdiazcano.cfg4k.providers.get
import com.jdiazcano.cfg4k.sources.ConfigSource
import com.jdiazcano.cfg4k.sources.StringConfigSource
import com.jdiazcano.cfg4k.sources.URLConfigSource
import com.jdiazcano.cfg4k.utils.SettingNotFound
import com.winterbe.expekt.should
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URI
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.fail

class ConfigProviderTest : Spek({

    val source = URLConfigSource(javaClass.classLoader.getResource("test.properties"))
    val providers = listOf(
            proxy(PropertyConfigLoader(source)),
            proxy(PropertyConfigLoader(source)).cache()
    )

    providers.forEachIndexed { i, provider ->
        describe("provider[$i]") {
            it("default values") {
                provider.get("this.does.not.exist", 1).should.be.equal(1)
                // When having a cached provider then it will cache the "this.does.not.exist" if it has a default value
                // because the delegated provider will return the default value. Should the default value not be passed
                // and the exception caught? I think that would mean a performance impact and having exceptions into
                // account for normal logic is not right
                assertFailsWith<SettingNotFound> {
                    provider.get<Int>("i.dont.extist")
                }
            }

            it("integer properties") {
                provider.get<Int>("integerProperty").should.be.equal(1)
            }

            it("long properties") {
                provider.get<Long>("longProperty").should.be.equal(2)
            }

            it("short properties") {
                provider.get<Short>("shortProperty").should.be.equal(1)
            }

            it("float properties") {
                provider.get<Float>("floatProperty").should.be.equal(2.1F)
            }

            it("double properties") {
                provider.get<Double>("doubleProperty").should.be.equal(1.1)
            }

            it("byte properties") {
                provider.get<Byte>("byteProperty").should.be.equal(2)
            }

            it("boolean properties") {
                provider.get<Boolean>("booleanProperty").should.be.`true`
            }

            it("big integer properties") {
                provider.get<BigInteger>("bigIntegerProperty").should.be.equal(BigInteger("1"))
            }

            it("big decimal properties") {
                provider.get<BigDecimal>("bigDecimalProperty").should.be.equal(BigDecimal("1.1"))
            }

            it("file properties") {
                provider.get<File>("file").should.be.equal(File("myfile.txt"))
            }

            it("path properties") {
                provider.get<Path>("path").should.be.equal(Paths.get("mypath.txt"))
            }

            it("url properties") {
                provider.get<URL>("url").should.be.equal(URL("https://www.amazon.com"))
            }

            it("uri properties") {
                provider.get<URI>("uri").should.be.equal(URI("https://www.amazon.com"))
            }

            it("date property") {
                Parsers.addParser(Date::class.java, DateParser("dd-MM-yyyy"))
                val date = provider.get<Date>("dateProperty")

                // A calendar must be built on top of that date to work with it
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.get(Calendar.DAY_OF_YEAR).should.be.equal(1)
                calendar.get(Calendar.MONTH).should.be.equal(0)
                calendar.get(Calendar.YEAR).should.be.equal(2017)
            }

            it("localdateproperty property") {
                Parsers.addParser(LocalDate::class.java, LocalDateParser("dd-MM-yyyy"))
                val localDate = provider.get<LocalDate>("localDateProperty")
                localDate.dayOfYear.should.be.equal(1)
                localDate.month.should.be.equal(Month.JANUARY)
                localDate.year.should.be.equal(2017)
            }

            it("isolocaldateproperty property") {
                Parsers.addParser(LocalDate::class.java, LocalDateParser(DateTimeFormatter.ISO_LOCAL_DATE))
                val localDate = provider.get<LocalDate>("isoLocalDateProperty")
                localDate.dayOfYear.should.be.equal(1)
                localDate.month.should.be.equal(Month.JANUARY)
                localDate.year.should.be.equal(2017)
            }

            it("localdatetime property") {
                Parsers.addParser(LocalDateTime::class.java, LocalDateTimeParser("dd-MM-yyyy HH:mm:ss"))
                val localDateTime = provider.get<LocalDateTime>("localDateTimeProperty")
                localDateTime.dayOfYear.should.be.equal(1)
                localDateTime.month.should.be.equal(Month.JANUARY)
                localDateTime.year.should.be.equal(2017)
                localDateTime.hour.should.be.equal(18)
                localDateTime.minute.should.be.equal(1)
                localDateTime.second.should.be.equal(31)
            }

            it("isolocaldatetime property") {
                Parsers.addParser(LocalDateTime::class.java, LocalDateTimeParser(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                val localDateTime = provider.get<LocalDateTime>("isoLocalDateTimeProperty")
                localDateTime.dayOfYear.should.be.equal(1)
                localDateTime.month.should.be.equal(Month.JANUARY)
                localDateTime.year.should.be.equal(2017)
                localDateTime.hour.should.be.equal(18)
                localDateTime.minute.should.be.equal(1)
                localDateTime.second.should.be.equal(31)
            }

            it("zoneddatetime property") {
                Parsers.addParser(ZonedDateTime::class.java, ZonedDateTimeParser("dd-MM-yyyy HH:mm:ss"))
                val zonedDateTime = provider.get<ZonedDateTime>("zonedDateTimeProperty")
                zonedDateTime.dayOfYear.should.be.equal(1)
                zonedDateTime.month.should.be.equal(Month.JANUARY)
                zonedDateTime.year.should.be.equal(2017)
                zonedDateTime.hour.should.be.equal(18)
                zonedDateTime.minute.should.be.equal(1)
                zonedDateTime.second.should.be.equal(31)
            }

            it("isozoneddatetime property") {
                Parsers.addParser(ZonedDateTime::class.java, ZonedDateTimeParser(DateTimeFormatter.ISO_ZONED_DATE_TIME))
                val zonedDateTime = provider.get<ZonedDateTime>("isoZonedDateTimeProperty")
                zonedDateTime.dayOfYear.should.be.equal(1)
                zonedDateTime.month.should.be.equal(Month.JANUARY)
                zonedDateTime.year.should.be.equal(2017)
                zonedDateTime.hour.should.be.equal(18)
                zonedDateTime.minute.should.be.equal(1)
                zonedDateTime.second.should.be.equal(31)
            }

            it("offsetdatetime property") {
                Parsers.addParser(OffsetDateTime::class.java, OffsetDateTimeParser("dd-MM-yyyy HH:mm:ssXXX"))
                val offsetDateTime = provider.get<OffsetDateTime>("offsetDateTimeProperty")
                offsetDateTime.dayOfYear.should.be.equal(1)
                offsetDateTime.month.should.be.equal(Month.JANUARY)
                offsetDateTime.year.should.be.equal(2017)
                offsetDateTime.hour.should.be.equal(18)
                offsetDateTime.minute.should.be.equal(1)
                offsetDateTime.second.should.be.equal(31)
            }

            it("isooffsetdatetime property") {
                Parsers.addParser(OffsetDateTime::class.java, OffsetDateTimeParser(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                val offsetDateTime = provider.get<OffsetDateTime>("isoOffsetDateTimeProperty")
                offsetDateTime.dayOfYear.should.be.equal(1)
                offsetDateTime.month.should.be.equal(Month.JANUARY)
                offsetDateTime.year.should.be.equal(2017)
                offsetDateTime.hour.should.be.equal(18)
                offsetDateTime.minute.should.be.equal(1)
                offsetDateTime.second.should.be.equal(31)
            }

            it("offsettime property") {
                Parsers.addParser(OffsetTime::class.java, OffsetTimeParser("HH:mm:ssXXX"))
                val offsetTime = provider.get<OffsetTime>("offsetTimeProperty")
                offsetTime.hour.should.be.equal(18)
                offsetTime.minute.should.be.equal(1)
                offsetTime.second.should.be.equal(31)
            }

            it("isooffsettime property") {
                Parsers.addParser(OffsetTime::class.java, OffsetTimeParser(DateTimeFormatter.ISO_OFFSET_TIME))
                val offsetTime = provider.get<OffsetTime>("isoOffsetTimeProperty")
                offsetTime.hour.should.be.equal(18)
                offsetTime.minute.should.be.equal(1)
                offsetTime.second.should.be.equal(31)
            }

            it("calendar property") {
                Parsers.addParser(Calendar::class.java, CalendarParser("dd-MM-yyyy"))
                val calendar = provider.get<Calendar>("calendarProperty")
                calendar.get(Calendar.DAY_OF_YEAR).should.be.equal(1)
                calendar.get(Calendar.MONTH).should.be.equal(0)
                calendar.get(Calendar.YEAR).should.be.equal(2017)
            }

            it("binding test") {
                val testBinder = provider.bind<TestBinder>("")
                testBinder.integerWithDefault().should.be.equal(123456)
                testBinder.booleanProperty().should.be.`true`
                testBinder.integerProperty().should.be.equal(1)
                testBinder.longProperty().should.be.equal(2)
                testBinder.shortProperty().should.be.equal(1)
                testBinder.floatProperty().should.be.equal(2.1F)
                testBinder.doubleProperty().should.be.equal(1.1)
                testBinder.byteProperty().should.be.equal(2)
                testBinder.a().should.be.equal("b")
                testBinder.c().should.be.equal("d")
                testBinder.bigDecimalProperty().should.be.equal(BigDecimal("1.1"))
                testBinder.bigIntegerProperty().should.be.equal(BigInteger("1"))
                testBinder.uri().should.be.equal(URI("https://www.amazon.com"))
                testBinder.url().should.be.equal(URL("https://www.amazon.com"))
                testBinder.file().should.be.equal(File("myfile.txt"))
                testBinder.path().should.be.equal(Paths.get("mypath.txt"))
                testBinder.nested().normal().should.be.equal(1)
                testBinder.nested().supernested().normal().should.be.equal(2)
            }

            it("property binding test") {
                provider.reload()
                val testBinder = provider.bind<PropertyTestBinder>("")
                testBinder.booleanProperty.should.be.`true`
                testBinder.integerProperty.should.be.equal(1)
                testBinder.longProperty.should.be.equal(2)
                testBinder.shortProperty.should.be.equal(1)
                testBinder.floatProperty.should.be.equal(2.1F)
                testBinder.doubleProperty.should.be.equal(1.1)
                testBinder.byteProperty.should.be.equal(2)
                testBinder.a.should.be.equal("b")
                testBinder.c.should.be.equal("d")
                testBinder.bigDecimalProperty.should.be.equal(BigDecimal("1.1"))
                testBinder.bigIntegerProperty.should.be.equal(BigInteger("1"))
                testBinder.uri.should.be.equal(URI("https://www.amazon.com"))
                testBinder.url.should.be.equal(URL("https://www.amazon.com"))
                testBinder.file.should.be.equal(File("myfile.txt"))
                testBinder.path.should.be.equal(Paths.get("mypath.txt"))
                testBinder.nested.normal().should.be.equal(1)
                testBinder.nested.supernested().normal().should.be.equal(2)
            }

            it("supernested binding") {
                val superNested = provider.bind<NestedBinder>("nested.supernested")
                superNested.normal().should.be.equal(2)
            }
        }
    }

    describe("A reload that fails must not throw an exception but call the listener") {
        val provider = DefaultConfigProvider(FailReloadConfigLoader(source))
        provider.addReloadErrorListener { assertTrue { true } }
        provider.addReloadListener { fail("This should not happen!") }
        provider.reload()
    }

    describe("a simple bytebuddy provider") {
        val provider = DefaultConfigProvider(PropertyConfigLoader(StringConfigSource("""
            a=b
            nested.a=b
            """)))
        val obj = "b".toConfig()
        val nestedObj = mapOf("a" to "b").toConfig()

        it("has the correct toString") {
            provider.load("a").toString().should.be.equal("ConfigObject(value=b)")
        }

        it("a primitive is equal to the expected ConfigObject") {
            provider.load("a").should.be.equal(obj)
        }

        it("a binding is equal to the expected ConfigObject") {
            provider.bind<Nested>("nested").toString().should.be.equal(nestedObj.toString())
        }
    }
})

// Just used for testing purposes
private class FailReloadConfigLoader(configSource: ConfigSource): PropertyConfigLoader(configSource) {
    override fun reload() {
        throw Exception("Ha! We are in a cute exception in case something happens!")
    }
}

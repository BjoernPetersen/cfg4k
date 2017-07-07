package com.jdiazcano.cfg4k.json

import com.jdiazcano.cfg4k.parsers.*
import com.jdiazcano.cfg4k.providers.Providers.cached
import com.jdiazcano.cfg4k.providers.Providers.proxy
import com.jdiazcano.cfg4k.providers.bind
import com.jdiazcano.cfg4k.providers.getProperty
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
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.test.assertFailsWith

class ConfigProviderTest: Spek({

    val providers = listOf(
            proxy(JsonConfigLoader(javaClass.classLoader.getResource("test.json"))),
            cached(proxy(JsonConfigLoader(javaClass.classLoader.getResource("test.json"))))
    )

    providers.forEachIndexed { i, provider ->
        describe("provider[$i]") {
            it("default values") {
                provider.getProperty("this.does.not.exist", 1).should.be.equal(1)
                // When having a cached provider then it will cache the "this.does.not.exist" if it has a default value
                // because the delegated provider will return the default value. Should the default value not be passed
                // and the exception caught? I think that would mean a performance impact and having exceptions into
                // account for normal logic is not right
                assertFailsWith<SettingNotFound> {
                    provider.getProperty<Int>("i.dont.extist")
                }
            }

            it("integer properties") {
                provider.getProperty("integerProperty", Int::class.java).should.be.equal(1)
            }

            it("long properties") {
                provider.getProperty("longProperty", Long::class.java).should.be.equal(2)
            }

            it("short properties") {
                provider.getProperty("shortProperty", Short::class.java).should.be.equal(1)
            }

            it("float properties") {
                provider.getProperty("floatProperty", Float::class.java).should.be.equal(2.1F)
            }

            it("double properties") {
                provider.getProperty("doubleProperty", Double::class.java).should.be.equal(1.1)
            }

            it("byte properties") {
                provider.getProperty("byteProperty", Byte::class.java).should.be.equal(2)
            }

            it("boolean properties") {
                provider.getProperty("booleanProperty", Boolean::class.java).should.be.`true`
            }

            it("big integer properties") {
                provider.getProperty<BigInteger>("bigIntegerProperty").should.be.equal(BigInteger("1"))
            }

            it("big decimal properties") {
                provider.getProperty<BigDecimal>("bigDecimalProperty").should.be.equal(BigDecimal("1.1"))
            }

            it("file properties") {
                provider.getProperty<File>("file").should.be.equal(File("myfile.txt"))
            }

            it("path properties") {
                provider.getProperty<Path>("path").should.be.equal(Paths.get("mypath.txt"))
            }

            it("url properties") {
                provider.getProperty<URL>("url").should.be.equal(URL("https://www.amazon.com"))
            }

            it("uri properties") {
                provider.getProperty<URI>("uri").should.be.equal(URI("https://www.amazon.com"))
            }

            it("date property") {
                Parsers.addParser(Date::class.java, DateParser("dd-MM-yyyy"))
                val date = provider.getProperty<Date>("dateProperty")

                // A calendar must be built on top of that date to work with it
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.get(Calendar.DAY_OF_YEAR).should.be.equal(1)
                calendar.get(Calendar.MONTH).should.be.equal(0)
                calendar.get(Calendar.YEAR).should.be.equal(2017)
            }

            it("localdateproperty property") {
                Parsers.addParser(LocalDate::class.java, LocalDateParser("dd-MM-yyyy"))
                val localDate = provider.getProperty<LocalDate>("localDateProperty")
                localDate.dayOfYear.should.be.equal(1)
                localDate.month.should.be.equal(Month.JANUARY)
                localDate.year.should.be.equal(2017)
            }

            it("isolocaldateproperty property") {
                Parsers.addParser(LocalDate::class.java, LocalDateParser(DateTimeFormatter.ISO_LOCAL_DATE))
                val localDate = provider.getProperty<LocalDate>("isoLocalDateProperty")
                localDate.dayOfYear.should.be.equal(1)
                localDate.month.should.be.equal(Month.JANUARY)
                localDate.year.should.be.equal(2017)
            }

            it("localdatetime property") {
                Parsers.addParser(LocalDateTime::class.java, LocalDateTimeParser("dd-MM-yyyy HH:mm:ss"))
                val localDateTime = provider.getProperty<LocalDateTime>("localDateTimeProperty")
                localDateTime.dayOfYear.should.be.equal(1)
                localDateTime.month.should.be.equal(Month.JANUARY)
                localDateTime.year.should.be.equal(2017)
                localDateTime.hour.should.be.equal(18)
                localDateTime.minute.should.be.equal(1)
                localDateTime.second.should.be.equal(31)
            }

            it("isolocaldatetime property") {
                Parsers.addParser(LocalDateTime::class.java, LocalDateTimeParser(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                val localDateTime = provider.getProperty<LocalDateTime>("isoLocalDateTimeProperty")
                localDateTime.dayOfYear.should.be.equal(1)
                localDateTime.month.should.be.equal(Month.JANUARY)
                localDateTime.year.should.be.equal(2017)
                localDateTime.hour.should.be.equal(18)
                localDateTime.minute.should.be.equal(1)
                localDateTime.second.should.be.equal(31)
            }

            it("zoneddatetime property") {
                Parsers.addParser(ZonedDateTime::class.java, ZonedDateTimeParser("dd-MM-yyyy HH:mm:ss"))
                val zonedDateTime = provider.getProperty<ZonedDateTime>("zonedDateTimeProperty")
                zonedDateTime.dayOfYear.should.be.equal(1)
                zonedDateTime.month.should.be.equal(Month.JANUARY)
                zonedDateTime.year.should.be.equal(2017)
                zonedDateTime.hour.should.be.equal(18)
                zonedDateTime.minute.should.be.equal(1)
                zonedDateTime.second.should.be.equal(31)
            }

            it("isozoneddatetime property") {
                Parsers.addParser(ZonedDateTime::class.java, ZonedDateTimeParser(DateTimeFormatter.ISO_ZONED_DATE_TIME))
                val zonedDateTime = provider.getProperty<ZonedDateTime>("isoZonedDateTimeProperty")
                zonedDateTime.dayOfYear.should.be.equal(1)
                zonedDateTime.month.should.be.equal(Month.JANUARY)
                zonedDateTime.year.should.be.equal(2017)
                zonedDateTime.hour.should.be.equal(18)
                zonedDateTime.minute.should.be.equal(1)
                zonedDateTime.second.should.be.equal(31)
            }

            it("offsetdatetime property") {
                Parsers.addParser(OffsetDateTime::class.java, OffsetDateTimeParser("dd-MM-yyyy HH:mm:ssXXX"))
                val offsetDateTime = provider.getProperty<OffsetDateTime>("offsetDateTimeProperty")
                offsetDateTime.dayOfYear.should.be.equal(1)
                offsetDateTime.month.should.be.equal(Month.JANUARY)
                offsetDateTime.year.should.be.equal(2017)
                offsetDateTime.hour.should.be.equal(18)
                offsetDateTime.minute.should.be.equal(1)
                offsetDateTime.second.should.be.equal(31)
            }

            it("isooffsetdatetime property") {
                Parsers.addParser(OffsetDateTime::class.java, OffsetDateTimeParser(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                val offsetDateTime = provider.getProperty<OffsetDateTime>("isoOffsetDateTimeProperty")
                offsetDateTime.dayOfYear.should.be.equal(1)
                offsetDateTime.month.should.be.equal(Month.JANUARY)
                offsetDateTime.year.should.be.equal(2017)
                offsetDateTime.hour.should.be.equal(18)
                offsetDateTime.minute.should.be.equal(1)
                offsetDateTime.second.should.be.equal(31)
            }

            it("offsettime property") {
                Parsers.addParser(OffsetTime::class.java, OffsetTimeParser("HH:mm:ssXXX"))
                val offsetTime = provider.getProperty<OffsetTime>("offsetTimeProperty")
                offsetTime.hour.should.be.equal(18)
                offsetTime.minute.should.be.equal(1)
                offsetTime.second.should.be.equal(31)
            }

            it("isooffsettime property") {
                Parsers.addParser(OffsetTime::class.java, OffsetTimeParser(DateTimeFormatter.ISO_OFFSET_TIME))
                val offsetTime = provider.getProperty<OffsetTime>("isoOffsetTimeProperty")
                offsetTime.hour.should.be.equal(18)
                offsetTime.minute.should.be.equal(1)
                offsetTime.second.should.be.equal(31)
            }

            it("calendar property") {
                Parsers.addParser(Calendar::class.java, CalendarParser("dd-MM-yyyy"))
                val calendar = provider.getProperty<Calendar>("calendarProperty")
                calendar.get(Calendar.DAY_OF_YEAR).should.be.equal(1)
                calendar.get(Calendar.MONTH).should.be.equal(0)
                calendar.get(Calendar.YEAR).should.be.equal(2017)
            }

            it("binding test") {
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
                testBinder.list().should.be.equal(listOf(1, 2, 3, 4, 5, 6, 7))
                testBinder.floatList().should.be.equal(listOf(1.2F, 2.2F, 3.2F))
                testBinder.bigDecimalProperty().should.be.equal(BigDecimal("1.1"))
                testBinder.bigIntegerProperty().should.be.equal(BigInteger("1"))
                testBinder.uri().should.be.equal(URI("https://www.amazon.com"))
                testBinder.url().should.be.equal(URL("https://www.amazon.com"))
                testBinder.file().should.be.equal(File("myfile.txt"))
                testBinder.path().should.be.equal(Paths.get("mypath.txt"))

                // toString should be the object tostring not the one that comes from the property
                testBinder.toString().should.not.be.equal("this should not be ever used")
            }
        }
    }
})

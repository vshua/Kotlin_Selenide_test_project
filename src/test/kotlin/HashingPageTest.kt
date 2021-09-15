import PageObjects.HashType
import PageObjects.setChecked
import com.codeborne.selenide.Condition
import com.codeborne.selenide.Condition.*
import com.codeborne.selenide.Selenide
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.openqa.selenium.Keys
import java.util.stream.Stream


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HashingPageTest {

    @BeforeAll
    fun openBrowser() {
        Selenide.open("http://10.0.0.117/turnontestmode")
    }

    @BeforeEach
    fun openHashingPageForEmu1() {
        Selenide.element("#qa-hash-side-panel-btn").click()
        Selenide.`$x`("//*[contains(@class,'qa-device-serial') and ./*[text()='S4035SAD']]").click()
//        Thread.sleep(1000)
    }

    @ParameterizedTest
    @MethodSource("provideStringsForStartAndEndLba")
    fun lbaStartAndEnd(
        startLba: String,
        endLba: String,
        errorStartLbaHigherThanLastText: String,
        exceedMaxLbaErrorText: String,
        buttonState: Condition
    ) {
        Selenide.element("#qa-start-lba").sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE)
        Selenide.element("#qa-start-lba").sendKeys(startLba)

        Selenide.element("#qa-end-lba").sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE)
        Selenide.element("#qa-end-lba").sendKeys(endLba)

        Selenide.element("#qa-lbas-error").shouldHave(exactText(errorStartLbaHigherThanLastText))

        Selenide.element("#qa-end-lba-error").shouldHave(exactText(exceedMaxLbaErrorText))

        Selenide.element("#qa-start-btn").shouldHave(buttonState)
    }

    private fun provideStringsForStartAndEndLba(): Stream<Arguments> {
        return Stream.of(
            Arguments.of("0", "200000", "", "", enabled),
            Arguments.of("1", "1", "", "", enabled),
            Arguments.of("10000", "1", "Start LBA should be less than or equal to end LBA", "", disabled),
            Arguments.of("55", "19931249999", "", "LBA shouldn't exceed device sector count", disabled)
        )
    }

    @Test
    fun defaultHashingSettings() {
        Selenide.element("#qa-start-lba").shouldHave(value("0"))
        Selenide.element("#qa-end-lba").shouldHave(value("19,531,249,999"))
        Selenide.`$x`("//*[@type='checkbox' and @name='MD5']").`is`(checked)
        Selenide.`$x`("//*[@type='checkbox' and @name='SHA1']").`is`(not(checked))
        Selenide.`$x`("//*[@type='checkbox' and @name='SHA256']").`is`(not(checked))
        Selenide.`$x`("//*[@type='checkbox' and @name='SHA512']").`is`(not(checked))
    }

    @ParameterizedTest
    @MethodSource("provideStringsForHashingWithDifferentHashTypes")
    fun hashingWithDifferentHashTypes(
        startLba: String,
        endLba: String,
        enabledHashTypes: Map<HashType, String>
    ) {
        Selenide.element("#qa-start-lba").sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE)
        Selenide.element("#qa-start-lba").sendKeys(startLba)

        Selenide.element("#qa-end-lba").sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE)
        Selenide.element("#qa-end-lba").sendKeys(endLba)

        HashType.values().forEach { hashType ->
            Selenide.`$x`("//*[@type='checkbox' and @name='${hashType.name}']").setChecked(false)
        }

        enabledHashTypes.forEach { hashType ->
            Selenide.`$x`("//*[@type='checkbox' and @name='${hashType.key}']").setChecked(true)
        }

        Thread.sleep(2000)

        Selenide.element("#qa-start-btn").click()

        enabledHashTypes.forEach { hashType ->
            Selenide.`$x`("//*[@id='hash-result-${hashType.key}']/div[2]").shouldHave(exactText(hashType.value))
        }
    }

    private fun provideStringsForHashingWithDifferentHashTypes(): Stream<Arguments> {
        return Stream.of(
            Arguments.of("0", "1000", mapOf(HashType.MD5 to "3fa0e3cd9985f007376c8b58289b9d3e")),
            Arguments.of("0", "1000", mapOf(HashType.SHA1 to "498b89d81630eff2da8774bbcf7e83fb9acf4a0d")),
//            Arguments.of("0", "1000",  listOf(HashType.SHA1), ""),
//            Arguments.of("0", "1000", listOf(HashType.SHA256), ""),
//            Arguments.of("0", "1000", listOf(HashType.SHA512), ""),
//            Arguments.of("0", "1000", listOf(HashType.SHA256, HashType.SHA512), "")
        )
    }


}

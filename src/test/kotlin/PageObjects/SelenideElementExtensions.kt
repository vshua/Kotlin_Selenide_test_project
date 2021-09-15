package PageObjects

import com.codeborne.selenide.Condition.*
import com.codeborne.selenide.SelenideElement

fun SelenideElement.setChecked(value: Boolean) {
    if (this.`is`(checked) != value)
        this.click()
}

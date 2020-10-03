import kotlinx.browser.document
import react.dom.render
import react.child

fun main() {
    render(document.getElementById("root")) {
        child(ContactApp)
    }
}

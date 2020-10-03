import react.*
import react.dom.*
import kotlinext.js.*
import kotlinx.html.js.*
import kotlinx.coroutines.*
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event

private val scope = MainScope()

val ContactApp = functionalComponent<RProps> { _ ->
    val (contactList, setContactList) = useState(emptyList<ContactItem>())

    var (from, setFrom) = useState(0)
    useEffect(dependencies = listOf()) {
        scope.launch {
            setContactList(getContactList(from))
        }
    }

    h1 {
        +"Contact List"
    }
    button {
        +"<"
        attrs.onClickFunction = {
            val f = if(from-10 <= 0) 0 else from-10
            setFrom(f)
            scope.launch {
                setContactList(getContactList(f))
            }
        }
    }
    button {
        +">"
        attrs.onClickFunction = {
            setFrom(from+10)
            scope.launch {
                setContactList(getContactList(from+10))
            }
        }
    }
    val (name, setName) = useState("")
    val (email, setEmail) = useState("")
    ul {
        contactList.sortedByDescending(ContactItem::name).forEach { item ->
            li {
                key = item.toString()
                +"[${item.name}] [${item.email}]  "
                button {
                    +"Delete"
                    attrs.onClickFunction = {
                        scope.launch {
                            deleteContactItem(item)
                            setContactList(getContactList(from))
                        }
                    }
                }
                form {
                    input(InputType.text) {
                        attrs.onChangeFunction = {
                            val value = (it.target as HTMLInputElement).value
                            setName(value)
                        }
                    }
                    input(InputType.email) {
                        attrs.onChangeFunction = {
                            val value = (it.target as HTMLInputElement).value
                            setEmail(value)
                        }
                    }
                    button {
                        +"Update"
                        attrs.onClickFunction = {
                            it.preventDefault()
                            val cartItem = ContactItem(name, email)
                            scope.launch {
                                updateContactItem(item, cartItem)
                                setContactList(getContactList(from))
                            }
                        }
                    }
                }
            }
        }
    }
    h3{
        +"Contact Details"
    }

    form {
        input(InputType.text) {
            attrs.onChangeFunction = {
                val value = (it.target as HTMLInputElement).value
                setName(value)
            }
        }
        input(InputType.email) {
            attrs.onChangeFunction = {
                val value = (it.target as HTMLInputElement).value
                setEmail(value)
            }
        }
        button {
            +"Insert"
            attrs.onClickFunction = {
                it.preventDefault()
                val cartItem = ContactItem(name, email)
                scope.launch {
                    addContactItem(cartItem)
                    setContactList(getContactList(from))
                }
            }
        }
    }
    h3{
        +"Search APIs"
    }
    h5 {
        +"Search By Name: "
    }
    val (searchString, setSearchString)= useState("")
    child(
        InputComponent,
        props = jsObject {
            onSubmit = { input ->
                setFrom(0)
                setSearchString(input)
                scope.launch {
                    setContactList(searchByName(input, 0))
                }
            }
        }
    )
    button {
        +"<"
        attrs.onClickFunction = {
            val f = if(from-10 <= 0) 0 else from-10
            setFrom(f)
            scope.launch {
                setContactList(searchByName(searchString, f))
            }
        }
    }
    button {
        +">"
        attrs.onClickFunction = {
            setFrom(from+10)
            scope.launch {
                setContactList(searchByName(searchString, from+10))
            }
        }
    }
    h5 {
        +"Search By Email: "
    }
    child(
        InputComponent,
        props = jsObject {
            onSubmit = { input ->
                scope.launch {
                    setContactList(searchByEmail(input))
                }
            }
        }
    )
}
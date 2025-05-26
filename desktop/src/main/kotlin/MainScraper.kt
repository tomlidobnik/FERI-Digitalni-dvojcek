import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.skrape
import it.skrape.fetcher.response
import it.skrape.core.htmlDocument
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import it.skrape.selects.html5.a
import it.skrape.selects.html5.span
import java.io.File

@Serializable
data class MapLocation(
    val name: String?,
    val address: String?,
    val url: String,
    val lat: String?,
    val lon: String?
)

fun scrapeMariborIgrisce() {
    val baseUrl = "https://maribor.si/mestni-servis/otroci/javna-igrisca/"
    val locations = mutableListOf<MapLocation>()
    val regex = Regex("@([\\-0-9\\.]+),([\\-0-9\\.]+),")

    for (page in 1..4) {
        val url = if (page == 1) baseUrl else "$baseUrl?stran=$page"
        skrape(HttpFetcher) {
            request { this.url = url }
            response {
                htmlDocument {
                    findFirst(".table_wrap") {
                        findFirst("table") {
                            findAll("tr").forEach { row ->
                                val tds = try { row.findAll("td") } catch (_: Exception) { emptyList() }
                                if (tds.size >= 3) {
                                    val name = tds[0].text
                                    val address = tds[1].text
                                    val aElem = tds[2].findAll("a").firstOrNull()
                                    val href = aElem?.attribute("href")
                                    if (href != null && href.contains("google.com/maps")) {
                                        val link = if (href.startsWith("/")) "https://maribor.si$href" else href
                                        val match = regex.find(link)
                                        val lat = match?.groupValues?.getOrNull(1)
                                        val lon = match?.groupValues?.getOrNull(2)
                                        locations.add(MapLocation(name, address, link, lat, lon))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val fileName = "locations2.json"
    val jsonString = Json { prettyPrint = true }.encodeToString(locations)
    File(fileName).writeText(jsonString)
    println("Saved ${locations.size} google.com/maps links with names, addresses, and coordinates to $fileName")
}

fun scrapeSportMaribor() {
    val baseUrl = "https://www.sportmaribor.si/sportni-objekti/"
    val links = mutableListOf<String>()
    skrape(HttpFetcher) {
        request { url = baseUrl }
        response {
            htmlDocument {
                a {
                    findAll {
                        mapNotNull { it.attribute("href") }
                            .filter { it.contains("/sportni-objekti/") }
                            .map { href ->
                                if (!href.startsWith("http")) {
                                    "https://www.sportmaribor.si$href"
                                } else {
                                    href
                                }
                            }
                            .filter { it != baseUrl }
                            .forEach { links.add(it) }
                    }
                }
            }
        }
    }

    println("Collected Links:")
    links.forEach { println(it) }

    val locationsMap = mutableMapOf<String, String>()
    links.forEach { link ->
        println("Visiting: $link")
        skrape(HttpFetcher) {
            request { url = link }
            response {
                htmlDocument {
                    val h1Text = findFirst("h1") { text }
                    var address: String? = null
                    span {
                        findAll {
                            forEach { spanElem ->
                                if (spanElem.text == "Lokacija") {
                                    val parent = spanElem.parent
                                    try {
                                        address = parent?.findFirst("p") { text }
                                    } catch (e: it.skrape.selects.ElementNotFoundException) {
                                        // No <p> found, skip
                                    }
                                }
                            }
                        }
                    }
                    if (!h1Text.isNullOrBlank() && !address.isNullOrBlank()) {
                        locationsMap[h1Text] = address!!
                        println("Found: $h1Text -> $address")
                    }
                }
            }
        }
    }

    val fileName = "locations.json"
    val jsonString = Json { prettyPrint = true }.encodeToString(locationsMap)
    File(fileName).writeText(jsonString)
    println("All locations have been saved to $fileName")
}

fun main() {
    scrapeMariborIgrisce()
    scrapeSportMaribor()
}
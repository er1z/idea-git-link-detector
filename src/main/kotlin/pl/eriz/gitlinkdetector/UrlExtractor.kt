package pl.eriz.gitlinkdetector

object UrlExtractor {
    private val URL_REGEX = Regex("""https?://[^\s"'<>\])}]+""")
    private val TRAILING_PUNCT = Regex("""[.,)]+$""")

    fun extract(text: String): List<String> =
        URL_REGEX.findAll(text)
            .map { TRAILING_PUNCT.replace(it.value, "") }
            .filter { it.isNotEmpty() }
            .toList()
}

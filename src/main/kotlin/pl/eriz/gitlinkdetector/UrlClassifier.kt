package pl.eriz.gitlinkdetector

import pl.eriz.gitlinkdetector.settings.GitLinkDetectorSettings

enum class UrlCategory { AUTH, PR, CUSTOM, NONE }

data class ClassificationResult(val category: UrlCategory, val label: String)

object UrlClassifier {
    private val AUTH_KEYWORDS = listOf("login", "auth", "oauth", "device", "activate", "sso", "token", "credentials")
    private val PR_KEYWORDS = listOf("pull", "merge_request", "compare", "/pr/", "new_pr", "pull-request")

    fun classify(url: String): ClassificationResult {
        val settings = GitLinkDetectorSettings.getInstance()
        val lower = url.lowercase()

        if (settings.enableAuthDetection && AUTH_KEYWORDS.any { lower.contains(it) }) {
            return ClassificationResult(UrlCategory.AUTH, "Authentication required")
        }
        if (settings.enablePrDetection && PR_KEYWORDS.any { lower.contains(it) }) {
            return ClassificationResult(UrlCategory.PR, "Pull request")
        }
        for (pattern in settings.customPatterns) {
            if (pattern.regex.isBlank()) continue
            try {
                if (Regex(pattern.regex).containsMatchIn(url)) {
                    return ClassificationResult(UrlCategory.CUSTOM, pattern.label.ifBlank { "Link" })
                }
            } catch (_: Exception) {
                // invalid regex, skip
            }
        }
        return ClassificationResult(UrlCategory.NONE, "")
    }
}

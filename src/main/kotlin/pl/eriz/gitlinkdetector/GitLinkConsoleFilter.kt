package pl.eriz.gitlinkdetector

import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

class GitLinkConsoleFilterProvider : ConsoleFilterProvider {
    override fun getDefaultFilters(project: Project): Array<Filter> = arrayOf(GitLinkConsoleFilter(project))
}

private class GitLinkConsoleFilter(private val project: Project) : Filter {
    override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
        if (!line.trimStart().startsWith("remote:")) return null
        UrlExtractor.extract(line).forEach { url ->
            val result = UrlClassifier.classify(url)
            if (result.category != UrlCategory.NONE) {
                LOG.debug("GitLinkDetector: detected ${result.category} URL: $url")
                ApplicationManager.getApplication().invokeLater {
                    BalloonNotifier.notify(project, url, result.label)
                }
            }
        }
        return null
    }

    companion object {
        private val LOG = Logger.getInstance(GitLinkConsoleFilter::class.java)
    }
}

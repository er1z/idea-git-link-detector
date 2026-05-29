package pl.eriz.gitlinkdetector

import com.intellij.execution.process.AnsiEscapeDecoder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.Pair as IdeaPair
import git4idea.commands.GitCommandOutputPrinter
import git4idea.util.GitVcsConsoleWriter
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

class GitCommandOutputInterceptor(private val project: Project) : GitCommandOutputPrinter {
    private val ansiDecoder = AnsiEscapeDecoder()

    // ConcurrentHashMap instead of ThreadLocal — showCommandStart and showCommandOutput
    // are called from different threads (dispatch vs. process-reader thread).
    private val seenUrls = ConcurrentHashMap.newKeySet<String>()

    init {
        LOG.debug("GitLinkDetector: GitCommandOutputInterceptor initialised for '${project.name}'")
    }

    private val writer: GitVcsConsoleWriter
        get() = GitVcsConsoleWriter.getInstance(project)

    override fun showCommandStart(processId: String, workingDir: Path, commandLine: String) {
        seenUrls.clear()
        writer.showCommandLine("[$workingDir] $commandLine")
    }

    override fun showCommandOutput(processId: String, workingDir: Path, outputType: Key<*>, line: String) {
        val pairs = ArrayList<IdeaPair<String, Key<*>>>()
        ansiDecoder.escapeText(line, outputType, AnsiEscapeDecoder.ColoredTextAcceptor { segment, key ->
            pairs.add(IdeaPair.create(segment, key))
        })
        writer.showMessage(pairs)

        // Strip ANSI codes before checking for the "remote:" prefix, then extract URLs.
        val cleanText = ANSI_ESCAPE.replace(line, "")
        val remoteText = cleanText.lines()
            .filter { it.trimStart().startsWith("remote:") }
            .joinToString("\n")
        if (remoteText.isEmpty()) return

        UrlExtractor.extract(remoteText).forEach { url ->
            if (seenUrls.add(url)) {
                val result = UrlClassifier.classify(url)
                if (result.category != UrlCategory.NONE) {
                    LOG.debug("GitLinkDetector: detected ${result.category} URL: $url")
                    ApplicationManager.getApplication().invokeLater {
                        BalloonNotifier.notify(project, url, result.label)
                    }
                }
            }
        }
    }

    override fun showCommandFinished(processId: String, workingDir: Path, exitCode: Int) {}

    companion object {
        private val LOG = Logger.getInstance(GitCommandOutputInterceptor::class.java)
        private val ANSI_ESCAPE = Regex("\\[[0-9;]*m")
    }
}

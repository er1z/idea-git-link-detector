package pl.eriz.gitlinkdetector.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "GitLinkDetectorSettings",
    storages = [Storage("gitLinkDetector.xml")]
)
@Service(Service.Level.APP)
class GitLinkDetectorSettings : PersistentStateComponent<GitLinkDetectorSettings> {
    var enableAuthDetection: Boolean = true
    var enablePrDetection: Boolean = true
    var customPatterns: MutableList<CustomPattern> = mutableListOf()

    override fun getState(): GitLinkDetectorSettings = this

    override fun loadState(state: GitLinkDetectorSettings) = XmlSerializerUtil.copyBean(state, this)

    companion object {
        fun getInstance(): GitLinkDetectorSettings =
            ApplicationManager.getApplication().getService(GitLinkDetectorSettings::class.java)
    }
}

class CustomPattern {
    var regex: String = ""
    var label: String = "Link"

    constructor()
    constructor(regex: String, label: String) {
        this.regex = regex
        this.label = label
    }
}

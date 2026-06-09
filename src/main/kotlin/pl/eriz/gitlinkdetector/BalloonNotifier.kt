package pl.eriz.gitlinkdetector

import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object BalloonNotifier {
    private var lastUrl: String? = null

    fun notify(project: Project?, url: String, label: String) {
        if (url == lastUrl) return
        lastUrl = url
        NotificationGroupManager.getInstance()
            .getNotificationGroup("GitLinkDetector")
            .createNotification("Git Link Detected", "$label: $url", NotificationType.INFORMATION)
            .addAction(NotificationAction.createSimple("Open in Browser") { BrowserUtil.browse(url) })
            .notify(project)
    }
}

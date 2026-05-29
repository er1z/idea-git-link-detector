package pl.eriz.gitlinkdetector

import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object BalloonNotifier {
    fun notify(project: Project?, url: String, label: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("GitLinkDetector")
            .createNotification("Git Link Detected", "$label: $url", NotificationType.INFORMATION)
            .addAction(NotificationAction.createSimple("Open in Browser") { BrowserUtil.browse(url) })
            .notify(project)
    }
}

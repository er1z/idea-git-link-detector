package pl.eriz.gitlinkdetector.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.table.AbstractTableModel

class GitLinkDetectorConfigurable : Configurable {
    private var enableAuthCheckbox: JBCheckBox? = null
    private var enablePrCheckbox: JBCheckBox? = null
    private var tableModel: CustomPatternsTableModel? = null
    private var table: JBTable? = null

    override fun getDisplayName() = "Git Link Detector"

    override fun createComponent(): JComponent {
        enableAuthCheckbox = JBCheckBox("Enable auth/login link detection")
        enablePrCheckbox = JBCheckBox("Enable PR/MR link detection")
        val model = CustomPatternsTableModel()
        tableModel = model
        table = JBTable(model).apply {
            columnModel.getColumn(0).preferredWidth = 300
            columnModel.getColumn(1).preferredWidth = 150
        }
        val decorator = ToolbarDecorator.createDecorator(table!!)
            .setAddAction { model.addRow(CustomPattern()) }
            .setRemoveAction { model.removeRow(table!!.selectedRow) }
            .disableUpDownActions()
        return FormBuilder.createFormBuilder()
            .addComponent(enableAuthCheckbox!!)
            .addComponent(enablePrCheckbox!!)
            .addLabeledComponent("Custom URL patterns:", decorator.createPanel(), true)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean {
        val settings = GitLinkDetectorSettings.getInstance()
        if (enableAuthCheckbox?.isSelected != settings.enableAuthDetection) return true
        if (enablePrCheckbox?.isSelected != settings.enablePrDetection) return true
        val modelPatterns = tableModel?.patterns ?: return false
        if (modelPatterns.size != settings.customPatterns.size) return true
        return modelPatterns.zip(settings.customPatterns).any { (a, b) ->
            a.regex != b.regex || a.label != b.label
        }
    }

    override fun apply() {
        val settings = GitLinkDetectorSettings.getInstance()
        settings.enableAuthDetection = enableAuthCheckbox?.isSelected ?: true
        settings.enablePrDetection = enablePrCheckbox?.isSelected ?: true
        settings.customPatterns = tableModel?.patterns
            ?.map { CustomPattern(it.regex, it.label) }
            ?.toMutableList()
            ?: mutableListOf()
    }

    override fun reset() {
        val settings = GitLinkDetectorSettings.getInstance()
        enableAuthCheckbox?.isSelected = settings.enableAuthDetection
        enablePrCheckbox?.isSelected = settings.enablePrDetection
        tableModel?.setPatterns(
            settings.customPatterns.map { CustomPattern(it.regex, it.label) }.toMutableList()
        )
    }
}

private class CustomPatternsTableModel : AbstractTableModel() {
    val patterns: MutableList<CustomPattern> = mutableListOf()
    private val columns = arrayOf("Pattern (regex)", "Label")

    override fun getRowCount() = patterns.size
    override fun getColumnCount() = 2
    override fun getColumnName(col: Int) = columns[col]
    override fun isCellEditable(row: Int, col: Int) = true
    override fun getValueAt(row: Int, col: Int): Any =
        if (col == 0) patterns[row].regex else patterns[row].label

    override fun setValueAt(value: Any?, row: Int, col: Int) {
        val v = value as? String ?: return
        if (col == 0) patterns[row].regex = v else patterns[row].label = v
        fireTableCellUpdated(row, col)
    }

    fun addRow(pattern: CustomPattern) {
        patterns.add(pattern)
        fireTableRowsInserted(patterns.size - 1, patterns.size - 1)
    }

    fun removeRow(row: Int) {
        if (row < 0 || row >= patterns.size) return
        patterns.removeAt(row)
        fireTableRowsDeleted(row, row)
    }

    fun setPatterns(newPatterns: MutableList<CustomPattern>) {
        patterns.clear()
        patterns.addAll(newPatterns)
        fireTableDataChanged()
    }
}

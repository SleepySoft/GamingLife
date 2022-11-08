package glcore

import java.util.*


class GlDailyStatistics() {
    val dailyData: PathDict = PathDict()

    fun loadDailyData(dateTime: Date) : Boolean {
        val offsetDays = GlDateTime.daysBetween(GlDateTime.datetime(), dateTime)
        val dayFolderName = GlRoot.getDailyFolderName(offsetDays)
        val dailyFileName: String = GlRoot.getFileNameTs()

        val fileContent: String = GlFile.loadFile(
            GlFile.joinPaths(dayFolderName, dailyFileName)).toString(Charsets.UTF_8)

        return if (fileContent.isNotEmpty()) {
            dailyData.attach(GlJson.deserializeAnyDict(fileContent))
            dailyData.hasUpdate = false
            true
        } else {
            false
        }
    }

    fun loadDailyData(dayOffset: Int) : Boolean {
        return loadDailyData(GlDateTime.datetime(dayOffset))
    }

    private fun parseDailyData() {

    }
}
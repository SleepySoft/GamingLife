package glcore

import android.os.Build
import android.support.annotation.RequiresApi
import java.util.Date


class GlSystemConfig() {
    val taskTop: MutableList< TaskData > = mutableListOf()
    val taskSub: MutableList< TaskData > = mutableListOf()
    val taskGroupLink: MutableMap< String , String > = mutableMapOf()      // sub id: top id

    // ------------------------------------------- Gets --------------------------------------------

    fun getTopTasks(): List< TaskData > = taskTop

    fun getSubTasks(): List< TaskData > = taskSub

    fun getTaskData(glId: String) : TaskData? = getTopTaskData(glId) ?: getSubTaskData(glId)

    fun getTopTaskData(glId: String) : TaskData? {
        val taskData = taskTop.filter { it.id == glId }
        return if (taskData.isNotEmpty()) taskData[0] else null
    }

    fun getSubTaskData(glId: String) : TaskData? {
        val taskData = taskTop.filter { it.id == glId }
        return if (taskData.isNotEmpty()) taskData[0] else null
    }

    fun getTopGroupOfTask(glId: String): String =
        getTopTaskData(glId)?.id ?: taskGroupLink[glId] ?: GROUP_ID_IDLE

    fun getTaskInTopGroup(topTaskId: String): List< TaskData > {
        val taskData = mutableListOf< TaskData >()
        if (getTopTaskData(topTaskId) != null) {
            for ((k, v) in taskGroupLink) {
                if (v == topTaskId) {
                    getSubTaskData(k)?.run {
                        taskData.add(this)
                    }
                }
            }
        }
        return taskData
    }

    // ------------------------------------------- Sets --------------------------------------------

    @RequiresApi(Build.VERSION_CODES.N)
    fun removeSubTask(glId: String) {
        taskSub.removeIf { it.id == glId }
    }

    fun updateTaskData(taskData: TaskData) {
        val existsTaskData = getTaskData(taskData.id)
        if (existsTaskData == null) {
            taskSub.add(taskData)
        } else {
            taskData.run {
                this.name = taskData.name
                this.color = taskData.color
            }
        }
    }

    fun setTaskSubGroup(glId: String, groupId: String) {
        if ((getSubTaskData(glId) != null) &&
            (getTopTaskData(groupId) != null)) {
            taskGroupLink[glId] = groupId
        }
    }

    // ----------------------- Task Switching -----------------------

}
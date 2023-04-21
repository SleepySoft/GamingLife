package com.sleepysoft.gaminglife

import android.content.Context
import com.sleepysoft.gaminglife.R
import glcore.*


object UiRes {

    val TASK_GROUP_SELECT_ORDER = listOf(
        GROUP_ID_CREATE,
        GROUP_ID_PROMOTE,
        GROUP_ID_LIFE,
        GROUP_ID_IDLE,
        GROUP_ID_ENJOY,
        GROUP_ID_WORK
    )

    private val STRING_ENUM_MAPPING = mapOf(
        "TASK_PERIOD_ARRAY" to ENUM_TASK_PERIOD_ARRAY,
        "TASK_TIME_QUALITY_ARRAY" to ENUM_TIME_QUALITY_ARRAY
    )

    private var initFlag = false
    private val stringArrayResDict = mutableMapOf< String , List< String > >()

    fun init(context: Context) {
        if (!initFlag) {
            initFlag = true

            stringArrayResDict["TASK_PERIOD_ARRAY"] = getListRes(context, R.array.TASK_PERIOD_ARRAY)
            stringArrayResDict["TASK_TIME_QUALITY_ARRAY"] = getListRes(context, R.array.TASK_TIME_QUALITY_ARRAY)
            stringArrayResDict["TASK_GROUP_DISPLAY_ARRAY"] = getListRes(context, R.array.TASK_GROUP_DISPLAY_ARRAY)

            assert((stringArrayResDict["TASK_PERIOD_ARRAY"]?.size ?: 0) == ENUM_TASK_PERIOD_ARRAY.size)
            assert((stringArrayResDict["TASK_TIME_QUALITY_ARRAY"]?.size ?: 0) == ENUM_TIME_QUALITY_ARRAY.size)
            assert((stringArrayResDict["TASK_GROUP_DISPLAY_ARRAY"]?.size ?: 0) == TASK_GROUP_SELECT_ORDER.size)
        }
    }

    fun stringArray(arrId: String) : List< String > = stringArrayResDict[arrId] ?: listOf()

    fun stringToEnum(arrId: String, index: Int) : Int {
        val enumArr = STRING_ENUM_MAPPING[arrId] ?: listOf()
        return if ((index >= 0) && (index < enumArr.size)) enumArr[index] else -1
    }

    fun getListRes(context: Context, res: Int) : List< String > {
        return context.resources.getStringArray(res).toList()
    }
}
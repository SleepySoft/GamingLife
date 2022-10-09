package glcore

import android.util.Log
import kotlin.reflect.KClass

const val SYSTEM_META_TASK_GROUP_TOP = "/Config/Meta/TaskGroup/TopGroups"
const val SYSTEM_META_TASK_GROUP_SUB = "/Config/Meta/TaskGroup/SubGroups"
const val SYSTEM_META_TASK_GROUP_LINK = "/Config/Meta/TaskGroup/GroupLinks"
const val SYSTEM_META_TASK_GROUP_COLOR = "/Config/Meta/TaskGroup/GroupColor"

const val RUNTIME_CURRENT_TASK = "/Runtime/TimeModule/CurrentTask"

const val GROUP_ID_RELAX = "5084b76d-4e75-4c44-9786-bdf94075f94d"

val TASK_GROUP_TOP_PRESET = mapOf(
    GROUP_ID_RELAX to "放松",
    "f9fb401a-dc28-463f-92a6-0d30bd8730bb" to "玩乐",
    "3e9fd903-9c51-4301-b610-715205983573" to "生活",
    "11000041-0376-4876-9efa-8a6a7028140d" to "工作",
    "1841978a-3adc-413a-a9ae-a34e019205f8" to "学习",
    "fa94a546-beeb-4570-b266-c066a4a31233" to "创作",
)

// https://material.io/design/color/the-color-system.html#tools-for-picking-colors

val TASK_GROUP_COLOR_PRESET = mapOf< String, Long >(
    GROUP_ID_RELAX to 0x00BBDEFB,                               // Blue 100
    "f9fb401a-dc28-463f-92a6-0d30bd8730bb" to 0x00FBBC05,       // Yellow
    "3e9fd903-9c51-4301-b610-715205983573" to 0x0034A853,       // Green
    "11000041-0376-4876-9efa-8a6a7028140d" to 0x00EA4335,       // Red
    "1841978a-3adc-413a-a9ae-a34e019205f8" to 0x00F9A825,       // Yellow 800
    "fa94a546-beeb-4570-b266-c066a4a31233" to 0x004485F4,       // Blue
)

val TASK_RECORD_TEMPLATE = mapOf(
    "taskID" to "",
    "groupID" to GROUP_ID_RELAX,
    "startTime" to System.currentTimeMillis()
)

// ---------------------------------------- Struct Defines -----------------------------------------

val STRUCT_DEC_TASK_DATA = mapOf< String, KClass< * > >(
    "id" to String::class,
    "name" to String::class,
    "color" to String::class
)

val STRUCT_DEC_TASK_RECORD = mapOf< String, KClass< * > >(
    "taskID" to String::class,
    "groupID" to String::class,
    "startTime" to Long::class
)

fun checkStruct(structDict: Map< String, Any >,
                structDeclare: Map< String, KClass< * > >) : Boolean {
    for ((k, v) in structDeclare) {
        if ((structDict[k] == null) || (structDict[k]!!::class != v)) {
            System.out.println(
                "Structure mismatch: Field [$k], Expect [$v], But [${structDict[k]}]")
            return false
        }
    }
    return true;
}


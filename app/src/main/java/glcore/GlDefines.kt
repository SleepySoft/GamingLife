package glcore
import kotlin.reflect.KClass

// --------------------------------------------- Path ----------------------------------------------

const val PATH_SYSTEM_TASK_GROUP_TOP = "/Config/Meta/TaskGroup/TopGroups"       // v as STRUCT_DEC_TASK_DATA
const val PATH_SYSTEM_TASK_GROUP_SUB = "/Config/Meta/TaskGroup/SubGroups"       // v as STRUCT_DEC_TASK_DATA
const val PATH_SYSTEM_TASK_GROUP_LINK = "/Config/Meta/TaskGroup/GroupLinks"     // v as MutableMap< String, String >

const val PATH_RUNTIME_CURRENT_TASK = "/Runtime/TimeModule/CurrentTask"         // v as STRUCT_DEC_TASK_RECORD

const val PATH_DAILY_START_TS = "/Daily/StartTimestamp"                         // The start seconds of the day since January 1, 1970, 00:00:00 GMT
const val PATH_DAILY_TASK_HISTORY = "/Daily/TimeModule/TaskHistory"             // v as list of STRUCT_DEC_TASK_RECORD


// --------------------------------------------- Value ---------------------------------------------

const val LONG_LONG_PRESS_TIMEOUT = 1500       // ms

const val GL_FILE_RUNTIME_DATA = "RuntimeData.json"
const val GL_FILE_DAILY_RECORD = "DailyRecord.json"
const val GL_FILE_SYSTEM_CONFIG = "SystemConfig.json"
const val GL_FILE_ENVIRONMENT = "Environment.json"

val GL_FILES = listOf< String >(
    GL_FILE_RUNTIME_DATA,
    GL_FILE_DAILY_RECORD,
    GL_FILE_SYSTEM_CONFIG,
    GL_FILE_ENVIRONMENT
)

const val FILE_AUDIO_TEMP_PCM = "temp/RawAudio.pcm"
const val FILE_AUDIO_TEMP_WAV = "temp/RawAudio.wav"

const val DAILY_FOLDER_TEMPLATE = "GL_DAILY_%s"

const val GROUP_ID_IDLE  = "5084b76d-4e75-4c44-9786-bdf94075f94d"
const val GROUP_ID_ENJOY  = "f9fb401a-dc28-463f-92a6-0d30bd8730bb"
const val GROUP_ID_LIFE   = "3e9fd903-9c51-4301-b610-715205983573"
const val GROUP_ID_WORK   = "11000041-0376-4876-9efa-8a6a7028140d"
const val GROUP_ID_PROMOTE  = "1841978a-3adc-413a-a9ae-a34e019205f8"
const val GROUP_ID_CREATE = "fa94a546-beeb-4570-b266-c066a4a31233"


// ---------------------------------------- Struct Support -----------------------------------------

typealias GlAnyList = MutableList< Any >
typealias GlAnyDict = MutableMap< String, Any >

typealias GlAnyStruct = GlAnyDict
typealias GlStrStruct = MutableMap< String, String >

typealias GlAnyStructList = MutableList< GlAnyStruct >
typealias GlStrStructList = MutableList< GlStrStruct >

typealias GlAnyStructDict = MutableMap< String, GlAnyStruct >
typealias GlStrStructDict = MutableMap< String, GlStrStruct >

typealias GlStructDeclare = Map< String, KClass< * > >

fun checkStruct(structDict: GlAnyStruct,
                structDeclare: GlStructDeclare) : Boolean {
    for ((k, v) in structDeclare) {
        if (v == Any::class) {
            // Accept all
            continue
        }
        if ((structDict[k] == null) || (structDict[k]!!::class != v)) {
            // User println for Unit Test
            System.out.println(
                "Structure mismatch: Field [$k], Expect [$v], But [${structDict[k]}]")
            return false
        }
    }
    return true;
}

fun checkListOfStruct(structDictList: List< GlAnyStruct >,
                      structDeclare: GlStructDeclare) : Boolean {
    for (structDict in structDictList) {
        if (!checkStruct(structDict, structDeclare)) {
            return false
        }
    }
    return true
}


// ---------------------------------------- Struct Defines -----------------------------------------

val STRUCT_DEC_TASK_DATA : GlStructDeclare = mapOf< String, KClass< * > >(
    "id" to String::class,
    "name" to String::class,
    "color" to String::class
)

val STRUCT_DEC_TASK_RECORD : GlStructDeclare = mapOf< String, KClass< * > >(
    "taskID" to String::class,
    "groupID" to String::class,
    "startTime" to Long::class
)


// -------------------------------------------- Preset ---------------------------------------------

// https://material.io/design/color/the-color-system.html#tools-for-picking-colors

val TASK_GROUP_TOP_PRESET = mapOf(

    GROUP_ID_IDLE to mapOf(
        "id" to GROUP_ID_IDLE,
        "name" to "放松",
        "color" to "#00BCD4"),      // Cyan 500

    GROUP_ID_ENJOY to mapOf(
        "id" to GROUP_ID_ENJOY,
        "name" to "玩乐",
        "color" to "#7E57C2"),      // Deep Purple 400

    GROUP_ID_LIFE to mapOf(
        "id" to GROUP_ID_LIFE,
        "name" to "生活",
        "color" to "#34A853"),      // Green

    GROUP_ID_WORK to mapOf(
        "id" to GROUP_ID_WORK,
        "name" to "工作",
        "color" to "#EA4335"),      // Red

    GROUP_ID_PROMOTE to mapOf(
        "id" to GROUP_ID_PROMOTE,
        "name" to "提升",
        "color" to "#FBBC05"),      // Yellow

    GROUP_ID_CREATE to mapOf(
        "id" to GROUP_ID_CREATE,
        "name" to "创作",
        "color" to "#4485F4"),      // Blue
)


val TASK_RECORD_TEMPLATE = mapOf(
    "taskID" to "",
    "groupID" to GROUP_ID_IDLE,
    "startTime" to System.currentTimeMillis()
)



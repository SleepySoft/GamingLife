package glcore
import kotlin.reflect.KClass

// --------------------------------------------- Path ----------------------------------------------

const val PATH_SYSTEM_TASK_GROUP_TOP_LEVEL = "/Config/Meta/TaskGroup/TopGroups"
// const val PATH_SYSTEM_TASK_GROUP_SUB = "/Config/Meta/TaskGroup/SubGroups"
// const val PATH_SYSTEM_TASK_GROUP_LINK = "/Config/Meta/TaskGroup/GroupLinks"

const val PATH_SYSTEM_TASK_RECORD_THRESHOLD = "/Config/User/TaskRecordThreshold"    // v as Long, in ms
const val PATH_SYSTEM_PRIVATE_KEY_HASH = "/Config/User/Key/Account/PrivateKeyHash"
const val PATH_SYSTEM_PRIVATE_KEY = "/Config/User/Key/Account/PrivateKey"
const val PATH_SYSTEM_PUBLIC_KEY = "/Config/User/Key/Account/PublicKey"
const val PATH_SYSTEM_GLID = "/Config/User/Key/Account/GLID"

const val PATH_SYSTEM_PERIODIC_TASK = "/Config/User/Adventure/PeriodicTaskList"

// -------------------------------------------------------------------------------------------------

const val PATH_RUNTIME_CURRENT_TASK = "/Runtime/TimeModule/CurrentTask"             // v as STRUCT_DEC_TASK_RECORD

/*
const val PATH_RUNTIME_PERIOD_DAILY_START_TS = "/Runtime/TimeModule/PeriodStartTimeStamp/Daily"
const val PATH_RUNTIME_PERIOD_WEEKLY_TS = "/Runtime/TimeModule/PeriodStartTimeStamp/Weekly"
const val PATH_RUNTIME_PERIOD_BIWEEKLY_TS = "/Runtime/TimeModule/PeriodStartTimeStamp/BiWeekly"
const val PATH_RUNTIME_PERIOD_MONTHLY_TS = "/Runtime/TimeModule/PeriodStartTimeStamp/Monthly"
const val PATH_RUNTIME_PERIOD_QUARTERLY_START_TS = "/Runtime/TimeModule/PeriodStartTimeStamp/Quarterly"
*/

const val PATH_RUNTIME_PERIODIC_TASKS_STARTED = "/Runtime/TimeModule/PeriodicTasksThatStarted"

// -------------------------------------------------------------------------------------------------

const val PATH_DAILY_START_TS = "/Daily/StartTimestamp"                             // The start seconds of the day since January 1, 1970, 00:00:00 GMT
const val PATH_DAILY_TASK_RECORD = "/Daily/TimeModule/TaskHistory"                  // v as list of STRUCT_DEC_TASK_RECORD

const val PATH_DAILY_PERIODIC_TASK_RECORD = "/Daily/TaskModule/PeriodicTaskRecords"

/*const val PATH_DAILY_TASK_RECORD = "/Daily/TaskModule/FinishedPeriodicTasks"
const val PATH_DAILY_TASK_RECORD = "/Daily/TaskModule/AbandonedPeriodicTasks"*/


// --------------------------------------------- Value ---------------------------------------------

const val SERVICE_LOCAL = "http://10.0.2.2:12288/GamingLife"
const val SERVICE_REMOTE = "http://10.0.2.2:12288/GamingLife"

const val LONG_LONG_PRESS_TIMEOUT = 1500                    // ms
const val TIMESTAMP_COUNT_IN_DAY = 24 * 60 * 60 * 1000      // Gl ts unit is ms
const val TIME_DEFAULT_TASK_RECORD_THRESHOLD = 10 * 1000    // ms
const val TIME_ONE_HOUR = 60 * 60 * 1000

const val GL_FILE_RUNTIME_DATA = "RuntimeData.json"
const val GL_FILE_DAILY_RECORD = "DailyRecord.json"
const val GL_FILE_SYSTEM_CONFIG = "SystemConfig.json"
const val GL_FILE_ENVIRONMENT_CONTEXT = "EnvironmentCtx.json"

val GL_FILES = listOf(
    GL_FILE_RUNTIME_DATA,
    GL_FILE_DAILY_RECORD,
    GL_FILE_SYSTEM_CONFIG,
    GL_FILE_ENVIRONMENT_CONTEXT
)

const val RECORD_FILE_NAME_AUDIO = "record.MP3"

const val DAILY_FOLDER_PREFIX = "GL_DAILY_"
const val DAILY_FOLDER_TEMPLATE = "GL_DAILY_%s"

const val LOCAL_KEYPAIR_MAIN_NAME = "GamingLifeLocalKeyPair"

const val GROUP_ID_IDLE  = "5084b76d-4e75-4c44-9786-bdf94075f94d"
const val GROUP_ID_ENJOY  = "f9fb401a-dc28-463f-92a6-0d30bd8730bb"
const val GROUP_ID_LIFE   = "3e9fd903-9c51-4301-b610-715205983573"
const val GROUP_ID_WORK   = "11000041-0376-4876-9efa-8a6a7028140d"
const val GROUP_ID_PROMOTE  = "1841978a-3adc-413a-a9ae-a34e019205f8"
const val GROUP_ID_CREATE = "fa94a546-beeb-4570-b266-c066a4a31233"



// --------------------------------------------- Color ---------------------------------------------

// https://material.io/design/color/the-color-system.html#tools-for-picking-colors

const val COLOR_TASK_IDLE       = "#00BCD4"             // Cyan 500
const val COLOR_TASK_ENJOY      = "#7E57C2"             // Deep Purple 400
const val COLOR_TASK_LIFE       = "#34A853"             // Green
const val COLOR_TASK_WORK       = "#EA4335"             // Red
const val COLOR_TASK_PROMOTE    = "#FBBC05"             // Yellow
const val COLOR_TASK_CREATE     = "#4485F4"             // Blue

const val COLOR_RECORD          = "#B2FF59"             // Light Green A200
const val COLOR_SUGGESTION      = "#EEFF41"             // Lime A200
const val COLOR_DAILY_BAR_BASE  = "#BDBDBD"             // Gray 400

const val COLOR_BLUE_100        = "#BBDEFB"
const val COLOR_INDIGO_100      = "#C5CAE9"

const val COLOR_STAT_BK         = "#E5E5E5"
const val COLOR_SCHEME_NORMAL   = "#87CEEB"
const val COLOR_SCHEME_EXTREME  = "#FFBC40"

const val COLOR_PERIODIC_TASK_OPTIONAL_BK           = "#99CCFF"
const val COLOR_PERIODIC_TASK_URGENCY_DAILY_START   = "#FFFFFF"
const val COLOR_PERIODIC_TASK_URGENCY_LONG_START    = "#FFE367"
const val COLOR_PERIODIC_TASK_URGENCY_FINAL         = "#FF0000"


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


fun castToAnyStruct(data: Any?) : GlAnyDict {
    if (data == null) {
        return mutableMapOf()
    }
    if (data !is Map< *, * >) {
        return mutableMapOf()
    }
    for (k in data.keys) {
        if (k !is String) {
            return mutableMapOf()
        }
    }
    @Suppress("UNCHECKED_CAST")
    return data as GlAnyDict
}


fun castToStrStruct(data: Any?) : GlStrStruct {
    if (data == null) {
        return mutableMapOf()
    }
    if (data !is Map< *, * >) {
        return mutableMapOf()
    }
    val strDict = mutableMapOf< String , String >()
    for ((k, v) in data) {
        // if ((k !is String) || (v !is String)) {
        //     return mutableMapOf()
        // }
        strDict[k.toString()] = v.toString()
    }
    return strDict
}


fun checkStruct(structDict: GlAnyStruct,
                structDeclare: GlStructDeclare,
                checkAll: Boolean = false) : Boolean {
    var result = true
    for ((k, v) in structDeclare) {
        if (v == Any::class) {
            // Accept all
            continue
        }
        if ((structDict[k] == null) || (structDict[k]!!::class != v)) {
            // TODO: Long type check may fail here (the digits from json are all Int). Should fix this.
            println("Structure mismatch: Field [$k], Expect [$v], But [${structDict[k]}]")
            result = false
            if (!checkAll) {
                break
            }
        }
    }
    return result
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

interface IGlObject {
    val uuid: String
}

abstract class IGlDeclare : IGlObject {
    var dataValid : Boolean = false

    companion object {
        fun toAnyStructList(dataList: List< IGlDeclare >): List< GlAnyStruct > {
            return mutableListOf< GlAnyStruct >().apply {
                for (data in dataList) {
                    this.add(data.toAnyStruct())
                }
            }
        }
    }

    abstract fun fromAnyStruct(data: Any?) : Boolean
    abstract fun toAnyStruct() : GlAnyStruct
    abstract fun copy() : IGlDeclare
}

/*val STRUCT_DEC_TASK_DATA : GlStructDeclare = mapOf< String, KClass< * > >(
    "id" to String::class,
    "name" to String::class,
    "color" to String::class
)

val STRUCT_DEC_TASK_RECORD : GlStructDeclare = mapOf< String, KClass< * > >(
    "taskID" to String::class,
    "groupID" to String::class,
    "startTime" to Long::class
)*/


// -------------------------------------------- Preset ---------------------------------------------

/*val TASK_GROUP_TOP_PRESET = mutableListOf(
    TaskData().apply { id = GROUP_ID_IDLE;      name = "放松"; color = COLOR_TASK_IDLE },
    TaskData().apply { id = GROUP_ID_ENJOY;     name = "玩乐"; color = COLOR_TASK_ENJOY },
    TaskData().apply { id = GROUP_ID_LIFE;      name = "生活"; color = COLOR_TASK_LIFE },
    TaskData().apply { id = GROUP_ID_WORK;      name = "工作"; color = COLOR_TASK_WORK },
    TaskData().apply { id = GROUP_ID_PROMOTE;   name = "提升"; color = COLOR_TASK_PROMOTE },
    TaskData().apply { id = GROUP_ID_CREATE;    name = "创作"; color = COLOR_TASK_CREATE }
)*/

val TASK_GROUP_TOP_PRESET = listOf(

    mapOf(
        "id" to GROUP_ID_IDLE,
        "name" to "放松",
        "color" to COLOR_TASK_IDLE),

    mapOf(
        "id" to GROUP_ID_ENJOY,
        "name" to "玩乐",
        "color" to COLOR_TASK_ENJOY),

    mapOf(
        "id" to GROUP_ID_LIFE,
        "name" to "生活",
        "color" to COLOR_TASK_LIFE),

    mapOf(
        "id" to GROUP_ID_WORK,
        "name" to "工作",
        "color" to COLOR_TASK_WORK),

    mapOf(
        "id" to GROUP_ID_PROMOTE,
        "name" to "提升",
        "color" to COLOR_TASK_PROMOTE),

    mapOf(
        "id" to GROUP_ID_CREATE,
        "name" to "创作",
        "color" to COLOR_TASK_CREATE),
)


val TASK_RECORD_TEMPLATE = mapOf(
    "taskID" to "",
    "groupID" to GROUP_ID_IDLE,
    "startTime" to System.currentTimeMillis()
)


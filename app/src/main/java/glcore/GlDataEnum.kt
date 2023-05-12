package glcore


// --------------------------------------------- Enum ----------------------------------------------

const val ENUM_TASK_CONCLUSION_NONE = 0
const val ENUM_TASK_CONCLUSION_DOING = 1
const val ENUM_TASK_CONCLUSION_FINISHED = 2
const val ENUM_TASK_CONCLUSION_ABANDONED = 3

val ENUM_TASK_CONCLUSION_ARRAY = listOf(
    ENUM_TASK_CONCLUSION_NONE,
    ENUM_TASK_CONCLUSION_DOING,
    ENUM_TASK_CONCLUSION_FINISHED,
    ENUM_TASK_CONCLUSION_ABANDONED
)

const val ENUM_TASK_PERIOD_ONESHOT = 0
const val ENUM_TASK_PERIOD_DAILY = 1
const val ENUM_TASK_PERIOD_WEEKLY = 7
const val ENUM_TASK_PERIOD_BI_WEEK = 14
const val ENUM_TASK_PERIOD_MONTHLY = 30
const val ENUM_TASK_PERIOD_QUARTERLY = 90

val ENUM_TASK_PERIOD_ARRAY = listOf(
    ENUM_TASK_PERIOD_ONESHOT,
    ENUM_TASK_PERIOD_DAILY,
    ENUM_TASK_PERIOD_WEEKLY,
    ENUM_TASK_PERIOD_BI_WEEK,
    ENUM_TASK_PERIOD_MONTHLY,
    ENUM_TASK_PERIOD_QUARTERLY)

const val ENUM_TIME_QUALITY_UNSPECIFIED = 0     // 不指定
const val ENUM_TIME_QUALITY_FRAGMENTED = 1      // 碎片时间
const val ENUM_TIME_QUALITY_STOLEN = 2          // 忙里偷闲的时间
const val ENUM_TIME_QUALITY_LOW = 3             // 低质量的时间段
const val ENUM_TIME_QUALITY_MEDIUM = 4          // 中等质量的时间段
const val ENUM_TIME_QUALITY_HIGH = 5            // 完余属于自己的时间段

val ENUM_TIME_QUALITY_ARRAY = listOf(
    ENUM_TIME_QUALITY_UNSPECIFIED,
    ENUM_TIME_QUALITY_FRAGMENTED,
    ENUM_TIME_QUALITY_STOLEN,
    ENUM_TIME_QUALITY_LOW,
    ENUM_TIME_QUALITY_MEDIUM,
    ENUM_TIME_QUALITY_HIGH
)

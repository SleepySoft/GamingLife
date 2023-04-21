package glcore

import kotlin.reflect.KClass

/*****************************************************************************************
 *
 * This file is generated by GlDataDeclareGen.py
 * You should update GlDataDeclare.json instead of updating this file by manual.
 *
 * ***************************************************************************************/

// -------------------------------------------------------------------------------------------------

open class TaskData : IGlDeclare() {
    var id: String = ""
    var name: String = ""
    var color: String = ""
    
    override var uuid: String = randomUUID()
    
    val structDeclare = mapOf< String, KClass< * > >(
        "id" to String::class, 
        "name" to String::class, 
        "color" to String::class
    )
    
    companion object {
        fun factory(): TaskData = TaskData()
        
        fun fromAnyStructList(anyStructList: List< * >): List< TaskData > {
            return mutableListOf< TaskData >().apply {
                for (anyStruct in anyStructList) {
                    val data = factory().apply { fromAnyStruct(anyStruct) }
                    if (data.dataValid) {
                        this.add(data)
                    }
                }
            }
        }
    }

    override fun fromAnyStruct(data: Any?): Boolean {
        val anyStruct = castToAnyStruct(data)
        dataValid = if (checkStruct(anyStruct, structDeclare)) {
            uuid = (anyStruct.get("uuid") as? String) ?: uuid
            
            id = anyStruct.get("id") as String
            name = anyStruct.get("name") as String
            color = anyStruct.get("color") as String
            true
        }
        else {
            false
        }
        return dataValid
    }

    override fun toAnyStruct(): GlAnyStruct {
        return mutableMapOf(
            "uuid" to uuid,
            
            "id" to id, 
            "name" to name, 
            "color" to color
        )
    }
}

// -------------------------------------------------------------------------------------------------

open class TaskRecord : IGlDeclare() {
    var taskID: String = ""
    var groupID: String = ""
    var startTime: Long = 0L
    
    override var uuid: String = randomUUID()
    
    val structDeclare = mapOf< String, KClass< * > >(
        "taskID" to String::class, 
        "groupID" to String::class, 
        "startTime" to Long::class
    )
    
    companion object {
        fun factory(): TaskRecord = TaskRecord()
        
        fun fromAnyStructList(anyStructList: List< * >): List< TaskRecord > {
            return mutableListOf< TaskRecord >().apply {
                for (anyStruct in anyStructList) {
                    val data = factory().apply { fromAnyStruct(anyStruct) }
                    if (data.dataValid) {
                        this.add(data)
                    }
                }
            }
        }
    }

    override fun fromAnyStruct(data: Any?): Boolean {
        val anyStruct = castToAnyStruct(data)
        dataValid = if (checkStruct(anyStruct, structDeclare)) {
            uuid = (anyStruct.get("uuid") as? String) ?: uuid
            
            taskID = anyStruct.get("taskID") as String
            groupID = anyStruct.get("groupID") as String
            startTime = anyStruct.get("startTime") as Long
            true
        }
        else {
            false
        }
        return dataValid
    }

    override fun toAnyStruct(): GlAnyStruct {
        return mutableMapOf(
            "uuid" to uuid,
            
            "taskID" to taskID, 
            "groupID" to groupID, 
            "startTime" to startTime
        )
    }
}

// -------------------------------------------------------------------------------------------------

open class PeriodicTask : IGlDeclare() {
    var name: String = ""
    var classification: String = ""
    var periodic: Int = 0
    var timeQuality: Int = 0
    var timeEstimation: Int = 0
    var batch: Int = 0
    var batchSize: Int = 0
    
    override var uuid: String = randomUUID()
    
    val structDeclare = mapOf< String, KClass< * > >(
        "name" to String::class, 
        "classification" to String::class, 
        "periodic" to Int::class, 
        "timeQuality" to Int::class, 
        "timeEstimation" to Int::class, 
        "batch" to Int::class, 
        "batchSize" to Int::class
    )
    
    companion object {
        fun factory(): PeriodicTask = PeriodicTask()
        
        fun fromAnyStructList(anyStructList: List< * >): List< PeriodicTask > {
            return mutableListOf< PeriodicTask >().apply {
                for (anyStruct in anyStructList) {
                    val data = factory().apply { fromAnyStruct(anyStruct) }
                    if (data.dataValid) {
                        this.add(data)
                    }
                }
            }
        }
    }

    override fun fromAnyStruct(data: Any?): Boolean {
        val anyStruct = castToAnyStruct(data)
        dataValid = if (checkStruct(anyStruct, structDeclare)) {
            uuid = (anyStruct.get("uuid") as? String) ?: uuid
            
            name = anyStruct.get("name") as String
            classification = anyStruct.get("classification") as String
            periodic = anyStruct.get("periodic") as Int
            timeQuality = anyStruct.get("timeQuality") as Int
            timeEstimation = anyStruct.get("timeEstimation") as Int
            batch = anyStruct.get("batch") as Int
            batchSize = anyStruct.get("batchSize") as Int
            true
        }
        else {
            false
        }
        return dataValid
    }

    override fun toAnyStruct(): GlAnyStruct {
        return mutableMapOf(
            "uuid" to uuid,
            
            "name" to name, 
            "classification" to classification, 
            "periodic" to periodic, 
            "timeQuality" to timeQuality, 
            "timeEstimation" to timeEstimation, 
            "batch" to batch, 
            "batchSize" to batchSize
        )
    }
}

// -------------------------------------------------------------------------------------------------

open class StageGoal : IGlDeclare() {
    var periodicTask: String = ""
    var goalCount: String = ""
    var continuous: Boolean = false
    
    override var uuid: String = randomUUID()
    
    val structDeclare = mapOf< String, KClass< * > >(
        "periodicTask" to String::class, 
        "goalCount" to String::class, 
        "continuous" to Boolean::class
    )
    
    companion object {
        fun factory(): StageGoal = StageGoal()
        
        fun fromAnyStructList(anyStructList: List< * >): List< StageGoal > {
            return mutableListOf< StageGoal >().apply {
                for (anyStruct in anyStructList) {
                    val data = factory().apply { fromAnyStruct(anyStruct) }
                    if (data.dataValid) {
                        this.add(data)
                    }
                }
            }
        }
    }

    override fun fromAnyStruct(data: Any?): Boolean {
        val anyStruct = castToAnyStruct(data)
        dataValid = if (checkStruct(anyStruct, structDeclare)) {
            uuid = (anyStruct.get("uuid") as? String) ?: uuid
            
            periodicTask = anyStruct.get("periodicTask") as String
            goalCount = anyStruct.get("goalCount") as String
            continuous = anyStruct.get("continuous") as Boolean
            true
        }
        else {
            false
        }
        return dataValid
    }

    override fun toAnyStruct(): GlAnyStruct {
        return mutableMapOf(
            "uuid" to uuid,
            
            "periodicTask" to periodicTask, 
            "goalCount" to goalCount, 
            "continuous" to continuous
        )
    }
}


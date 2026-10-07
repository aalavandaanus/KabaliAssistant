package com.aalavandaanus.kabaliassistant

import android.app.Application
import androidx.room.Room
import com.aalavandaanus.kabaliassistant.core.scheduling.AlarmManagerScheduler
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventHandler
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventHandlerRegistry
import com.aalavandaanus.kabaliassistant.core.scheduling.Scheduler
import com.aalavandaanus.kabaliassistant.data.alarms.AlarmRepository
import com.aalavandaanus.kabaliassistant.data.alarms.RoomAlarmRepository
import com.aalavandaanus.kabaliassistant.data.database.KabaliDatabase
import com.aalavandaanus.kabaliassistant.features.alarms.AlarmScheduledEventHandler
import com.aalavandaanus.kabaliassistant.features.alarms.AlarmService

class KabaliApplication : Application() {

    lateinit var database: KabaliDatabase
        private set

    lateinit var alarmRepository: AlarmRepository
        private set

    lateinit var scheduler: Scheduler
        private set

    lateinit var alarmService: AlarmService
        private set

    lateinit var scheduledEventHandlerRegistry:
            ScheduledEventHandlerRegistry
        private set

    override fun onCreate() {
        super.onCreate()

        // 1. Database
        database = Room.databaseBuilder(
            applicationContext,
            KabaliDatabase::class.java,
            DATABASE_NAME
        ).build()

        // 2. Repository
        alarmRepository = RoomAlarmRepository(
            alarmDao = database.alarmDao()
        )

        // 3. Core scheduler
        scheduler = AlarmManagerScheduler(
            context = applicationContext
        )

        // 4. Feature service
        alarmService = AlarmService(
            alarmRepository = alarmRepository,
            scheduler = scheduler
        )

        // 5. Scheduled-event handlers
        val handlers = setOf<ScheduledEventHandler>(
            AlarmScheduledEventHandler(
                alarmRepository = alarmRepository
            )
        )

        // 6. Handler registry
        scheduledEventHandlerRegistry =
            ScheduledEventHandlerRegistry(
                handlers = handlers
            )
    }

    companion object {
        private const val DATABASE_NAME = "kabali_assistant.db"
    }
}

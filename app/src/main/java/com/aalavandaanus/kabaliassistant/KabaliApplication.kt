
package com.aalavandaanus.kabaliassistant

import android.app.Application
import androidx.room.Room
import com.aalavandaanus.kabaliassistant.core.scheduling.AlarmManagerScheduler
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventHandler
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventHandlerRegistry
import com.aalavandaanus.kabaliassistant.core.scheduling.Scheduler
import com.aalavandaanus.kabaliassistant.data.alarms.AlarmRepository
import com.aalavandaanus.kabaliassistant.data.alarms.RoomAlarmRepository
import com.aalavandaanus.kabaliassistant.data.birthdays.BirthdayRepository
import com.aalavandaanus.kabaliassistant.data.birthdays.RoomBirthdayRepository
import com.aalavandaanus.kabaliassistant.data.database.KabaliDatabase
import com.aalavandaanus.kabaliassistant.data.reminders.ReminderRepository
import com.aalavandaanus.kabaliassistant.data.reminders.RoomReminderRepository
import com.aalavandaanus.kabaliassistant.data.settings.AppSettingsRepository
import com.aalavandaanus.kabaliassistant.data.settings.SharedPreferencesAppSettingsRepository
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleCategoryRepository
import com.aalavandaanus.kabaliassistant.data.schedules.RoomScheduleCategoryRepository
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleRepository
import com.aalavandaanus.kabaliassistant.data.schedules.RoomScheduleRepository
import com.aalavandaanus.kabaliassistant.features.alarms.AlarmScheduledEventHandler
import com.aalavandaanus.kabaliassistant.features.alarms.AlarmService
import com.aalavandaanus.kabaliassistant.features.birthdays.BirthdayService
import com.aalavandaanus.kabaliassistant.features.birthdays.BirthdayWorkScheduler
import com.aalavandaanus.kabaliassistant.features.reminders.ReminderScheduledEventHandler
import com.aalavandaanus.kabaliassistant.features.reminders.ReminderService
import com.aalavandaanus.kabaliassistant.features.schedules.ScheduleScheduledEventHandler
import com.aalavandaanus.kabaliassistant.features.schedules.ScheduleService

class KabaliApplication : Application() {

    lateinit var database: KabaliDatabase
        private set

    lateinit var alarmRepository: AlarmRepository
        private set

    lateinit var reminderRepository: ReminderRepository
        private set

    lateinit var birthdayRepository: BirthdayRepository
        private set

    lateinit var appSettingsRepository: AppSettingsRepository
        private set

    lateinit var scheduler: Scheduler
        private set

    lateinit var alarmService: AlarmService
        private set

    lateinit var reminderService: ReminderService
        private set

    lateinit var birthdayService: BirthdayService
        private set

    lateinit var scheduledEventHandlerRegistry: ScheduledEventHandlerRegistry
        private set

    lateinit var scheduleRepository: ScheduleRepository
        private set

    lateinit var scheduleCategoryRepository: ScheduleCategoryRepository
        private set

    lateinit var scheduleService: ScheduleService
        private set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            KabaliDatabase::class.java,
            DATABASE_NAME
        )
            .addMigrations(
                KabaliDatabase.MIGRATION_1_2,
                KabaliDatabase.MIGRATION_2_3,
                KabaliDatabase.MIGRATION_3_4,
                KabaliDatabase.MIGRATION_4_5
            )
            .build()

        alarmRepository = RoomAlarmRepository(
            alarmDao = database.alarmDao()
        )

        reminderRepository = RoomReminderRepository(
            reminderDao = database.reminderDao()
        )

        birthdayRepository = RoomBirthdayRepository(
            birthdayDao = database.birthdayDao()
        )

        scheduleRepository = RoomScheduleRepository(
            scheduleDao = database.scheduleDao()
        )

        scheduleCategoryRepository = RoomScheduleCategoryRepository(
            categoryDao = database.scheduleCategoryDao()
        )

        appSettingsRepository = SharedPreferencesAppSettingsRepository(
            context = applicationContext
        )

        // Initialize the shared scheduler before services that depend on it.
        scheduler = AlarmManagerScheduler(
            context = applicationContext
        )

        scheduleService = ScheduleService(
            scheduleRepository = scheduleRepository,
            scheduler = scheduler
        )

        alarmService = AlarmService(
            alarmRepository = alarmRepository,
            scheduler = scheduler
        )

        reminderService = ReminderService(
            reminderRepository = reminderRepository,
            scheduler = scheduler
        )

        birthdayService = BirthdayService(
            birthdayRepository = birthdayRepository
        )

        BirthdayWorkScheduler.ensureScheduled(
            context = applicationContext,
            hour = appSettingsRepository.getBirthdayNotificationHour(),
            minute = appSettingsRepository.getBirthdayNotificationMinute()
        )

        val handlers = setOf<ScheduledEventHandler>(
            AlarmScheduledEventHandler(
                alarmRepository = alarmRepository
            ),
            ReminderScheduledEventHandler(
                reminderRepository = reminderRepository
            ),
            ScheduleScheduledEventHandler(
                scheduleRepository = scheduleRepository
            )
        )

        scheduledEventHandlerRegistry = ScheduledEventHandlerRegistry(
            handlers = handlers
        )
    }

    companion object {
        private const val DATABASE_NAME = "kabali_assistant.db"
    }
}
package com.amrit.jaago

import android.app.Application
import com.amrit.jaago.alarm.AlarmNotifications
import com.amrit.jaago.alarm.AlarmScheduler
import com.amrit.jaago.model.AlarmStore

class JaagoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Runs before any activity, service or receiver in this process.
        AlarmStore.init(this)
        AlarmNotifications.createChannels(this)
        // Cheap insurance: if anything ever cleared the system alarm table, put it back.
        AlarmScheduler.rescheduleAll(this)
    }
}

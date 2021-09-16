package org.smartregister.fhircore.anc.utils

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import java.util.EnumSet
import java.util.concurrent.CountDownLatch

class ActivityFinisher private constructor() : Runnable {

    private val activityLifecycleMonitor: ActivityLifecycleMonitor = ActivityLifecycleMonitorRegistry.getInstance()
    private var latch: CountDownLatch? = null
    private var activities: MutableList<Activity>? = null

    constructor(latch: CountDownLatch?, activities: MutableList<Activity>?) : this() {
        this.latch = latch
        this.activities = activities
    }

    companion object {
        fun finishOpenActivities() {
            Handler(Looper.getMainLooper()).post(ActivityFinisher())
        }
    }

    override fun run() {
        val activities = this.activities ?: mutableListOf()

        for (stage in EnumSet.range(Stage.CREATED, Stage.STOPPED)) {
            activities.addAll(activityLifecycleMonitor.getActivitiesInStage(stage))
        }

        if (latch != null) {
            latch?.countDown()
        } else {
            for (activity in activities) {
                if (!activity.isFinishing) {
                    activity.finish()
                }
            }
        }
    }
}
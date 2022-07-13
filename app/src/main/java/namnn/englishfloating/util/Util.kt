package namnn.englishfloating.util

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import namnn.englishfloating.service.JobFloatingService


public class Util {
    // schedule the start of the service every 10 - 30 seconds
    companion object {
        fun scheduleJob(context: Context) {
            val serviceComponent = ComponentName(context, JobFloatingService::class.java)
            val builder = JobInfo.Builder(0, serviceComponent)
            builder.setMinimumLatency(60000)
            //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
            //builder.setRequiresDeviceIdle(true); // device should be idle
            //builder.setRequiresCharging(false); // we don't care if the device is charging or not
            val jobScheduler: JobScheduler = context.getSystemService(JobScheduler::class.java)
            jobScheduler.schedule(builder.build())
        }
    }

}
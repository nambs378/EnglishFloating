package namnn.englishfloating.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import namnn.englishfloating.Service.FloatingViewService
import namnn.englishfloating.util.Util


public class JobFloatingService : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        val service = Intent(applicationContext, FloatingViewService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            baseContext.startForegroundService(service)
        } else {
            baseContext.startService(service)
        }
        Util.scheduleJob(applicationContext) // reschedule the job

        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true;

    }
}
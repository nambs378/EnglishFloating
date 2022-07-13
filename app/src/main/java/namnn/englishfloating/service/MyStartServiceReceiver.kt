package namnn.englishfloating.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import namnn.englishfloating.util.Util

class MyStartServiceReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        Util.scheduleJob(context!!);
    }
}
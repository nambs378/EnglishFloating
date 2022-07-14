package namnn.englishfloating.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import namnn.englishfloating.util.Util

class MyStartServiceReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        Util.scheduleJob(context!!);
    }
}
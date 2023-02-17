package com.groplant.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationManagerCompat;

import com.jakewharton.threetenabp.AndroidThreeTen;
import com.groplant.DataStructures.Plant;
import com.groplant.DataStructures.PlantList;
import com.groplant.Notifications.NotificationClass;
import com.groplant.Utils.CommunicationKeys;

public class WaterPlantService extends Service {

    public WaterPlantService(){
    }

    @Override
    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        AndroidThreeTen.init(getApplicationContext());
//        android.os.Debug.waitForDebugger();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 1º Get plantList from memory
        PlantList plantList =  PlantList.getInstance(this);

        // 2º Get specific plant to water
        Plant plantToWater = intent.getParcelableExtra(CommunicationKeys.NotificationClass_WaterSinglePlantService_PlantToWater);

        // 3º Water specific plant and save(if found)
        int index = plantList.find(plantToWater);
        if (index != -1){
            plantList.waterPlant(index);
        }

        // 4º Remove notification from status bar              TODO: remove notification just on the oncreate, for better user exp
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.cancel(NotificationClass.notificationId);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

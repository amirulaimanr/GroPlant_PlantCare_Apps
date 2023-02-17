package com.groplant.Notifications;

import android.content.Context;

import com.groplant.DataStructures.Plant;
import com.groplant.R;

import java.util.ArrayList;

public class MultiplePlantsNotifBuilder extends NotifBuilder {

    private ArrayList<Plant> plantList;

    public MultiplePlantsNotifBuilder(ArrayList<Plant> plantList, Context context){
        super(context);
        this.plantList = plantList;
    }

    @Override
    protected void addSpecificFeatures() {
        builder.setContentTitle(context.getResources().getString(R.string.app_name));
        String text = plantList.size() + " " + context.getResources().getString(R.string.notification_title_several_plants);
        builder.setContentText(text);
        builder.setNumber(plantList.size());        // For badge next to icon on Android8+ devices
    }
}

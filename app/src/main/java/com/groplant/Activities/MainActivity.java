package com.groplant.Activities;

import android.app.job.JobScheduler;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jakewharton.threetenabp.AndroidThreeTen;
import com.groplant.DataStructures.PlantList;
import com.groplant.Dialogs.MiddleBottomSheetDialog;
import com.groplant.Dialogs.OnBoarding;
import com.groplant.JobSchedulers.NotificationJobService;
import com.groplant.R;
import com.groplant.RecyclerViewAdapter;
import com.groplant.Utils.CommunicationKeys;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private MiddleBottomSheetDialog middleDalog;

    private PlantList plantList;

    @Override
    public void onCreate(Bundle savedInstanceState){
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AndroidThreeTen.init(this);

        plantList = PlantList.getInstance(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new RecyclerViewAdapter(this, plantList);
        mRecyclerView.setAdapter(mAdapter);

        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);

        OnBoarding.checkOnboardingDialog(this, viewGroup);
        checkNoPlantsMessage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Check if there's a Job for notification scheduled:
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        if(jobScheduler.getAllPendingJobs().isEmpty()
                || jobScheduler.getAllPendingJobs().get(0).getId() != NotificationJobService.NOTIF_JOB_ID){
            // There isn't -> schedule it
            NotificationJobService.scheduleNextJob(this);
        }
    }

    public void onRowClicked(int position){
        Intent intent = new Intent(this, EditPlantActivity.class);
        intent.putExtra(CommunicationKeys.Main_EditPlant_ExtraPlantPosition, position);
        startActivityForResult(intent, CommunicationKeys.Main_EditPlant_RequestCode);
    }

    public void onWateringButtonClicked(int position){
        int newPos = plantList.waterPlant(position);
        if (newPos != position){
            mRecyclerView.smoothScrollToPosition(newPos);
            mAdapter.notifyItemMoved(position, newPos); // Indicate possible change of position in list (after sorting)
        }
        mAdapter.notifyItemChanged(newPos);             // Indicate change in DaysRemaining field

        checkNoPlantsMessage();
    }

    public void onNewPlantButtonClicked(View view){
        Intent intent = new Intent (this, NewPlantActivity.class);
        startActivityForResult(intent, CommunicationKeys.Main_NewPlant_RequestCode);
    }

    public void onSettingsButtonClicked(View view){
        Intent intent = new Intent (this, SettingsActivity.class);
        startActivityForResult(intent, CommunicationKeys.Main_Settings_RequestCode);
    }

    public void onMiddleButtonClicked(View view){
        if (middleDalog == null){
            middleDalog = new MiddleBottomSheetDialog(this);
        }
        middleDalog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode,resultCode,data);

        // Check which request we're responding to
        if (requestCode == CommunicationKeys.Main_NewPlant_RequestCode) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                int pos = data.getIntExtra(CommunicationKeys.NewPlant_Main_PlantPos, 0);
                mRecyclerView.smoothScrollToPosition(pos);
                mAdapter.notifyItemInserted(pos);
            }
        }
        else if(requestCode == CommunicationKeys.Main_EditPlant_RequestCode){
            if(resultCode == RESULT_OK){
                int prevPos = data.getIntExtra(CommunicationKeys.EditPlant_Main_PlantPrevPosition, 0);
                int newPos = data.getIntExtra(CommunicationKeys.EditPlant_Main_PlantNewPosition, 0);
                //mRecyclerView.smoothScrollToPosition(newPos);
                if (newPos != prevPos){
                    mAdapter.notifyItemMoved(prevPos, newPos);
                }
                mAdapter.notifyItemChanged(newPos);
            }
            else if (resultCode == CommunicationKeys.EditPlant_Main_ResultDelete){
                int prevPos = data.getIntExtra(CommunicationKeys.EditPlant_Main_PlantPrevPosition, 0);
                mRecyclerView.smoothScrollToPosition(prevPos);
                mAdapter.notifyItemRemoved(prevPos);
            }
        }
        else if(requestCode == CommunicationKeys.Main_Settings_RequestCode){
            if(resultCode == CommunicationKeys.Settings_Main_ResultDeleteAll){
                mAdapter.notifyDataSetChanged();
            }
        }

        checkNoPlantsMessage();
    }

    private void checkNoPlantsMessage(){
        // Sometimes the ViewStub is active, sometimes the View is active
        View view = (findViewById(R.id.no_plants_viewstub) != null) ?
                findViewById(R.id.no_plants_viewstub) : findViewById(R.id.no_plants_inflated);

        if(plantList.getSize() <= 0){
            view.setVisibility(View.VISIBLE);
        }
        else{
            view.setVisibility(View.GONE);
        }
    }

}

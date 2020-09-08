package com.example.selfdefence;

import android.graphics.Color;
import android.os.Bundle;

import com.hololo.tutorial.library.Step;
import com.hololo.tutorial.library.TutorialActivity;

public class HowToUse extends TutorialActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addFragment(new Step.Builder()
                .setTitle("How to use")
                .setContent("In order to ask for help, you need to add your family and friends mobile number.")
                .setSummary("Step 1")
                .setBackgroundColor(Color.parseColor("#CC0059"))
                .setDrawable(R.drawable.sc1).build());

        addFragment(new Step.Builder()
                .setTitle("How to use in trouble?")
                .setContent("To press the Volume Up button 5 times or whether shake your phone 3 times to send Sms and call")
                .setSummary("Step 2")
                .setBackgroundColor(Color.parseColor("#CC0059"))
                .setDrawable(R.drawable.sc2).build());

        addFragment(new Step.Builder()
                .setTitle("What happens after 5 seconds?")
                .setContent("SOS will be triggered resulting in sending a call, message and location to the registered mobile numbers." +
                        "\nThese operations will perform on Main home page, Trigger Page and View contact page")
                .setSummary("This is summary")
                .setBackgroundColor(Color.parseColor("#CC0059"))
                .setDrawable(R.drawable.sc3).build());
    }

    @Override
    public void currentFragmentPosition(int position) {

    }
}
package com.qrcode_quest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast toast = Toast.makeText(this, "Hello toast", Toast.LENGTH_SHORT);
        toast.show();

        initButtons();
    }

    private void initButtons() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        // replace current fragment in fragment container by TestFragment instance
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // fragment replaced by test
                fragmentManager.beginTransaction()
                        .add(R.id.frameLayout, TestFragment.class, null)
                        .setReorderingAllowed(true)
                        .addToBackStack("test")
                        .commit();
            }
        });

        // replace current fragment in fragment container by TestFragment2 instance
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // fragment replaced by test
                fragmentManager.beginTransaction()
                        .add(R.id.frameLayout, TestFragment2.class, null)
                        .setReorderingAllowed(true)
                        .addToBackStack("test2")
                        .commit();
            }
        });

        // clear back stack
        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // fragment replaced by test
                fragmentManager
                        .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }
}
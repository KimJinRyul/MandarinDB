package jrkim.mandarindb;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnMakeDB).setOnClickListener(this);
        findViewById(R.id.btnGetDB).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnMakeDB:
                Toast.makeText(this, "MAKE DB", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnGetDB:
                Toast.makeText(this, "GET DB", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}

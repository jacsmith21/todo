package ca.jacobsm.todo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private RecyclerView mTaskListView;
    private ListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTaskListView = (RecyclerView) findViewById(R.id.list);
        LinearLayoutManager mListLayoutManager = new LinearLayoutManager(getApplicationContext());
        mTaskListView.setLayoutManager(mListLayoutManager);
        mAdapter = new ListAdapter(getApplicationContext());
        mTaskListView.setAdapter(mAdapter);

        FloatingActionButton addTaskButton = (FloatingActionButton)  findViewById(R.id.action_add_task);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addTask();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.setOnItemClickListener(new
            ListAdapter.TaskItemClickListener(){
                @Override
                public void onTaskItemClick(View view, int index) {
                    Log.d(TAG,"Clicked item " + index);
                }
            });
    }

    public void addTask(){
        final EditText taskEditText = new EditText(this);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Add task")
                .setView(taskEditText)
                .setPositiveButton("Add", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        String task = String.valueOf(taskEditText.getText());
                        mAdapter.add(task);
                        Log.d(TAG, "Adding task_row to db: " + task);
                    }
                })
                .setNegativeButton("Cancel", null);
        AlertDialog dialog = alertDialogBuilder.show();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}

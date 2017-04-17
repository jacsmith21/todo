package ca.jacobsm.todo;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ca.jacobsm.todo.utilities.ItemTouchHelperCallback;

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

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(mTaskListView, mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mTaskListView);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getApplicationContext(), mListLayoutManager.getOrientation());
        mTaskListView.addItemDecoration(dividerItemDecoration);

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

    @Override
    protected void onPause() {
        mAdapter.updateDatabase();
        super.onPause();
    }

    public void addTask(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Add Task");
        alertDialogBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                Dialog d = (Dialog)dialog;
                EditText taskEditText = (EditText) d.findViewById(R.id.task_edit);
                String task = String.valueOf(taskEditText.getText());
                mAdapter.add(task);
                Toast.makeText(MainActivity.this, "Task Added",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", null);

        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.task_dialog, null);
        alertDialogBuilder.setView(dialogLayout);
        alertDialogBuilder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}

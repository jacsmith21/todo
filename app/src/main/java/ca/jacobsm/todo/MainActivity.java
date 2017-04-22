package ca.jacobsm.todo;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ca.jacobsm.todo.db.TaskContract;
import ca.jacobsm.todo.db.TaskDBHelper;
import ca.jacobsm.todo.utilities.ItemTouchHelperCallback;

public class MainActivity extends AppCompatActivity {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM");
    private static final String TAG = "MainActivity";

    private RecyclerView mItemView;
    private ItemAdapter mItemAdapter;
    private TaskDBHelper mHelper;
    private int year;
    private int month;
    private int day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Database helper
        mHelper = new TaskDBHelper(this);

        //Initializing current date
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        //Initializing data
        ArrayList<RowObject> rowObjects = new ArrayList<RowObject>();
        initData(rowObjects);

        for(RowObject rowObject : rowObjects) Log.d(TAG,rowObject.toString());

        //Creating item click listener for items
        ItemAdapter.ItemClickListener itemClickListener = new ItemAdapter.ItemClickListener(){
            @Override
            public void onItemClick(View view, int index) {
                Log.d(TAG,"Clicked item " + index);
            }
        };

        //Recycler view categories
        mItemView = (RecyclerView) findViewById(R.id.item_list);
        LinearLayoutManager mListLayoutManager = new LinearLayoutManager(getApplicationContext());
        mItemView.setLayoutManager(mListLayoutManager);
        mItemAdapter = new ItemAdapter(getApplicationContext(), rowObjects, itemClickListener);
        mItemView.setAdapter(mItemAdapter);
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(mItemView, mItemAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mItemView);

        //Floating action button
        FloatingActionButton addTaskButton = (FloatingActionButton)  findViewById(R.id.action_add_task);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addTask();
            }
        });
    }

    private void initData(ArrayList<RowObject> rowObjects) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE, TaskContract.TaskEntry.COL_TASK_DATE},
                null, null, null, null, null);

        boolean overdue = false;
        boolean update = true;
        int dayOfWeek = 0;
        TaskObject taskObject = null;
        Date date = null;
        while(true){
            CategoryObject categoryObject = getCategoryObject(dayOfWeek);
            Date categoryDate = categoryObject.getDate();

            if(update){
                if(cursor.moveToNext()) {
                    int titleIndex = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
                    int dateIndex = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_DATE);
                    update = false;
                    taskObject = new TaskObject(cursor.getString(titleIndex));
                    date = new Date((long) (cursor.getInt(dateIndex)) * 1000);
                    Log.d(TAG, "Creating object " + taskObject.getTask());
                }else{
                    break;
                }
            }

            Log.d(TAG,"Comparing: " + date.toString() + " to " + categoryDate.toString());
            if(date.compareTo(categoryDate) < 0){
                if(dayOfWeek == 0 && !overdue){
                    overdue = true;
                    Date overdueDate = getDate(-1);
                    rowObjects.add(new CategoryObject("Overdue", overdueDate));
                }
                rowObjects.add(taskObject);
                update = true;
            }else{
                rowObjects.add(categoryObject);
                dayOfWeek++;
            }
        }

        while(dayOfWeek < 7){
            CategoryObject categoryObject = getCategoryObject(dayOfWeek);
            rowObjects.add(categoryObject);
            dayOfWeek++;
        }
    }

    private CategoryObject getCategoryObject(int daysAfterToday){
        Date date = getDate(daysAfterToday);
        String title;
        if(daysAfterToday == 0) title = "Today";
        else if(daysAfterToday == 1) title = "Tomorrow";
        else title = CategoryObject.getDayString(date);
        CategoryObject categoryObject = new CategoryObject(title, date);
        return categoryObject;
    }

    private Date getDate(int daysAfterToday){
        GregorianCalendar gc = new GregorianCalendar(year, month, day);
        gc.add(Calendar.DATE, daysAfterToday);
        Date date = gc.getTime();
        return date;
    }

    @Override
    protected void onPause() {
        updateDatabase();
        super.onPause();
    }

    /* Clears database then inserts current list of items. It skips the category objects and but uses the category object dates */
    public void updateDatabase(){
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + TaskContract.TaskEntry.TABLE);
        ArrayList<RowObject> rowObjects = mItemAdapter.getRowObjects();
        Date date = null;
        for(RowObject rowObject : rowObjects){
            if(rowObject instanceof TaskObject){
                TaskObject taskObject = (TaskObject)rowObject;
                Log.d(TAG,"Inserting " + taskObject.getTask() + " @ " + date.getTime());
                ContentValues values = new ContentValues();
                values.put(TaskContract.TaskEntry.COL_TASK_TITLE, taskObject.getTask());
                int seconds = (int) (date.getTime() / 1000);
                values.put(TaskContract.TaskEntry.COL_TASK_DATE,seconds);
                db.insertWithOnConflict(TaskContract.TaskEntry.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }else{
                date = ((CategoryObject)rowObject).getDate();
            }
        }
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
                mItemAdapter.add(task);
                if(!task.equals("")) Toast.makeText(MainActivity.this, "Task Added",Toast.LENGTH_SHORT).show();
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

package ca.jacobsm.todo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import ca.jacobsm.todo.db.TaskContract;
import ca.jacobsm.todo.db.TaskDBHelper;

/**
 * Created by SDS on 4/16/2017.
 */

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
    private static String TAG = "ListAdapter";
    private LayoutInflater inflater;
    private ArrayList<RowDataObject> rowDataObjects;
    private SQLiteOpenHelper mHelper;
    private TaskItemClickListener mOnClickListener;

    public ListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        mHelper = new TaskDBHelper(context);
        initUI(context);
    }

    public void initUI(Context context) {
        ArrayList<RowDataObject> rowDataObjects = new ArrayList<RowDataObject>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                null, null, null, null, null);
        while(cursor.moveToNext()) {
            int i = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            RowDataObject rowDataObject = new RowDataObject(cursor.getString(i));
            rowDataObjects.add(rowDataObject);
        }

        this.rowDataObjects = rowDataObjects;

        cursor.close();
        db.close();
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.task_row, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        String task = rowDataObjects.get(position).getTask();
        holder.taskTextView.setText(task);
    }

    @Override
    public int getItemCount() {
        return rowDataObjects.size();
    }

    public void setOnItemClickListener(TaskItemClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }


    public void add(String task){
        int index = getItemCount();
        Log.d(TAG,"Inserting item at row " + index);

        //Creating new row object
        RowDataObject rowDataObject = new RowDataObject(task);

        //Adding to database
        SQLiteDatabase db = mHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
        db.insertWithOnConflict(TaskContract.TaskEntry.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();

        //Adding to array list and notifying adapter
        rowDataObjects.add(rowDataObject);
        notifyItemInserted(index);
    }

    public void delete(int index){
        Log.d(TAG,"Removing item at row " + index);

        if(index == -1) return; //-1 is sent when the user clicks too fast while deleting

        String task = rowDataObjects.get(index).getTask();
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE, TaskContract.TaskEntry.COL_TASK_TITLE + " = ?",new String[]{task}); //DELETE taskTable WHERE tasks = task
        db.close();

        rowDataObjects.remove(index);
        notifyItemRemoved(index);
    }

    class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        static final String TAG = "ListViewHolder";
        TextView taskTextView;
        Button deleteButton;

        public ListViewHolder(View itemView){
            super(itemView);
            taskTextView = (TextView) itemView.findViewById(R.id.task_title);
            deleteButton = (Button) itemView.findViewById(R.id.task_delete);
            itemView.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
            Log.d(TAG,"Creating view holder " + String.valueOf(taskTextView.getText()));
        }

        @Override
        public void onClick(View view){
            int index = getAdapterPosition();
            if(view.equals(deleteButton)) delete(index);
            else mOnClickListener.onTaskItemClick(view, index);
        }
    }

    public interface TaskItemClickListener {
        void onTaskItemClick(View view, int index);
    }
}

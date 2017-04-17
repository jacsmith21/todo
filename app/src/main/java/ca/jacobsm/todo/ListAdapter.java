package ca.jacobsm.todo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import ca.jacobsm.todo.db.TaskContract;
import ca.jacobsm.todo.db.TaskDBHelper;
import ca.jacobsm.todo.utilities.ItemTouchHelperAdapter;
import ca.jacobsm.todo.utilities.ItemTouchHelperViewHolder;

import static android.graphics.Color.LTGRAY;

/**
 * Created by SDS on 4/16/2017.
 */

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> implements ItemTouchHelperAdapter {
    private static String TAG = "ListAdapter";
    private LayoutInflater inflater;
    private ArrayList<RowDataObject> mRowDataObjects;
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

        this.mRowDataObjects = rowDataObjects;

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
        String task = mRowDataObjects.get(position).getTask();
        holder.taskTextView.setText(task);
    }

    @Override
    public int getItemCount() {
        return mRowDataObjects.size();
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
        mRowDataObjects.add(rowDataObject);
        notifyItemInserted(index);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mRowDataObjects, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mRowDataObjects, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        Log.d(TAG,"Moving item from " + fromPosition + " to " + toPosition);
    }

    @Override
    public void onItemDismissed(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder) {
        final int index = viewHolder.getAdapterPosition();
        final RowDataObject rowDataObject = mRowDataObjects.get(index);
        Log.d(TAG,"Removing item at row " + index);

        Snackbar snackbar = Snackbar
                .make(recyclerView, "Task Removed", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view){
                        mRowDataObjects.add(index, rowDataObject);
                        notifyItemInserted(index);
                        recyclerView.scrollToPosition(index);
                    }
                });
        snackbar.show();

        mRowDataObjects.remove(index);
        notifyItemRemoved(index);
    }

    /* Clears database then inserts current list of items */
    public void updateDatabase(){
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + TaskContract.TaskEntry.TABLE);
        for(RowDataObject rowDataObject : mRowDataObjects){
            ContentValues values = new ContentValues();
            values.put(TaskContract.TaskEntry.COL_TASK_TITLE, rowDataObject.getTask());
            db.insertWithOnConflict(TaskContract.TaskEntry.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, ItemTouchHelperViewHolder {
        static final String TAG = "ListViewHolder";
        TextView taskTextView;

        public ListViewHolder(View itemView){
            super(itemView);
            taskTextView = (TextView) itemView.findViewById(R.id.task_title);
            itemView.setOnClickListener(this);
            Log.d(TAG,"Creating view holder " + String.valueOf(taskTextView.getText()));
        }

        @Override
        public void onClick(View view){
            int index = getAdapterPosition();
            mOnClickListener.onTaskItemClick(view, index);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);

        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }

    public interface TaskItemClickListener {
        void onTaskItemClick(View view, int index);
    }
}

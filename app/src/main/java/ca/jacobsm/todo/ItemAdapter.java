package ca.jacobsm.todo;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import ca.jacobsm.todo.utilities.ItemTouchHelperAdapter;
import ca.jacobsm.todo.utilities.ItemTouchHelperViewHolder;

import static android.graphics.Color.LTGRAY;

/**
 * Created by SDS on 4/17/2017.
 */

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter {
    private final String TAG = "ItemAdapter";
    public static final int CATEGORY_OBJECT = 0;
    public static final int TASK_OBJECT = 1;

    private ArrayList<RowObject> mRowObjects;
    private ItemClickListener mItemClickListener;
    private Context mContext;
    private TextView mLastSelectedCategoryTextView;
    private int mLastSelectedCategoryPosition;
    private View mLastItemDivider;

    public ItemAdapter(Context context, ArrayList<RowObject> rowObjects, ItemClickListener itemClickListener) {
        mContext = context;
        mRowObjects = rowObjects;
        mItemClickListener = itemClickListener;
        mLastSelectedCategoryTextView = null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view;
        RecyclerView.ViewHolder holder = null;
        switch (viewType) {
            case CATEGORY_OBJECT:
                view = inflater.inflate(R.layout.category_row, parent, false);
                holder = new CategoryViewHolder(view);
                break;
            case TASK_OBJECT:
                view = inflater.inflate(R.layout.task_row, parent, false);
                holder = new TaskViewHolder(view);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType){
            case CATEGORY_OBJECT:
                //Casting objects
                CategoryViewHolder categoryViewHolder = (CategoryViewHolder)holder;
                CategoryObject categoryObject = (CategoryObject) mRowObjects.get(position);

                //Getting information from the category object
                String title = categoryObject.getTitle();
                String date = categoryObject.getDateString();

                //Binding that information to the view holder
                categoryViewHolder.titleTextView.setText(title);
                categoryViewHolder.dateTextView.setText(date);

                //Bolding category title if nothing selected or selected
                if(mLastSelectedCategoryTextView == null) categoryViewHolder.setSelected(true);
                else if(categoryViewHolder.titleTextView.equals(mLastSelectedCategoryTextView)) categoryViewHolder.setSelected(true);
                else categoryViewHolder.setSelected(false); //TODO fix bug where bold selection is gone

                //Log.d(TAG,"Binding category object: " + title);
                break;
            case TASK_OBJECT:
                //Casting objects
                TaskViewHolder taskViewHolder = (TaskViewHolder)holder;
                TaskObject taskObject = (TaskObject) mRowObjects.get(position);

                //Getting the information
                String task = taskObject.getTask();

                //Binding that information to the view holder
                taskViewHolder.taskTextView.setText(task);

                //Setting the last task invisible
                if(position==(getItemCount()-1)) setLastDivider(taskViewHolder.divider);

                //Log.d(TAG,"Binding task object: " + task);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(mRowObjects.get(position) instanceof TaskObject) return TASK_OBJECT;
        else return CATEGORY_OBJECT;
    }

    @Override
    public int getItemCount() {
        return mRowObjects.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if(toPosition != 0) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mRowObjects, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mRowObjects, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
            Log.d(TAG,"Moving item from " + fromPosition + " to " + toPosition);
        }
    }

    @Override
    public void onItemDismissed(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder) {
        final int index = viewHolder.getAdapterPosition();
        final RowObject rowObject = mRowObjects.get(index);
        Log.d(TAG,"Removing item at row " + index);

        Snackbar snackbar = Snackbar
                .make(recyclerView, "Task Removed", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view){
                        mRowObjects.add(index, rowObject);
                        notifyItemInserted(index);
                        recyclerView.scrollToPosition(index);
                    }
                });
        snackbar.show();

        mRowObjects.remove(index);
        notifyItemRemoved(index);
    }

    public void add(String task) {
        if(!task.equals("")) {
            int index = getItemCount(); //Initialize to end of list
            TaskObject taskObject = new TaskObject(task);

            boolean foundSelected = false;
            for (int i = 0; i < getItemCount(); i++) {
                RowObject rowObject = mRowObjects.get(i);
                if (rowObject instanceof CategoryObject) {
                    if (foundSelected) {
                        index = i;
                        break;
                    } else if (((CategoryObject) rowObject).isSelected()) {
                        foundSelected = true;
                    }
                }
            }

            Log.d(TAG, "Adding " + task + " to position " + index);
            mRowObjects.add(index, taskObject);
            notifyItemInserted(index);
        }
    }

    public void setLastDivider(View divider){
        if(mLastItemDivider != null){
            mLastItemDivider.setVisibility(View.VISIBLE);
        }
        mLastItemDivider = divider;
        mLastItemDivider.setVisibility(View.INVISIBLE);
    }

    public ArrayList<RowObject> getRowObjects(){ return mRowObjects; }

    public class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, ItemTouchHelperViewHolder {
        public TextView titleTextView;
        public TextView dateTextView;

        public CategoryViewHolder(View itemView){
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.category_title);
            dateTextView = (TextView) itemView.findViewById(R.id.category_date);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view){
            Log.d(TAG,"You clicked on " + String.valueOf(titleTextView.getText()));
            setSelected(true);
            int index = getAdapterPosition();
            mItemClickListener.onItemClick(view, index);
        }

        public void setSelected(boolean selected) {
            int index = getAdapterPosition();
            CategoryObject categoryObject = (CategoryObject)mRowObjects.get(index);
            if (selected) {
                Log.d(TAG, "Setting bold: " + String.valueOf(titleTextView.getText()) );
                if(mLastSelectedCategoryTextView != null){
                    CategoryObject lastCategoryObject = (CategoryObject)mRowObjects.get(mLastSelectedCategoryPosition);
                    lastCategoryObject.setSelected(false);
                    mLastSelectedCategoryTextView.setTypeface(null, Typeface.NORMAL);
                }
                mLastSelectedCategoryTextView = titleTextView;
                mLastSelectedCategoryTextView.setTypeface(null, Typeface.BOLD);
                categoryObject.setSelected(true);
                mLastSelectedCategoryPosition = index;
            } else {
                Log.d(TAG, "Setting non-bold: " + String.valueOf(titleTextView.getText()) );
                titleTextView.setTypeface(null, Typeface.NORMAL);
                categoryObject.setSelected(false);
            }
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(LTGRAY);

        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, ItemTouchHelperViewHolder {
        public TextView taskTextView;
        public View divider;

        public TaskViewHolder(View itemView){
            super(itemView);
            taskTextView = (TextView) itemView.findViewById(R.id.task_title);
            divider = (View) itemView.findViewById(R.id.divider);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view){
            Log.d(TAG,"You clicked on " + String.valueOf(taskTextView.getText()));
            int index = getAdapterPosition();
            mItemClickListener.onItemClick(view, index);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(LTGRAY);

        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }

    }

    public interface ItemClickListener {
        void onItemClick(View view, int index);
    }
}

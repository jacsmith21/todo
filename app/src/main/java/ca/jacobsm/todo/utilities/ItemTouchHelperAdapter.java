package ca.jacobsm.todo.utilities;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by SDS on 4/17/2017.
 */

public interface ItemTouchHelperAdapter {
    //Called when an item has been dragged far enough to switch positions
    void onItemMove(int fromPosition, int toPosition);

    //Called when an item has been dismissed by a swipe
    void onItemDismissed(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder);
}

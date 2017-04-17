package ca.jacobsm.todo.utilities;

/**
 * Created by SDS on 4/17/2017.
 */

public interface ItemTouchHelperViewHolder {
    //Called when moved or swiped, should indicate it is active
    void onItemSelected();

    //Called when the move or swipe is done, should return to it's normal state
    void onItemClear();
}

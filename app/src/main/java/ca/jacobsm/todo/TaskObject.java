package ca.jacobsm.todo;

/**
 * Created by SDS on 4/16/2017.
 */

public class TaskObject implements RowObject {
    private String task;

    public TaskObject(String task) {
        this.task = task;
    }

    public String getTask(){ return task; }
}

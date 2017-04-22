package ca.jacobsm.todo.db;

import android.provider.BaseColumns;

/**
 * Created by SDS on 4/16/2017.
 */

public class TaskContract {
    public static final String DB_NAME = "ca.jacobsm.todo.db";
    public static final int DB_VERSION = 1;

    public class TaskEntry implements BaseColumns {
        public static final String TABLE = "tasks";
        public static final String COL_TASK_TITLE = "title";
        public static final String COL_TASK_DATE = "date";
    }
}

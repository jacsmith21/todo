package ca.jacobsm.todo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by SDS on 4/17/2017.
 */

public class CategoryObject implements RowObject {
    private String title;
    private Date date;
    private boolean selected;

    public CategoryObject(String title, Date date) {
        this.title = title;
        this.date = date;
        selected = false;
    }

    public String getTitle(){ return title; }

    public Date getDate(){ return date; }

    public String getDateString(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM d");
        return simpleDateFormat.format(date);
    }


    public static String getDayString(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
        return simpleDateFormat.format(date);
    }

    public boolean isSelected(){ return selected; }

    public void setSelected(boolean selected){ this.selected = selected; }
}

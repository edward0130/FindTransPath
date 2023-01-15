import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static long getTimestamp(String time, String format) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (!"".equals(format)) {
            simpleDateFormat = new SimpleDateFormat(format);
        }

        long timeStamp = 0;
        Date date = null;
        try {

            date = simpleDateFormat.parse(time);

            timeStamp = date.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeStamp;
    }
}

package warrior.himanshu.impulsecloud;

import android.os.Environment;


class CheckForSDCard {
    //Method to Check If SD Card is mounted or not
    static boolean isSDCardPresent() {
        if (Environment.getExternalStorageState().equals(

                Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }
}

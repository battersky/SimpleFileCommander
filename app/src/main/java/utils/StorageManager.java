package utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by batter on 8/10/14.
 */

public class StorageManager {

    private final static String NO_EXTERNAL_STORAGE = "no external storage";
    //private final static String SECONDARY_SD_CARD_PATH = "/sdcard1/";

    private final static String SECONDARY_SD_CARD_PATH = "/storage/sdcard1";

    public static String getDisplayedAvailableExternalStorage(Context context) {
        String storageState = Environment.getExternalStorageState();
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File SDCardPoint = new File(SECONDARY_SD_CARD_PATH);
        if (SDCardPoint.exists()) {
            Log.d("Batter", "sdcard exist");
            return getDisplayedAvailableStorage(context, storageState, SECONDARY_SD_CARD_PATH);
        } else {
            return NO_EXTERNAL_STORAGE;
        }
    }

    public static String getDisplayedAvailableInternalStorage(Context context) {
        String rootPath = Environment.getDataDirectory().getAbsolutePath();;
        /*if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
            if (Environment.isExternalStorageEmulated()) {
                rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            }
        }*/
        return getDisplayedAvailableStorage(context, Environment.MEDIA_MOUNTED, rootPath);
    }

    private static String getDisplayedAvailableStorage(
            Context context, String storageState, String storagePoint) {
        if (!storageAvailable(storageState)) {
            return NO_EXTERNAL_STORAGE;
        } else {
            StatFs statFs = new StatFs(storagePoint);
            long blockSize = statFs.getBlockSize();
            return Formatter.formatFileSize(context, statFs.getAvailableBlocks() * blockSize);
        }
    }

    private static boolean storageAvailable(String storageState) {
        if (!(storageState.equals(Environment.MEDIA_MOUNTED)
                || storageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY))) {
            return false;
        }
        return true;
    }

    public static class StorageInfo {

        public final String path;
        public final boolean readonly;
        public final boolean removable;
        public final int number;

        StorageInfo(String path, boolean readonly, boolean removable, int number) {
            this.path = path;
            this.readonly = readonly;
            this.removable = removable;
            this.number = number;
        }

        public String getDisplayName() {
            StringBuilder res = new StringBuilder();
            if (!removable) {
                res.append("Internal SD card");
            } else if (number > 1) {
                res.append("SD card " + number);
            } else {
                res.append("SD card");
            }
            if (readonly) {
                res.append(" (Read only)");
            }

            res.append(" path: " + path);
            return res.toString();
        }
    }

    public static List<StorageInfo> getStorageList() {

        List<StorageInfo> list = new ArrayList<StorageInfo>();
        String def_path = Environment.getExternalStorageDirectory().getPath();
        boolean def_path_removable = Environment.isExternalStorageRemovable();
        String def_path_state = Environment.getExternalStorageState();
        boolean def_path_available = def_path_state.equals(Environment.MEDIA_MOUNTED)
                || def_path_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        boolean def_path_readonly = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);

        HashSet<String> paths = new HashSet<String>();
        int cur_removable_number = 1;

        if (def_path_available) {
            paths.add(def_path);
            list.add(0, new StorageInfo(def_path, def_path_readonly, def_path_removable, def_path_removable ? cur_removable_number++ : -1));
        }

        BufferedReader buf_reader = null;
        try {
            buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
            Log.d("Batter", "/proc/mounts");
            while ((line = buf_reader.readLine()) != null) {
                Log.d("Batter", line);
                if (line.contains("vfat") || line.contains("/mnt")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String unused = tokens.nextToken(); //device
                    String mount_point = tokens.nextToken(); //mount point
                    if (paths.contains(mount_point)) {
                        continue;
                    }
                    unused = tokens.nextToken(); //file system
                    List<String> flags = Arrays.asList(tokens.nextToken().split(",")); //flags
                    boolean readonly = flags.contains("ro");

                    if (line.contains("/dev/block/vold")) {
                        if (!line.contains("/mnt/secure")
                                && !line.contains("/mnt/asec")
                                && !line.contains("/mnt/obb")
                                && !line.contains("/dev/mapper")
                                && !line.contains("tmpfs")) {
                            paths.add(mount_point);
                            list.add(new StorageInfo(mount_point, readonly, true, cur_removable_number++));
                        }
                    }
                }
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }  finally {
            if (buf_reader != null) {
                try {
                    buf_reader.close();
                } catch (IOException ex) {}
            }
        }
        return list;
    }
}

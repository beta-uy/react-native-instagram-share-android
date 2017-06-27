package bhumi.customInstagramShare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.File;

public class CustomInstagramShareModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private Activity mActivity;
    private ReactApplicationContext reactContext;
    private Callback callback;

    final int INSTAGRAM_SHARE_REQUEST = 500;


    public CustomInstagramShareModule(ReactApplicationContext reactContext, Activity activity) {
        super(reactContext);
        this.mActivity = activity;
        this.reactContext = reactContext;
        this.reactContext.addActivityEventListener(new RNInstagramShareActivityEventListener());
    }

    private class RNInstagramShareActivityEventListener extends BaseActivityEventListener {
        @Override
        public void onActivityResult(Activity activity, final int requestCode, final int resultCode, final Intent intent) {
            Log.d("------------>resultCode", "" + resultCode);
            if (requestCode == INSTAGRAM_SHARE_REQUEST) {
                callback.invoke("Image shared successfully with instagram.");
            }
        }
    }

    @Override
    public String getName() {
        return "CustomInstagramShare";
    }

    @ReactMethod
    public void shareWithInstagram(String mediaPath , Callback callback) {
       this.callback = callback;

       String type = "image/*";
       String filename = mediaPath.substring(mediaPath.lastIndexOf("/")+1);
        try {
            if (isAppInstalled("com.instagram.android") == false) {
                callback.invoke("Sorry,instagram is not installed in your device.");
            } else {
                Uri mediaUri = Uri.parse(mediaPath);
                String realPath = getRealPathFromUri(reactContext, mediaUri);
                File media = new File(realPath);
                if (media.exists()) {
                    // Create the new Intent using the 'Send' action.
                    Intent share = new Intent(Intent.ACTION_SEND);

                    // Set the MIME type
                    share.setType(type);
                    share.setPackage("com.instagram.android");

                    //Create the URI from the media
                    Uri uri = Uri.fromFile(media);

                    // Add the URI to the Intent.
                    share.putExtra(Intent.EXTRA_STREAM, uri);

                    // Broadcast the Intent.
                    mActivity.startActivityForResult(Intent.createChooser(share, "Share to"), INSTAGRAM_SHARE_REQUEST);
                } else {
                    callback.invoke("Sorry,file does not exist on given path.");
                }
            }
        } catch (Exception e) {
            callback.invoke("Sorry, there was an error. Try again");
        }
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = mActivity.getPackageManager();
        boolean installed = false;
        try {
           pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
           installed = true;
        } catch (PackageManager.NameNotFoundException e) {
           installed = false;
        }
        return installed;
    }

}

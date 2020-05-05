package pl.aprilapps.easyphotopicker

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.preference.PreferenceManager
import android.webkit.MimeTypeMap
import java.io.*
import java.util.*

/**
 * Created by Jacek Kwiecień on 14.12.15.
 */
object EasyImageFiles {

    var DEFAULT_FOLDER_NAME = "SLIDE"
    var TEMP_FOLDER_NAME = "Temp"

    private val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }


    fun getFolderName(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(BundleKeys.FOLDER_NAME, DEFAULT_FOLDER_NAME)
    }

    fun tempImageDirectory(context: Context): File {
        val publicTemp =
            PreferenceManager.getDefaultSharedPreferences(context).getBoolean(BundleKeys.PUBLIC_TEMP, false)
        val dir = if (publicTemp) publicTempDir(context) else privateTempDir(context)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun publicRootDir(context: Context): File {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    }

    //    public static File publicRootPicturesDir(Context context) {
    //        File dir = new File(publicRootDir(context), getFolderName(context));
    //        if (!dir.exists()) dir.mkdirs();
    //        return dir;
    //    }

    fun publicAppExternalDir(context: Context): File? {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    }

    fun publicTempDir(context: Context): File {
        val cameraPicturesDir = File(EasyImageFiles.getFolderLocation(context), EasyImageFiles.getFolderName(context))
        val publicTempDir = File(cameraPicturesDir, TEMP_FOLDER_NAME)
        if (!publicTempDir.exists()) publicTempDir.mkdirs()
        return publicTempDir
    }

    private fun privateTempDir(context: Context): File {
        val privateTempDir = File(context.applicationContext.cacheDir, getFolderName(context))
        if (!privateTempDir.exists()) privateTempDir.mkdirs()
        return privateTempDir
    }

    //    public static File publicAppExternalFilesDir(Context context) {
    //        File dir = new File(publicAppExternalDir(context), getFolderName(context));
    //        if (!dir.exists()) dir.mkdirs();
    //        return dir;
    //    }

    fun writeToFile(`in`: InputStream?, file: File) {
        try {

            val out = FileOutputStream(file)
            `in`.use { input ->
                out.use { fileOut ->
                    input?.copyTo(fileOut)
                }
            }
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    fun pickedExistingPicture(context: Context, photoUri: Uri): File {
        val pictureInputStream = context.contentResolver.openInputStream(photoUri)
        val directory = tempImageDirectory(context)
        //        File photoFile = new File(directory, UUID.randomUUID().toString() + "." + getMimeType(context, photoUri));
        val photoFile = File(directory, UUID.randomUUID().toString() + "." + "png")
        photoFile.createNewFile()
        writeToFile(pictureInputStream, photoFile)
        return photoFile
    }


    @Throws(Exception::class)
    fun saveBitmapToFile(context: Context, bitmap: Bitmap): File {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
        val bitmapdata = bos.toByteArray()
        val pictureInputStream = ByteArrayInputStream(bitmapdata)

        //        File directory = tempImageDirectory(context);
        val directory = publicTempDir(context)

        //        File photoFile = new File(directory, UUID.randomUUID().toString() + "." + getMimeType(context, photoUri));
        val photoFile = File(directory, UUID.randomUUID().toString() + "." + "png")
        photoFile.createNewFile()
        writeToFile(pictureInputStream, photoFile)
        return photoFile
    }

    /**
     * Default folder location will be inside app public directory. That way write permissions after SDK 18 aren't required and contents are deleted if app is uninstalled.
     *
     * @param context context
     */
    fun getFolderLocation(context: Context): String? {
        val publicAppExternalDir = publicAppExternalDir(context)
        var defaultFolderLocation: String? = null
        if (publicAppExternalDir != null) {
            defaultFolderLocation = publicAppExternalDir.path
        }
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(BundleKeys.FOLDER_LOCATION, defaultFolderLocation)
    }

    @Throws(IOException::class)
    fun getCameraPicturesLocation(context: Context): File {

        var cacheDir: File? = context.cacheDir

        if (isExternalStorageWritable) {
            cacheDir = context.externalCacheDir
        }

        val dir = File(cacheDir, DEFAULT_FOLDER_NAME)
        if (!dir.exists()) dir.mkdirs()
        return File.createTempFile(UUID.randomUUID().toString(), ".png", dir)
    }

    /**
     * To find out the extension of required object in given uri
     * Solution by http://stackoverflow.com/a/36514823/1171484
     */
    fun getMimeType(context: Context, uri: Uri): String? {
        val extension: String?

        //Check uri format to avoid null
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            //If scheme is a content
            val mime = MimeTypeMap.getSingleton()
            extension = mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())

        }

        return extension
    }

}

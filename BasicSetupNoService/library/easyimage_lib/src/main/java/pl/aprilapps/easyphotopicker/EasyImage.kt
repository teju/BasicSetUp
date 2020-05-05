package pl.aprilapps.easyphotopicker

import android.app.Activity
import android.app.Fragment
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.text.TextUtils
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import pl.aprilapps.easyphotopicker.EasyImageConfig.Companion.REQ_PICK_PICTURE_FROM_DOCUMENTS
import pl.aprilapps.easyphotopicker.EasyImageConfig.Companion.REQ_PICK_PICTURE_FROM_GALLERY
import pl.aprilapps.easyphotopicker.EasyImageConfig.Companion.REQ_SOURCE_CHOOSER
import pl.aprilapps.easyphotopicker.EasyImageConfig.Companion.REQ_TAKE_PICTURE
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.util.*


/**
 * Created by Jacek Kwiecień on 16.10.2015.
 */
class EasyImage : EasyImageConfig {

    enum class ImageSource {
        GALLERY, DOCUMENTS, CAMERA
    }

    interface Callbacks {
        fun onImagePickerError(e: Exception, source: ImageSource, type: Int)

        fun onImagePicked(imageFile: File, source: ImageSource, type: Int)

        fun onCanceled(source: ImageSource, type: Int)
    }

    class Configuration(private val context: Context) {

        fun setImagesFolderName(folderName: String): Configuration {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(BundleKeys.FOLDER_NAME, folderName)
                .commit()
            return this
        }

        fun saveInRootPicturesDirectory(): Configuration {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(BundleKeys.FOLDER_LOCATION, EasyImageFiles.publicRootDir(context).toString())
                .commit()
            return this
        }

        fun saveInAppExternalFilesDir(): Configuration {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(BundleKeys.FOLDER_LOCATION, EasyImageFiles.publicAppExternalDir(context)!!.toString())
                .commit()
            return this
        }


        /**
         * Use this method if you want your picked gallery or documents pictures to be duplicated into public, other apps accessible, directory.
         * You'll have to take care of removing that file on your own after you're done with it. Use EasyImage.clearPublicTemp() method for that.
         * If you don't delete them they could show up in user galleries.
         *
         * @return modified Configuration object
         */
        fun setCopyExistingPicturesToPublicLocation(copyToPublicLocation: Boolean): Configuration {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(BundleKeys.PUBLIC_TEMP, copyToPublicLocation)
                .commit()
            return this
        }
    }

    companion object {

        private val SHOW_GALLERY_IN_CHOOSER = false

        private val KEY_PHOTO_URI = "pl.aprilapps.easyphotopicker.photo_uri"
        private val KEY_LAST_CAMERA_PHOTO = "pl.aprilapps.easyphotopicker.last_photo"
        private val KEY_TYPE = "pl.aprilapps.easyphotopicker.type"

        @Throws(IOException::class)
        private fun createCameraPictureFile(context: Context): Uri {
            val imagePath = EasyImageFiles.getCameraPicturesLocation(context)
            val packageName = context.applicationContext.packageName
            //        String authority = packageName + ".easyphotopicker.fileprovider";
            val uri = FileProvider.getUriForFile(context, packageName, imagePath)
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putString(KEY_PHOTO_URI, uri.toString())
            editor.putString(KEY_LAST_CAMERA_PHOTO, imagePath.toString())
            editor.apply()
            return uri
        }

        private fun createDocumentsIntent(context: Context, type: Int): Intent {
            storeType(context, type)
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            return intent
        }

        private fun createGalleryIntent(context: Context, type: Int): Intent {
            storeType(context, type)
            return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        }

        private fun createCameraIntent(context: Context, type: Int): Intent {
            storeType(context, type)

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                val capturedImageUri = createCameraPictureFile(context)
                //We have to explicitly grant the write permission since Intent.setFlag works only on API Level >=20
                //grantWritePermission(context, intent, capturedImageUri)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return intent
        }

        private fun revokeWritePermission(context: Context, uri: Uri) {
            context.revokeUriPermission(
                uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        private fun grantWritePermission(context: Context, intent: Intent, uri: Uri) {
            val resInfoList = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(
                    packageName,
                    uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }

        @Throws(IOException::class)
        private fun createChooserIntent(context: Context, chooserTitle: String, type: Int): Intent {
            return createChooserIntent(context, chooserTitle, SHOW_GALLERY_IN_CHOOSER, type)
        }

        @Throws(IOException::class)
        private fun createChooserIntent(
            context: Context,
            chooserTitle: String,
            showGallery: Boolean,
            type: Int
        ): Intent {
            storeType(context, type)

            val outputFileUri = createCameraPictureFile(context)
            val cameraIntents = ArrayList<Intent>()
            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val packageManager = context.packageManager
            val camList = packageManager.queryIntentActivities(captureIntent, 0)
            for (res in camList) {
                val packageName = res.activityInfo.packageName
                val intent = Intent(captureIntent)
                intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                intent.setPackage(packageName)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
                cameraIntents.add(intent)
            }
            val galleryIntent: Intent

            if (showGallery) {
                galleryIntent = createGalleryIntent(context, type)
            } else {
                galleryIntent = createDocumentsIntent(context, type)
            }

            val chooserIntent = Intent.createChooser(galleryIntent, chooserTitle)
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toTypedArray<Parcelable>())

            return chooserIntent
        }

        private fun storeType(context: Context, type: Int) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_TYPE, type).commit()
        }

        private fun restoreType(context: Context?): Int {
            return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_TYPE, 0)
        }

        fun openChooserWithDocuments(activity: Activity, chooserTitle: String, type: Int) {
            try {
                val intent = createChooserIntent(activity, chooserTitle, type)
                activity.startActivityForResult(intent, REQ_SOURCE_CHOOSER)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        fun openChooserWithDocuments(fragment: Fragment, chooserTitle: String, type: Int) {
            try {
                val intent = createChooserIntent(fragment.getActivity(), chooserTitle, type)
                fragment.startActivityForResult(intent, REQ_SOURCE_CHOOSER)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }



        fun openChooserWithGallery(activity: Activity, chooserTitle: String, type: Int) {
            try {
                val intent = createChooserIntent(activity, chooserTitle, true, type)
                activity.startActivityForResult(intent, REQ_SOURCE_CHOOSER)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        fun openChooserWithGallery(fragment: Fragment, chooserTitle: String, type: Int) {
            try {
                val intent = createChooserIntent(fragment.getActivity(), chooserTitle, true, type)
                fragment.startActivityForResult(intent, REQ_SOURCE_CHOOSER)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }


        fun openDocuments(activity: Activity, type: Int) {
            val intent = createDocumentsIntent(activity, type)
            activity.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_DOCUMENTS)
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun openDocuments(fragment: Fragment, type: Int) {
            val intent = createDocumentsIntent(fragment.context, type)
            fragment.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_DOCUMENTS)
        }



        fun openGallery(activity: Activity, type: Int) {
            val intent = createGalleryIntent(activity, type)
            activity.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_GALLERY)
        }


        fun openGallery(activity: FragmentActivity, type: Int) {
            val intent = createGalleryIntent(activity, type)
            activity.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_GALLERY)
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun openGallery(fragment: Fragment, type: Int) {
            val intent = createGalleryIntent(fragment.context, type)
            fragment.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_GALLERY)
        }



        fun openCamera(activity: Activity, type: Int) {
            val intent = createCameraIntent(activity, type)
            activity.startActivityForResult(intent, REQ_TAKE_PICTURE)
        }


        fun openCamera(fragment: Fragment, type: Int) {
            val intent = createCameraIntent(fragment.activity, type)
            fragment.startActivityForResult(intent, REQ_TAKE_PICTURE)
        }

        fun openCamera(fragment: FragmentActivity, type: Int) {
            val intent = createCameraIntent(fragment, type)
            fragment.startActivityForResult(intent, REQ_TAKE_PICTURE)
        }

        @Nullable
        @Throws(IOException::class, URISyntaxException::class)
        private fun takenCameraPicture(context: Context): File? {
            val lastCameraPhoto =
                PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_LAST_CAMERA_PHOTO, null)
            return if (lastCameraPhoto != null) {
                File(lastCameraPhoto)
            } else {
                null
            }
        }

        fun handleActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?,
            activity: Activity,
            callbacks: Callbacks
        ) {

            if (requestCode == EasyImageConfig.REQ_SOURCE_CHOOSER || requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_GALLERY || requestCode == EasyImageConfig.REQ_TAKE_PICTURE || requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_DOCUMENTS) {

                if (resultCode == Activity.RESULT_OK) {
                    if (requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_DOCUMENTS) {
                        onPictureReturnedFromDocuments(data, activity, callbacks)
                    } else if (requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_GALLERY) {
                        onPictureReturnedFromGallery(data, activity, callbacks)
                    } else if (requestCode == EasyImageConfig.REQ_TAKE_PICTURE) {
                        onPictureReturnedFromCamera(activity, callbacks)
                    } else if (data == null || data.data == null) {
                        onPictureReturnedFromCamera(activity, callbacks)
                    } else {
                        onPictureReturnedFromDocuments(data, activity, callbacks)
                    }
                } else {
                    if (requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_DOCUMENTS) {
                        callbacks.onCanceled(ImageSource.DOCUMENTS, restoreType(activity))
                    } else if (requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_GALLERY) {
                        callbacks.onCanceled(ImageSource.GALLERY, restoreType(activity))
                    } else if (requestCode == EasyImageConfig.REQ_TAKE_PICTURE) {
                        callbacks.onCanceled(ImageSource.CAMERA, restoreType(activity))
                    } else if (data == null || data.data == null) {
                        callbacks.onCanceled(ImageSource.CAMERA, restoreType(activity))
                    } else {
                        callbacks.onCanceled(ImageSource.DOCUMENTS, restoreType(activity))
                    }
                }
            }
        }

        fun willHandleActivityResult(requestCode: Int, resultCode: Int, data: Intent): Boolean {
            return if (requestCode == EasyImageConfig.REQ_SOURCE_CHOOSER || requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_GALLERY || requestCode == EasyImageConfig.REQ_TAKE_PICTURE || requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_DOCUMENTS) {
                true
            } else false
        }

        /**
         * @param context context
         * @return File containing lastly taken (using camera) photo. Returns null if there was no photo taken or it doesn't exist anymore.
         */
        fun lastlyTakenButCanceledPhoto(context: Context): File? {
            val filePath = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_LAST_CAMERA_PHOTO, null)
                ?: return null
            val file = File(filePath)
            return if (file.exists()) {
                file
            } else {
                null
            }
        }

        private fun onPictureReturnedFromDocuments(data: Intent?, activity: Activity, callbacks: Callbacks) {
            try {
                val photoPath = data!!.data
                val photoFile = EasyImageFiles.pickedExistingPicture(activity, photoPath!!)
                callbacks.onImagePicked(photoFile!!, ImageSource.DOCUMENTS, restoreType(activity))
            } catch (e: Exception) {
                e.printStackTrace()
                callbacks.onImagePickerError(e, ImageSource.DOCUMENTS, restoreType(activity))
            }

        }

        private fun onPictureReturnedFromGallery(data: Intent?, activity: Activity, callbacks: Callbacks) {
            try {
                val photoPath = data!!.data

                val photoFile = EasyImageFiles.pickedExistingPicture(activity, photoPath!!)
                callbacks.onImagePicked(photoFile, ImageSource.GALLERY, restoreType(activity))
            } catch (e: Exception) {
                e.printStackTrace()
                callbacks.onImagePickerError(e, ImageSource.GALLERY, restoreType(activity))

            }

        }

        private fun onPictureReturnedFromCamera(activity: Activity, callbacks: Callbacks) {
            try {

                val lastImageUri =
                    PreferenceManager.getDefaultSharedPreferences(activity).getString(KEY_PHOTO_URI, null)
                if (!TextUtils.isEmpty(lastImageUri)) {
                    revokeWritePermission(activity, Uri.parse(lastImageUri))
                }

                val photoFile = EasyImage.takenCameraPicture(activity)

                if (photoFile == null) {
                    val e = IllegalStateException("Unable to get the picture returned from camera")
                    callbacks.onImagePickerError(e, ImageSource.CAMERA, restoreType(activity))
                } else {
                    callbacks.onImagePicked(photoFile, ImageSource.CAMERA, restoreType(activity))
                }

                PreferenceManager.getDefaultSharedPreferences(activity)
                    .edit()
                    .remove(KEY_LAST_CAMERA_PHOTO)
                    .remove(KEY_PHOTO_URI)
                    .apply()
            } catch (e: Exception) {
                e.printStackTrace()
                callbacks.onImagePickerError(e, ImageSource.CAMERA, restoreType(activity))
            }

        }

        fun clearPublicTemp(context: Context) {
            val tempFiles = ArrayList<File>()
            val files = EasyImageFiles.publicTempDir(context).listFiles()
            for (file in files) {
                file.delete()
            }
        }


        /**
         * Method to clear configuration. Would likely be used in onDestroy(), onDestroyView()...
         *
         * @param context context
         */
        fun clearConfiguration(context: Context) {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .remove(BundleKeys.FOLDER_NAME)
                .remove(BundleKeys.FOLDER_LOCATION)
                .remove(BundleKeys.PUBLIC_TEMP)
                .apply()
        }

        fun configuration(context: Context): Configuration {
            return Configuration(context)
        }
    }
}
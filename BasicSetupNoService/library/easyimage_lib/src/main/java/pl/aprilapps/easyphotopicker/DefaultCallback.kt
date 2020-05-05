package pl.aprilapps.easyphotopicker

/**
 * Stas Parshin
 * 05 November 2015
 */
abstract class DefaultCallback : EasyImage.Callbacks {

    override fun onImagePickerError(e: Exception, source: EasyImage.ImageSource, type: Int) {}

    override fun onCanceled(source: EasyImage.ImageSource, type: Int) {}
}

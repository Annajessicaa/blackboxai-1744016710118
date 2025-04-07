import android.content.Context
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.client.http.ByteArrayContent

object DriveHelper {
    fun uploadFile(context: Context, fileBytes: ByteArray, fileName: String) {
        val driveService = GoogleDriveServiceFactory.buildDriveService(context)
        val fileMetadata = File()
            .setParents(listOf("root"))
            .setName(fileName)

        val mediaContent = ByteArrayContent("application/octet-stream", fileBytes)
        driveService.files().create(fileMetadata, mediaContent)
            .setFields("id")
            .execute()
    }
}
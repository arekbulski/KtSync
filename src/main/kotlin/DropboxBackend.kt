import com.beust.klaxon.Klaxon
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.GetMetadataErrorException
import com.dropbox.core.v2.files.WriteMode
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.pathString

// This class exposes Dropbox API operations in a somewhat agnostic way.
class DropboxBackend (subprocessor: Processor) : LocalDiskBackend(subprocessor) {

    var accessToken = ""
    var apiConfig = DbxRequestConfig("")
    var apiClient = DbxClientV2(apiConfig, "")

//----------------------------------------------------------------------------------------------------------------------

    override fun backupProcess(process: ProcessingJob) {
        try {
            accessToken = process.profile!!.accessToken!!
            apiConfig = DbxRequestConfig("KtSync/WIP")
            apiClient = DbxClientV2(apiConfig, accessToken)

            // Check connectivity, if access token is valid, and if KtSync folder is there.
            // TODO: Extract /A/KS from the destination path.
            apiClient.files().getMetadata("/Aplikacje/KtSync")
        } catch (e: Exception) {
            throw TotalFailureException("Failed to connect with Dropbox, or access token is invalid, or the KtSync folder does not exist.", this, e)
        }
    }

//----------------------------------------------------------------------------------------------------------------------

    // TODO: Note that Dropbox does not support case-sensitive filenames, and therefore will simply file if we leave it like this. The other way would be to hexlify names. Or to encrypt filenames.
    override fun encodeNameRemote (name: String): String {
        return name
    }

    // Note that both files (main and .metadata) must be uploaded for the pathname to formally exist.
    override fun existsRemote(pathname: String): Boolean {
        try {
            apiClient.files().getMetadata(pathname)
            val metadataPath = pathname + ".metadata"
            apiClient.files().getMetadata(metadataPath)
            return true
        } catch (e: GetMetadataErrorException) {
            if (e.errorValue.pathValue.isNotFound)
                return false
            throw TotalFailureException("Dropbox API failed.", this, e)
        } catch (e: Exception) {
            throw TotalFailureException("Dropbox API failed.", this, e)
        }
    }

    override fun renameToRemote(pathname: String, newname: String) {
        try {
            apiClient.files().moveV2(pathname, newname)
            val metadataPathname = pathname + ".metadata"
            val metadataNewname = newname + ".metadata"
            apiClient.files().moveV2(metadataPathname, metadataNewname)
        } catch (e: Exception) {
            throw TotalFailureException("Dropbox API failed.", this, e)
        }
    }

    override fun createFolderRemote(pathname: String) {
        try {
            apiClient.files().createFolderV2(pathname, false)
        } catch (e: Exception) {
            throw TotalFailureException("Dropbox API failed.", this, e)
        }
    }

    override fun getMetadataRemote(pathname: String): MetadataStruct {
        try {
            val metadataPath = pathname + ".metadata"
            val metadataStream = apiClient.files().download(metadataPath).inputStream
            val metadataObj = Klaxon().parse<MetadataStruct>(metadataStream)!!
            return metadataObj
        } catch (e: Exception) {
            throw TotalFailureException("Dropbox API failed.", this, e)
        }
    }

    override fun setMetadataRemote(pathname: String, metadata: MetadataStruct) {
        try {
            val metadataPath = pathname + ".metadata"
            val metadataJson = Klaxon().toJsonString(metadata)
            val metadataBinary = metadataJson.toByteArray(Charsets.UTF_8)
            apiClient.files().uploadBuilder(metadataPath)
                .withMode(WriteMode.ADD)
                .withAutorename(false)
                .withMute(true)
                .uploadAndFinish(ByteArrayInputStream(metadataBinary))
        } catch (e: Exception) {
            throw TotalFailureException("Dropbox API failed.", this, e)
        }
    }

    override fun getFileSizeRemote(pathname: String): Long {
        return getMetadataRemote(pathname).size
    }

    override fun getModificationTimeRemote(pathname: String): FileTime {
        throw NotImplementedError()
    }

    override fun setModificationTimeRemote(pathname: String, mtime: FileTime) {
        throw NotImplementedError()
    }

    override fun getPosixPermissionsRemote(pathname: String): Set<PosixFilePermission> {
        throw NotImplementedError()
    }

    override fun setPosixPermissionsRemote(pathname: String, permissions: Set<PosixFilePermission>) {
        throw NotImplementedError()
    }

    override fun copyFileProgressivelyRemote(sourcePath: String, destinationPath: String, onUpdate: ((Long) -> Unit)?, onSuccess: (() -> Unit)?, onFailure: ((Exception) -> Unit)?) {
        try {

            FileInputStream(File(sourcePath)).use { inputStream ->
                apiClient.files().uploadBuilder(destinationPath)
                    .withMode(WriteMode.ADD)
                    .withAutorename(false)
                    .withMute(true)
                    .uploadAndFinish(inputStream) { onUpdate?.invoke(it) }
            }

            propagateCombined({
                val metadata = this.getMetadataLocal(sourcePath)
                this.setMetadataRemote(destinationPath, metadata)
            }, null, {
                throw PartialFailureException("Could not get/set mtime/permissions from file $sourcePath to file $destinationPath.", this, it)
            })

            onSuccess?.invoke()
        } catch (e: PartialFailureException) {
            onFailure?.invoke(e)
        } catch (e: Exception) {
            onFailure?.invoke(TotalFailureException("Dropbox API failed.", this, e))
        }
    }

    override fun cloneFileRemote(sourcePath: String, destinationPath: String) {
        try {
            apiClient.files().copyV2Builder(sourcePath, destinationPath)
                .withAutorename(false)
                .start()

            val sourceMetadataPath = sourcePath + ".metadata"
            val destinationMetadataPath = destinationPath + ".metadata"
            apiClient.files().copyV2Builder(sourceMetadataPath, destinationMetadataPath)
                .withAutorename(false)
                .start()

        } catch (e: Exception) {
            throw TotalFailureException("Dropbox API failed, could not rename from $sourcePath to $destinationPath.", this, e)
        }
    }

    override fun copySymbolicLinkRemote(sourcePath: String, destinationPath: String) {
        try {
            val target = Files.readSymbolicLink(File(sourcePath).toPath()).pathString
            val targetBytes = target.toByteArray(Charsets.UTF_8)
            apiClient.files().uploadBuilder(destinationPath)
                .withMode(WriteMode.ADD)
                .withAutorename(false)
                .withMute(true)
                .uploadAndFinish(ByteArrayInputStream(targetBytes))

            val metadata = MetadataStruct().apply {
                size = targetBytes.size.toLong()
                isSymbolicLink = true
            }
            this.setMetadataRemote(destinationPath, metadata)

        } catch (e: Exception) {
            throw TotalFailureException("Dropbox API failed, could not rename from $sourcePath to $destinationPath.", this, e)
        }
    }

//----------------------------------------------------------------------------------------------------------------------

}
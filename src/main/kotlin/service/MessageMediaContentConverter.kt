package com.example.service

import io.ktor.util.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File

class IdGenerator(
    private val messageService: MessageService,
    val BATCH_SIZE: Int = 50
) {
    private var currentMaxId = 0L
    private val mutex = Mutex()

    suspend fun initialize() {
        currentMaxId = messageService.findIdLastMessage() ?: 0L
    }

    suspend fun getNextId(): Long = mutex.withLock {
            if (currentMaxId % BATCH_SIZE == 0L) {
                currentMaxId = messageService.findIdLastMessage() ?: 0L
            }
            ++currentMaxId

    }
}

class MediaConverter(
    private val idGenerator: IdGenerator,
    private val DIRECTORY_PATH:String
){


    private val logger  = LoggerFactory.getLogger("MediaConverter")

    init {
        File(DIRECTORY_PATH).takeIf { !it.exists() }?.mkdir()
    }

    suspend fun saveMediaToFile(mediaContent: ByteArray):String?
    {

        try {
            val idMessage  = idGenerator.getNextId()
            val formattedId = "media_%04d".format(idMessage)
            val fileMedia = File(DIRECTORY_PATH,formattedId).apply {
                this.writeBytes(mediaContent)
            }
            return fileMedia.path
        }
        catch (e: FileSystemException)
        {
            logger.error("[ERROR] Error while working with files." +
                    "Message error: ${e.message}")
        }
        return null
    }

    fun checkMediaContent(path:String): String?{

        try {
            val media = File(DIRECTORY_PATH,path)
            if (media.exists() && media.isFile)
            {
                val mediaArray  =  media.readBytes()
                return mediaArray.encodeBase64()
            }
        }
        catch (e: FileSystemException){
            logger.error("[ERROR] Error while checking file." +
                    "Message error: ${e.message}")
        }
        return null
    }
}




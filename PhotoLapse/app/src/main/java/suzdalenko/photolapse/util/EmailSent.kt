package suzdalenko.photolapse.util
import android.util.Log
import java.io.File
import java.util.Properties
import java.util.concurrent.Executors
import javax.activation.DataHandler
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class EmailSent {
    companion object {
        fun enviarCorreoAutomaticamente(destinatario: String, asunto: String, cuerpo: String, imageFile: File?, callback: (Boolean) -> Unit) {
            val props = Properties()
            props["mail.smtp.host"] = "smtp.gmail.com"
            props["mail.smtp.port"] = "465"
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.socketFactory.port"] = "465"
            props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
            val username = "go.simple.soft@gmail.com" // "suzdalenko.suzdalenko@gmail.com"
            val password = "qqgn wbqm fkgs pyuz"      // "voel vlhf jsyg juuq"
            val session = Session.getInstance(props, object : javax.mail.Authenticator() {
                override fun getPasswordAuthentication(): javax.mail.PasswordAuthentication {
                    return javax.mail.PasswordAuthentication(username, password)
                }
            })

            try {
                val message = MimeMessage(session)
                message.setFrom(InternetAddress(username, "photo@lapse.app"))
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario))
                message.subject = asunto

                // Crear parte de texto del mensaje
                val textPart = MimeBodyPart()
                textPart.setText(cuerpo)

                // Crear parte del archivo adjunto
                val filePart = MimeBodyPart()
                val dataSource = javax.activation.FileDataSource(imageFile)
                filePart.dataHandler = DataHandler(dataSource)
                filePart.fileName = imageFile?.name

                // Crear multipart para combinar texto y archivo adjunto
                val multipart = MimeMultipart()
                multipart.addBodyPart(textPart)
                multipart.addBodyPart(filePart)

                // Establecer contenido del mensaje como multipart
                message.setContent(multipart)

                // Enviar el correo electrónico en un AsyncTask para no bloquear el hilo principal
                val executor = Executors.newSingleThreadExecutor()
                executor.execute {
                    try {
                        Transport.send(message)
                        callback(true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback(false)
                    }
                }
                executor.shutdown()

            } catch (e: MessagingException) {
                e.printStackTrace()
                callback(false)
            }
        }

        fun sendFileListing(destinatario: String, asunto: String, cuerpo: String, imageFiles: List<File>?, callback: (Boolean) -> Unit) {
            val props = Properties().apply {
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "465")
                put("mail.smtp.auth", "true")
                put("mail.smtp.socketFactory.port", "465")
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            }
            val username = "go.simple.soft@gmail.com"
            val password = "qqgn wbqm fkgs pyuz"
            val session = Session.getInstance(props, object : javax.mail.Authenticator() {
                override fun getPasswordAuthentication(): javax.mail.PasswordAuthentication {
                    return javax.mail.PasswordAuthentication(username, password)
                }
            })
            try {
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(username, "photo@lapse.app"))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario))
                    subject = asunto
                }
                // Create the text part of the message
                val textPart = MimeBodyPart().apply {
                    setText(cuerpo)
                }
                // Create a multipart message
                val multipart = MimeMultipart().apply {
                    addBodyPart(textPart)
                }
                // Add each file as a part
                imageFiles?.forEach { imageFile ->
                    val filePart = MimeBodyPart().apply {
                        val dataSource = javax.activation.FileDataSource(imageFile)
                        dataHandler = DataHandler(dataSource)
                        fileName = imageFile.name
                    }
                    multipart.addBodyPart(filePart)
                }
                // Set the content of the message to be the multipart
                message.setContent(multipart)
                // Send the email in a background thread to avoid blocking the main thread
                val executor = Executors.newSingleThreadExecutor()
                executor.execute {
                    try {
                        Transport.send(message)
                        callback(true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.d("checkAndUploadImagesA", "ERROR AL ENVIAR CORREO "+e.message.toString())
                        callback(false)
                    }
                }
                executor.shutdown()
            } catch (e: MessagingException) {
                e.printStackTrace()
                Log.d("checkAndUploadImagesA", "ERROR AL ENVIAR CORREO "+e.message.toString())
                callback(false)
            }
        }
    }
}
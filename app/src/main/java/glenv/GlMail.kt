package glenv

import glcore.GlLog
import java.util.*
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.*
import javax.mail.Authenticator
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class GlMail(
    private val sender: String,
    private val password: String,
    private val receivers: List< String >,
    private val carbonCopies: List< String >) {

    var mailProtocol: String = "SMTP"
    var mailSmtpHost: String = "smtp.qiye.163.com"
    var mailSmtpPort: Int = 25
    var mailSmtpAuth: Boolean = true
    var mailSmtpTimeout: Int = 3000
    var mailSmtpConnectionTimeout: Int = 25000

    fun mailSend(title: String, text: String, attachments: List< String >) : Boolean {
        return try {
            doMailSend(title, text, attachments)
            true
        } catch (e: Exception) {
            GlLog.i("Send Mail FAIL.")
            GlLog.e(e.stackTraceToString())
            false
        } finally {

        }
    }

    // ---------------------------------------------------------------------------------------------

    fun doMailSend(title: String, text: String, attachments: List< String >) {
        val properties: Properties = mailProperties()
        val session: Session = Session.getDefaultInstance(properties,
            object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(sender, password)
                }
            })

        val message: MimeMessage = MimeMessage(session).apply {
            setFrom(InternetAddress(sender.toString()))

            for (receiver in receivers) {
                addRecipient(Message.RecipientType.TO, InternetAddress(receiver))
            }
            for (carbonCopy in carbonCopies) {
                addRecipients(Message.RecipientType.CC, carbonCopy)
            }

            subject = title
        }

        if (attachments.isEmpty()) {
            message.setText(text)
        } else {
            val msgMultipart = MimeMultipart("mixed")

            val content: MimeBodyPart = MimeBodyPart().apply {
                setContent(text, "text/plain;charset=UTF-8")
            }
            msgMultipart.addBodyPart(content)

            for (attachment in attachments) {
                val attch: MimeBodyPart = MimeBodyPart()
                val dh: DataHandler = DataHandler(FileDataSource(attachment))
                attch.dataHandler = dh
                attch.fileName = dh.name
                msgMultipart.addBodyPart(attch)
            }

            message.setContent(msgMultipart)
        }

        message.saveChanges()
        Transport.send(message)
    }

    private fun mailProperties() : Properties =
        Properties().apply {
            this.setProperty("mail.transport.protocol", mailProtocol)
            this.setProperty("mail.smtp.host", mailSmtpHost)
            this.setProperty("mail.smtp.port", mailSmtpPort.toString())
            this.setProperty("mail.smtp.auth", if (mailSmtpAuth) "true" else "false")
            this.setProperty("mail.smtp.timeout", mailSmtpTimeout.toString())
            this.setProperty("mail.smtp.connectiontimeout", mailSmtpConnectionTimeout.toString())
        }
}

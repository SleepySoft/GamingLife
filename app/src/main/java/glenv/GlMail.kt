package glenv

import java.util.*
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.*
import javax.mail.Authenticator
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class GlMail {
    var mailProtocol: String = "SMTP"
    var mailSmtpHost: String = "smtp.qiye.163.com"
    var mailSmtpPort: Int = 25
    var mailSmtpAuth: Boolean = true
    var mailSmtpTimeout: Int = 3000

    private fun mailProperties() : Properties =
        Properties().apply {
            this.setProperty("mail.transport.protocol", mailProtocol)
            this.setProperty("mail.smtp.host", mailSmtpHost)
            this.setProperty("mail.smtp.port", mailSmtpPort.toString())
            this.setProperty("mail.smtp.auth", if (mailSmtpAuth) "true" else "false")
            this.setProperty("mail.smtp.timeout", mailSmtpTimeout.toString())
        }

    //纯文本邮件
    @Throws(Exception::class)
    fun sendText() {
        // 收件人电子邮箱
        val to: String = ""

        // 发件人电子邮箱
        val from: String = ""

        // 获取系统属性
        val properties: Properties = Properties()

        // 设置邮件服务器
        properties.setProperty("mail.transport.protocol", "SMTP")
        properties.setProperty("mail.smtp.host", "smtp.qiye.163.com")
        properties.setProperty("mail.smtp.port", "25")
        properties.setProperty("mail.smtp.auth", "true")
        properties.setProperty("mail.smtp.timeout", "3000")

        // 获取默认session对象
        val session: Session = Session.getDefaultInstance(properties,
            object : Authenticator() {
                // 登陆邮件发送服务器的用户名和密码
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication("", "")
                }
            })

        // 创建默认的 MimeMessage 对象
        val message: MimeMessage = MimeMessage(session)

        // Set From: 头部头字段
        message.setFrom(InternetAddress(from))

        // Set To: 头部头字段
        message.addRecipient(Message.RecipientType.TO, InternetAddress(to))

        // Set Subject: 头部头字段
        message.setSubject("邮件标题")

        // 设置消息体
        message.setText("邮件内容")

        // 发送消息
        Transport.send(message)
    }


    //带附件的邮件
    @Throws(MessagingException::class)
    fun sendFile(title: String?, txt: String?, path: String?, to: String?) {
        val properties: Properties = Properties()
        properties.setProperty("mail.smtp.auth", "true") // 服务器需要认证
        properties.setProperty("mail.transport.protocol", "smtp") // 声明发送邮件使用的端口
        properties.setProperty("mail.host", "smtp.qiye.163.com") // 发送邮件的服务器地址
        //可省略
        properties.setProperty("mail.smtp.port", "25")
        properties.setProperty("mail.smtp.timeout", "25000") //读超时时间
        properties.setProperty("mail.smtp.connectiontimeout", "25000") //连接超时时间
        val session: Session = Session.getInstance(properties,
            object : Authenticator() {
                // 登陆邮件发送服务器的用户名和密码
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication("", "")
                }
            })
        val msg: MimeMessage = MimeMessage(session) // 声明一个邮件体
        msg.setFrom(InternetAddress("")) //发件人
        msg.setSubject(title) //设置邮件主题
        msg.setRecipients(
            MimeMessage.RecipientType.TO,
            InternetAddress.parse(to)
        ) //收件人列表，多个收件人用逗号隔开
        msg.addRecipients(Message.RecipientType.CC, "") //抄送人

        // 设置第一个附件
        val attch1: MimeBodyPart = MimeBodyPart() // 附件1
        val dh1: DataHandler = DataHandler(FileDataSource(path)) // 附件的信息
        attch1.setDataHandler(dh1) // 指定附件
        attch1.setFileName(dh1.getName()) //附件名称

        // 设置第二个附件
        val attch2: MimeBodyPart = MimeBodyPart() // 附件2
        val dh2: DataHandler = DataHandler(FileDataSource("")) // 附件的信息
        attch2.setDataHandler(dh2) // 指定附件
        attch2.setFileName(dh2.getName()) //附件名称

        // 设置第三个附件
        val attch3: MimeBodyPart = MimeBodyPart() // 附件3
        val dh3: DataHandler = DataHandler(FileDataSource("")) // 附件的信息
        attch3.setDataHandler(dh3) // 指定附件
        attch3.setFileName(dh3.getName()) //附件名称

        //设置邮件的正文
        val content: MimeBodyPart = MimeBodyPart() // 邮件的正文，混合体（图片+文字）
        //        content.setContent(txt + "<img src=http://mimg.126.net/logo/126logo.gif>", "text/html;charset=UTF-8");//指定正文，网络图片
        content.setContent(txt, "text/plain;charset=UTF-8") //指定正文，网络图片

        // 标明邮件的组合关系，混合的关系
        val msgMultipart: MimeMultipart = MimeMultipart("mixed")
        // 将附件和正文设置到这个邮件体中
        msgMultipart.addBodyPart(content)
        msgMultipart.addBodyPart(attch1)
        //        msgMultipart.addBodyPart(attch2);
//        msgMultipart.addBodyPart(attch3);
        msg.setContent(msgMultipart) // 设置邮件体
        msg.saveChanges() //保存邮件
        Transport.send(msg) //发送邮件
    }
}
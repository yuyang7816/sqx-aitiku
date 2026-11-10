package com.sqx.common.mail.service.impl;

import com.sqx.common.core.exception.BizException;
import com.sqx.common.mail.domain.MailAccount;
import com.sqx.common.mail.enums.MailError;
import com.sqx.common.mail.service.IMailService;
import com.sqx.common.redis.utils.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

/**
 * @author chengming
 * @since 2025/6/12
 */
@Service
public class MailServiceImpl implements IMailService {
    private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);

    @Override
    public void sendSimpleMail(String to, String subject, String content, String... cc) {
        try {
            MailAccount mailAccount = createMailAccount();
            Session session = createSession(mailAccount);
            // 创建邮件
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailAccount.getFromAddress(), mailAccount.getFromName())); // 发件人地址
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to)); // 收件人地址
            message.setSubject(subject); // 邮件主题
            message.setText(content); // 邮件正文
            message.setSentDate(new Date());

            // 抄送人
            if (cc != null && cc.length > 0) {
                String ccList = String.join(",", cc);
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccList));
            }

            // 发送邮件
            Transport.send(message);
            logger.info("邮件发送成功");
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            logger.info("邮件发送失败");
            throw new BizException(MailError.SEND_FAIL);
        }
    }

    @Override
    public void sendHtmlMail(String to, String subject, String content, String... cc) {
        try {
            MailAccount mailAccount = createMailAccount();
            Session session = createSession(mailAccount);
            // 创建邮件
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailAccount.getFromAddress(), mailAccount.getFromName())); // 发件人地址
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to)); // 收件人地址
            message.setSubject(subject); // 邮件主题
            message.setContent(content, "text/html"); // 邮件正文
            message.setSentDate(new Date());

            // 抄送人
            if (cc != null && cc.length > 0) {
                String ccList = String.join(",", cc);
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccList));
            }

            // 发送邮件
            Transport.send(message);
            logger.info("邮件发送成功");
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            logger.info("邮件发送失败");
            throw new BizException(MailError.SEND_FAIL);
        }
    }

    @Override
    public void sendAttachmentsMail(String to, String subject, String content, String filePath, String... cc) {
        try {
            MailAccount mailAccount = createMailAccount();
            Session session = createSession(mailAccount);
            // 创建邮件
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailAccount.getFromAddress(), mailAccount.getFromName())); // 发件人地址
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to)); // 收件人地址
            message.setSubject(subject); // 邮件主题

            MimeMultipart multipart = new MimeMultipart();

            // 添加文本内容
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(content);
            multipart.addBodyPart(messageBodyPart);

            // 添加附件
            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(filePath);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            String filename = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
            attachmentBodyPart.setFileName(MimeUtility.encodeText(filename)); // 设置附件名称
            multipart.addBodyPart(attachmentBodyPart);

            message.setContent(multipart); // 邮件正文
            message.setSentDate(new Date());

            // 抄送人
            if (cc != null && cc.length > 0) {
                String ccList = String.join(",", cc);
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccList));
            }

            // 发送邮件
            Transport.send(message);
            logger.info("邮件发送成功");
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            logger.info("邮件发送失败");
            throw new BizException(MailError.SEND_FAIL);
        }
    }

    @Override
    public void sendResourceMail(String to, String subject, String content, String rscPath, String rscId, String... cc) {
        try {
            MailAccount mailAccount = createMailAccount();
            Session session = createSession(mailAccount);
            // 创建邮件
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailAccount.getFromAddress(), mailAccount.getFromName())); // 发件人地址
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to)); // 收件人地址
            message.setSubject(subject); // 邮件主题

            MimeMultipart multipart = new MimeMultipart("related");

            // 创建HTML正文部分
            BodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(content, "text/html");
            multipart.addBodyPart(htmlPart);

            // 添加内嵌图片
            BodyPart imgPart = new MimeBodyPart();
            FileDataSource fds = new FileDataSource(rscPath);
            imgPart.setDataHandler(new DataHandler(fds));
            imgPart.setHeader("Content-ID", String.format("<%s>", rscId)); // Content-ID需要加上尖括号
            multipart.addBodyPart(imgPart);

            message.setContent(multipart); // 邮件正文
            message.setSentDate(new Date());

            // 抄送人
            if (cc != null && cc.length > 0) {
                String ccList = String.join(",", cc);
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccList));
            }

            // 发送邮件
            Transport.send(message);
            logger.info("邮件发送成功");
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            logger.info("邮件发送失败");
            throw new BizException(MailError.SEND_FAIL);
        }
    }

    /**
     * 创建邮箱账户信息
     * @return
     */
    private MailAccount createMailAccount() {
        String host = ConfigUtil.getConfigValue("mail_host");
        String fromUser = ConfigUtil.getConfigValue("mail_send_mail");
        String port = ConfigUtil.getConfigValue("mail_port");
        String password = ConfigUtil.getConfigValue("mail_auth_code");
        String fromName = ConfigUtil.getConfigValue("sys_server_name");

        MailAccount mailAccount = new MailAccount();
        mailAccount.setHost(host);
        mailAccount.setPort(port);
        mailAccount.setUsername(fromUser);
        mailAccount.setPassword(password);
        mailAccount.setFromName(fromName);
        mailAccount.setFromAddress(fromUser);

        return mailAccount;
    }

    /**
     * 创建会话
     * @return Session
     */
    private Session createSession(MailAccount mailAccount) {
        // 设置属性
        Properties props = new Properties();
        props.put("mail.smtp.host", mailAccount.getHost()); // SMTP 服务器地址
        props.put("mail.smtp.port", mailAccount.getPort()); // SMTP 端口
        props.put("mail.smtp.auth", "true"); // 启用身份验证
        props.put("mail.smtp.ssl.enable", "true"); // 启用 TLS
        props.put("mail.smtp.ssl.trust", mailAccount.getHost()); // 信任的 SMTP 服务器

        // 创建会话
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailAccount.getUsername(), mailAccount.getPassword());
            }
        });

        logger.info("创建邮件session成功");
        return session;
    }
}

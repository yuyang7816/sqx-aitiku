package com.sqx.app.base;

import com.sqx.app.base.notification.core.model.NotificationRequest;
import com.sqx.app.base.utils.map.AddressResult;
import com.sqx.app.base.utils.map.GoogleMapUtil;
import com.sqx.app.base.notification.core.service.INotificationService;
import com.sqx.common.mail.service.IMailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author chengming
 * @since 2025/5/13
 */
@Tag(name = "开发测试")
@RestController()
@RequestMapping("app/test")
public class TestController {

    @Resource
    private IMailService mailService;
    @Resource
    private INotificationService INotificationService;

    @Operation(summary = "谷歌地图")
    @GetMapping("test")
    public void test() {
        String lat = "34.26486";
        String lng = "108.95424";
        String language = "zh-cn";

        AddressResult addressResult = GoogleMapUtil.getAddressByCoor(lat, lng, language);
        System.out.println("addressResult = " + addressResult);
    }

    @Operation(summary = "邮件")
    @GetMapping("mail")
    public void testMail() {
        String to = "chengming24@foxmail.com";
        String subject = "测试HTML邮件";
        String content = "<h1>This is actual message embedded in HTML tags</h1>";
        String filePath = "D:\\projects\\Java\\sqx\\sqx-server\\pom.xml";

        mailService.sendAttachmentsMail(to, subject, content, filePath);
    }

    @Operation(summary = "消息推送")
    @GetMapping("notify")
    public void testNotify() {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(123L);
        request.setTitle("测试");
        request.setContent("测试");
        request.setType(1);
        request.setUrl("http://www.baidu.com");
        request.setImageUrls("http://www.baidu.com");
        request.setDataId(1L);

        INotificationService.send(request);
    }
}

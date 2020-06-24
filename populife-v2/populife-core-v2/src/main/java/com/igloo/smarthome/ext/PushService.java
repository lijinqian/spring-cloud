package com.igloo.smarthome.ext;

import com.igloo.smarthome.model.User;
import java.util.HashMap;
import java.util.Map;
import javapns.communication.exceptions.CommunicationException;
import javapns.devices.Device;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.AppleNotificationServer;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.PushNotificationManager;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tcsyn.basic.util.JsonUtil;

@Component
public class PushService {
   Logger logger = Logger.getLogger(this.getClass());
   @Value("${current.env}")
   String currentEnv;
   @Value("${apple.apns.filename}")
   String apnsFileName;
   @Value("${apple.apns.password}")
   String apnsFilePassword;
   PushNotificationManager pushManager;
   AppleNotificationServer ans;
   @Autowired
   RedisTemplate<String, String> redisTemplate;

   @PostConstruct
   public void init() {
      this.pushManager = new PushNotificationManager();

      try {
         this.ans = new AppleNotificationServerBasicImpl(PushService.class.getResourceAsStream(this.apnsFileName), this.apnsFilePassword, true);
         this.pushManager.initializeConnection(this.ans);
      } catch (Exception var2) {
         var2.printStackTrace();
      }

   }

   @Async
   public void send(User user, String message, Integer event) {
      String deviceId = user.getDeviceId();
      if (StringUtils.equals(user.getOptSystem(), "IOS")) {
         this.send(user.getApnsToken(), message, event);
      } else if (StringUtils.equals(user.getOptSystem(), "Android")) {
         if (StringUtils.isNotBlank(deviceId)) {
            this.sendAndroid(event, deviceId, message);
         }
      } else if (StringUtils.isNotBlank(deviceId)) {
         if (deviceId.length() >= 32) {
            this.send(user.getApnsToken(), message, event);
         } else {
            this.sendAndroid(event, deviceId, message);
         }
      }

   }

   private void sendAndroid(Integer event, String deviceId, String message) {
      Map<String, Object> map = new HashMap();
      map.put("event", event);
      map.put("msg", message);
      this.redisTemplate.opsForList().leftPush(deviceId, JsonUtil.toJson(map));
   }

   public void send(String token, String message, Integer event) {
      if (!StringUtils.isBlank(token)) {
         PushNotificationPayload payLoad = new PushNotificationPayload(message, 1, "default");
         payLoad.addCustomDictionary("event", event);
         Device device = new BasicDevice();
         device.setToken(token);

         try {
            PushedNotification notification = this.pushManager.sendNotification(device, payLoad, true);
            this.logger.info("Push APNS result is successful ? : " + notification.isSuccessful());
         } catch (CommunicationException var8) {
            this.logger.error(var8.getMessage(), var8);
         }

      }
   }
}
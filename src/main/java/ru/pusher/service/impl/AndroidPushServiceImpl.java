package ru.pusher.service.impl;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import org.apache.log4j.Logger;
import ru.pusher.Pusher;
import ru.pusher.exceptions.PushException;
import ru.pusher.service.IPushService;

/**
 * @link https://code.google.com/p/gcm/
 */
public class AndroidPushServiceImpl implements IPushService {
  private static final Logger logger = Logger.getLogger(AndroidPushServiceImpl.class);
  private static final String MESSAGE_KEY = "text";

  @Override
  public void sendPush(String registrationId, String text) throws PushException {
    Message message = new Message.Builder()
            .timeToLive(30)
            .delayWhileIdle(true)
            .addData(MESSAGE_KEY, text)
            .build();
    try {
      Sender sender = new Sender(Pusher.getGoogleServerKey());
      Result result = sender.send(message, registrationId, Pusher.getAndroidSendPushRetryCount());

      if (result.getMessageId() == null) {
        throw new PushException("Push уведомление не отправлено. Код ошибки: " + result.getErrorCodeName());
      } else {
        if (result.getCanonicalRegistrationId() != null) {
          logger.warn("Необходимо обновить registrationId на " + result.getCanonicalRegistrationId());
        }

        logger.info("Push уведомление успешно отправлено");
      }
    } catch (PushException e) {
      throw e;
    } catch (Exception e) {
      throw new PushException(e);
    }
  }
}
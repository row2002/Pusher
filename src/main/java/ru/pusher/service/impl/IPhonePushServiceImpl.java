package ru.pusher.service.impl;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javapns.notification.ResponsePacket;
import org.apache.log4j.Logger;
import ru.pusher.Pusher;
import ru.pusher.exceptions.PushException;
import ru.pusher.service.IPushService;

import java.util.List;

/**
 * @link https://code.google.com/p/javapns/wiki/PushNotificationAdvanced
 */
public class IPhonePushServiceImpl implements IPushService {
  private static final Logger logger = Logger.getLogger(IPhonePushServiceImpl.class);
  private static final String DEFAULT_SOUND = "default";

  @Override
  public void sendPush(String pushId, String message) throws PushException {
    try {
      PushNotificationPayload payload = PushNotificationPayload.complex();
      payload.addAlert(message);
      payload.addSound(DEFAULT_SOUND);

      List<PushedNotification> notifications = Push.payload(payload, Pusher.getIphoneCertPath(), Pusher.getIphoneCertPwd(), Pusher.getIphoneCertProduction(), pushId);

      for (PushedNotification notification : notifications) {
        if (notification.isSuccessful()) {
          logger.info("Push уведомление успешно отправлено");
        } else {
          Exception theProblem = notification.getException();
          String errMsg = "Push уведомление не отправлено";

          ResponsePacket theErrorResponse = notification.getResponse();
          if (theErrorResponse != null) errMsg = theErrorResponse.getMessage();

          throw new PushException(errMsg, theProblem);
        }
      }
    } catch (KeystoreException e) {
      throw new PushException("Ошибка при попытке использования хранилища ключей", e);
    } catch (CommunicationException e) {
      throw new PushException("Ошибка во время установки соединения с серверами Apple", e);
    } catch (PushException e) {
      throw e;
    } catch (Exception e) {
      throw new PushException(e);
    }
  }
}

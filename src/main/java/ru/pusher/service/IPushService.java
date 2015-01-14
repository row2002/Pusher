package ru.pusher.service;

import ru.pusher.exceptions.PushException;

public interface IPushService {
  void sendPush(String pushId, String message) throws PushException;
}

package com.aaptrix.savitri.notification;

import com.google.firebase.messaging.FirebaseMessagingService;

public class FireBaseInstanceIDService extends FirebaseMessagingService {
	@Override
	public void onNewToken(String token) {
		super.onNewToken(token);
	}
}

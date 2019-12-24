package com.aaptrix.savitri.notification;

import android.app.Notification;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;

import com.aaptrix.savitri.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Objects;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class FireBaseMessagingService extends FirebaseMessagingService {
	
	private static int count = 0;
	
	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		super.onMessageReceived(remoteMessage);
		String title, message;
		
		if (Objects.requireNonNull(remoteMessage.getNotification()).getTitle() != null || remoteMessage.getNotification().getBody() != null) {
			title = remoteMessage.getNotification().getTitle();
			message = remoteMessage.getNotification().getBody();
		} else {
			title = remoteMessage.getData().get("title");
			message = remoteMessage.getData().get("message");
		}
		
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Notification notification = new NotificationCompat.Builder(this)
				.setContentTitle(title)
				.setContentText(message)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
				.setAutoCancel(true)
				.setLights(Color.RED, 3000, 3000)
				.setVibrate(new long[1])
				.setSound(soundUri)
				.build();
		NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
		manager.notify(count, notification);
		count++;
	}
}

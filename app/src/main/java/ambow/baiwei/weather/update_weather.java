package ambow.baiwei.weather;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class update_weather extends Service {
    public update_weather() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

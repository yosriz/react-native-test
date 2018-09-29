
package kin.devplatform.reactnative;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.Map;

import kin.devplatform.Environment;
import kin.devplatform.Kin;
import kin.devplatform.exception.BlockchainException;
import kin.devplatform.exception.ClientException;

public class RNKin extends ReactContextBaseJavaModule {

    private static final String BLOCKCHAIN_NETWORK_URL = "blockchainNetworkUrl";
    private static final String BLOCKCHAIN_PASSPHRASE = "blockchainPassphrase";
    private static final String ISSUER = "issuer";
    private static final String ECOSYSTEM_SERVER_URL = "ecosystemServerUrl";
    private static final String ECOSYSTEM_WEB_FRONT = "ecosystemWebFront";
    private static final String BI_URL = "biUrl";
    private final ReactApplicationContext reactContext;
    private final Handler handler;

    public RNKin(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public String getName() {
        return "RNKin";
    }

    @ReactMethod
    public void start(final String userId, final String jwt, final ReadableMap environment, final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Environment env = new Environment(environment.getString(BLOCKCHAIN_NETWORK_URL),
                            environment.getString(BLOCKCHAIN_PASSPHRASE), environment.getString(ISSUER),
                            environment.getString(ECOSYSTEM_SERVER_URL), environment.getString(ECOSYSTEM_WEB_FRONT),
                            environment.getString(BI_URL));
                    Kin.start(reactContext.getApplicationContext(), jwt, env);
                    promise.resolve(null);
                } catch (ClientException | BlockchainException e) {
                    e.printStackTrace();
                    promise.reject(String.valueOf(e.getCode()), e.getMessage(), e);
                }
            }
        });
    }

    @ReactMethod
    public void enableLogs(boolean enable){
        Kin.enableLogs(enable);
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("ENV_PLAYGROUND_NETWORK_URL", Environment.getPlayground().getBlockchainNetworkUrl());
        constants.put("ENV_PLAYGROUND_BI_URL", Environment.getPlayground().getBiUrl());
        constants.put("ENV_PLAYGROUND_BLOCKCHAIN_PASSPHRASE", Environment.getPlayground().getBlockchainPassphrase());
        constants.put("ENV_PLAYGROUND_ISSUER", Environment.getPlayground().getIssuer());
        constants.put("ENV_PLAYGROUND_SERVER_URL", Environment.getPlayground().getEcosystemServerUrl());
        constants.put("ENV_PLAYGROUND_WEB_FRONT", Environment.getPlayground().getEcosystemWebFront());

        return constants;
    }
}

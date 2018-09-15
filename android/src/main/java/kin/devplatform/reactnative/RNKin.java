/**
 * React Native Secure Key Store
 * Store keys securely in Android Keystore
 * Ref: cordova-plugin-secure-key-store
 */

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
    public void getPlayground(Callback callback) {
        Log.d("RNKin", "getPlayground(), Thread = " + Thread.currentThread());
        WritableMap env = Arguments.createMap();
        env.putString(BLOCKCHAIN_NETWORK_URL, Environment.getPlayground().getBlockchainNetworkUrl());
        env.putString(BLOCKCHAIN_PASSPHRASE, Environment.getPlayground().getBlockchainPassphrase());
        env.putString(ISSUER, Environment.getPlayground().getIssuer());
        env.putString(ECOSYSTEM_WEB_FRONT, Environment.getPlayground().getEcosystemWebFront());
        env.putString(ECOSYSTEM_SERVER_URL, Environment.getPlayground().getEcosystemServerUrl());
        env.putString(BI_URL, Environment.getPlayground().getBiUrl());
        callback.invoke(env);
    }

    @ReactMethod
    public void start(final String jwt, final ReadableMap environment, final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("RNKin", "start() - thread = " + Thread.currentThread());
                try {
                    Kin.enableLogs(true);
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

    @Override
    public Map<String, Object> getConstants() {
        Log.d("RNKin", "getConstants(), Thread = " + Thread.currentThread());
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

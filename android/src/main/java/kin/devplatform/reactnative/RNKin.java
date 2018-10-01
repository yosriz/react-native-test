
package kin.devplatform.reactnative;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;

import kin.devplatform.Environment;
import kin.devplatform.Kin;
import kin.devplatform.KinCallback;
import kin.devplatform.base.Observer;
import kin.devplatform.data.model.Balance;
import kin.devplatform.data.model.OrderConfirmation;
import kin.devplatform.exception.BlockchainException;
import kin.devplatform.exception.ClientException;
import kin.devplatform.exception.KinEcosystemException;
import kin.devplatform.marketplace.model.NativeSpendOffer;

public class RNKin extends ReactContextBaseJavaModule {

    private static final String BLOCKCHAIN_NETWORK_URL = "blockchainNetworkUrl";
    private static final String BLOCKCHAIN_PASSPHRASE = "blockchainPassphrase";
    private static final String ISSUER = "issuer";
    private static final String ECOSYSTEM_SERVER_URL = "ecosystemServerUrl";
    private static final String ECOSYSTEM_WEB_FRONT = "ecosystemWebFront";
    private static final String BI_URL = "biUrl";
    private static final String EVENT_BALANCE = "EVENT_BALANCE";
    private static final String EVENT_NATIVE_OFFER_CLICKED = "EVENT_NATIVE_OFFER_CLICKED";
    private final ReactApplicationContext reactContext;
    private final Handler handler;
    private Observer<Balance> balanceObserver;
    private Observer<NativeSpendOffer> nativeOfferClickedObserver;

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
                    Environment env = environmentFromJSObj(environment);
                    Kin.start(reactContext.getApplicationContext(), jwt, env);
                    promise.resolve(null);
                } catch (ClientException | BlockchainException e) {
                    rejectWithException(e, promise);
                }
            }
        });
    }

    @NonNull
    private Environment environmentFromJSObj(ReadableMap environment) {
        return new Environment(environment.getString(BLOCKCHAIN_NETWORK_URL),
                environment.getString(BLOCKCHAIN_PASSPHRASE), environment.getString(ISSUER),
                environment.getString(ECOSYSTEM_SERVER_URL), environment.getString(ECOSYSTEM_WEB_FRONT),
                environment.getString(BI_URL));
    }

    private void rejectWithException(KinEcosystemException e, Promise promise) {
        promise.reject(String.valueOf(e.getCode()), e.getMessage(), e);
    }

    @ReactMethod
    public void enableLogs(boolean enable) {
        Kin.enableLogs(enable);
    }

    @ReactMethod
    public void subscribeToBalanceObserver(final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (balanceObserver == null) {
                    balanceObserver = new Observer<Balance>() {
                        @Override
                        public void onChanged(Balance balance) {
                            getReactApplicationContext()
                                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                    .emit(EVENT_BALANCE, balance.getAmount().doubleValue());
                        }
                    };
                }
                try {
                    Kin.addBalanceObserver(balanceObserver);
                    promise.resolve(null);
                } catch (ClientException e) {
                    rejectWithException(e, promise);
                }
            }
        });
    }

    @ReactMethod
    public void unsubscribeFromBalanceObserver(final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (balanceObserver != null) {
                    try {
                        Kin.removeBalanceObserver(balanceObserver);
                        promise.resolve(true);
                    } catch (ClientException e) {
                        rejectWithException(e, promise);
                    }
                } else {
                    promise.resolve(false);
                }
            }
        });
    }

    @ReactMethod
    public void getPublicAddress(final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    promise.resolve(Kin.getPublicAddress());
                } catch (ClientException e) {
                    rejectWithException(e, promise);
                }
            }
        });
    }

    @ReactMethod
    public void getCachedBalance(final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Balance cachedBalance = Kin.getCachedBalance();
                    promise.resolve(cachedBalance.getAmount().doubleValue());
                } catch (ClientException e) {
                    rejectWithException(e, promise);
                }
            }
        });
    }

    @ReactMethod
    public void launchMarketplace(final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Activity currentActivity = getCurrentActivity();
                    if (currentActivity != null) {
                        Kin.launchMarketplace(currentActivity);
                        promise.resolve(null);
                    } else {
                        promise.reject(new IllegalStateException("Can't get current activity"));
                    }
                } catch (ClientException e) {
                    rejectWithException(e, promise);
                }
            }
        });
    }

    @ReactMethod
    public void getBalance(final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Kin.getBalance(new KinCallback<Balance>() {
                        @Override
                        public void onResponse(Balance balance) {
                            promise.resolve(balance.getAmount().doubleValue());
                        }

                        @Override
                        public void onFailure(KinEcosystemException e) {
                            rejectWithException(e, promise);
                        }
                    });
                    promise.resolve(null);
                } catch (ClientException e) {
                    rejectWithException(e, promise);
                }
            }
        });
    }

    @ReactMethod
    public void getOrderConfirmation(final String orderID, final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Kin.getOrderConfirmation(orderID, createOrderConfirmationCallback(promise));
                    promise.resolve(null);
                } catch (ClientException e) {
                    rejectWithException(e, promise);
                }
            }
        });
    }

    @NonNull
    private KinCallback<OrderConfirmation> createOrderConfirmationCallback(final Promise promise) {
        return new KinCallback<OrderConfirmation>() {

            @Override
            public void onResponse(OrderConfirmation orderConfirmation) {
                WritableMap map = new WritableNativeMap();
                map.putString("status", orderConfirmation.getStatus().getValue());
                map.putString("jwt", orderConfirmation.getJwtConfirmation());
                promise.resolve(map);
            }

            @Override
            public void onFailure(KinEcosystemException e) {
                rejectWithException(e, promise);
            }
        };
    }

    @ReactMethod
    public void requestPayment(final String jwt, final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Kin.requestPayment(jwt, createOrderConfirmationCallback(promise));
                    promise.resolve(null);
                } catch (ClientException e) {
                    rejectWithException(e, promise);
                }
            }
        });
    }

    @ReactMethod
    public void purchase(final String jwt, final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Kin.purchase(jwt, createOrderConfirmationCallback(promise));
                    promise.resolve(null);
                } catch (ClientException e) {
                    rejectWithException(e, promise);
                }
            }
        });
    }

    @ReactMethod
    public void payToUser(final String jwt, final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Kin.payToUser(jwt, createOrderConfirmationCallback(promise));
                    promise.resolve(null);
                } catch (ClientException e) {
                    rejectWithException(e, promise);
                }
            }
        });
    }

    @ReactMethod
    public void addNativeOffer(final ReadableMap nativeOffer, final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    NativeSpendOffer spendOffer = createSpendOfferFromJSObj(nativeOffer);
                    promise.resolve(Kin.addNativeOffer(spendOffer));
                } catch (ClientException e) {
                    rejectWithException(e, promise);
                }
            }
        });
    }

    @NonNull
    private NativeSpendOffer createSpendOfferFromJSObj(ReadableMap nativeOffer) {
        String id = nativeOffer.getString("id");
        String title = nativeOffer.getString("title");
        String description = nativeOffer.getString("description");
        String image = nativeOffer.getString("image");
        int amount = nativeOffer.getInt("amount");
        NativeSpendOffer spendOffer = new NativeSpendOffer(id);
        spendOffer.setTitle(title);
        spendOffer.setDescription(description);
        spendOffer.setImage(image);
        spendOffer.amount(amount);
        return spendOffer;
    }


    @ReactMethod
    public void subscribeToNativeOfferClickedObserver(final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (nativeOfferClickedObserver == null) {
                    nativeOfferClickedObserver = new Observer<NativeSpendOffer>() {
                        @Override
                        public void onChanged(NativeSpendOffer offer) {
                            WritableMap map = new WritableNativeMap();
                            map.putString("id", offer.getId());
                            map.putString("title", offer.getTitle());
                            map.putString("description", offer.getDescription());
                            map.putString("image", offer.getImage());
                            map.putInt("amount", offer.getAmount());

                            getReactApplicationContext()
                                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                    .emit(EVENT_NATIVE_OFFER_CLICKED, map);
                        }
                    };
                }
                try {
                    Kin.addNativeOfferClickedObserver(nativeOfferClickedObserver);
                    promise.resolve(null);
                } catch (ClientException e) {
                    rejectWithException(e, promise);
                }
            }
        });
    }

    @ReactMethod
    public void unsubscribeToNativeOfferClickedObserver(final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (balanceObserver != null) {
                    try {
                        Kin.removeNativeOfferClickedObserver(nativeOfferClickedObserver);
                        promise.resolve(true);
                    } catch (ClientException e) {
                        rejectWithException(e, promise);
                    }
                } else {
                    promise.resolve(false);
                }
            }
        });
    }

    @ReactMethod
    public void removeNativeOffer(final ReadableMap nativeOffer, final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    NativeSpendOffer spendOffer = createSpendOfferFromJSObj(nativeOffer);
                    promise.resolve(Kin.removeNativeOffer(spendOffer));
                } catch (ClientException e) {
                    rejectWithException(e, promise);
                }
            }
        });
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

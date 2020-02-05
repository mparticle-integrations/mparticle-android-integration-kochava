package com.mparticle.kits;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.kochava.base.AttributionUpdateListener;
import com.kochava.base.DeepLinkListener;
import com.kochava.base.Deeplink;
import com.kochava.base.DeeplinkProcessedListener;
import com.kochava.base.ReferralReceiver;
import com.kochava.base.Tracker;
import com.kochava.base.Tracker.Configuration;
import com.mparticle.AttributionError;
import com.mparticle.AttributionResult;
import com.mparticle.MParticle;
import com.mparticle.internal.Logger;
import com.mparticle.internal.MPUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class KochavaKit extends KitIntegration implements KitIntegration.AttributeListener {
    public static final String ATTRIBUTION_PARAMETERS = "attribution";
    public static final String DEEPLINK_PARAMETERS = "deeplink";
    public static final String ENHANCED_DEEPLINK_PARAMETERS = "enhancedDeeplink";

    private static final String APP_ID = "appId";
    private static final String USE_CUSTOMER_ID = "useCustomerId";
    private static final String INCLUDE_ALL_IDS = "passAllOtherIdentities";
    private static final String LIMIT_ADD_TRACKING = "limitAdTracking";
    private static final String RETRIEVE_ATT_DATA = "retrieveAttributionData";
    private static final String ENABLE_LOGGING = "enableLogging";

    private static Map<String, String> identityLink;

    @Override
    public String getName() {
        return "Kochava";
    }

    @Override
    protected List<ReportingMessage> onKitCreate(Map<String, String> settings, Context context) {
        boolean attributionEnabled = Boolean.parseBoolean(getSettings().get(RETRIEVE_ATT_DATA));

        int logLevel = Tracker.LOG_LEVEL_NONE;
        if (Boolean.parseBoolean(getSettings().get(ENABLE_LOGGING))) {
            logLevel = Tracker.LOG_LEVEL_DEBUG;
        }

        Configuration configuration = new Configuration(context.getApplicationContext())
                .setLogLevel(logLevel)
                .setAppGuid(getSettings().get(APP_ID))
                .setAppLimitAdTracking(Boolean.parseBoolean(getSettings().get(LIMIT_ADD_TRACKING)));

        if (identityLink != null) {
            configuration.setIdentityLink(new Tracker.IdentityLink().add(identityLink));
        }
        if (attributionEnabled) {
            configuration.setAttributionUpdateListener(mAttributionListener);
        }

        Tracker.configure(configuration);

        if (attributionEnabled) {
            Tracker.setDeepLinkListener(getKitManager().getLaunchUri(), mDeepLinkListener);
            Tracker.processDeeplink(getKitManager().getLaunchUri().toString(), mDeepLinkProcessedListener);
        }
        return null;
    }

    @Override
    public void setLocation(Location location) {

    }

    @Override
    public void setUserAttribute(String attributeKey, String attributeValue) {

    }

    @Override
    public void setUserAttributeList(String s, List<String> list) {
        
    }

    @Override
    public boolean supportsAttributeLists() {
        return true;
    }

    @Override
    public void setAllUserAttributes(Map<String, String> map, Map<String, List<String>> map1) {

    }

    @Override
    public void removeUserAttribute(String key) {

    }

    @Override
    public void setInstallReferrer(Intent intent) {
        new ReferralReceiver().onReceive(getContext(), intent);
    }

    @Override
    public void setUserIdentity(MParticle.IdentityType identityType, String id) {
        if (identityType == MParticle.IdentityType.CustomerId) {
            if (!getSettings().containsKey(USE_CUSTOMER_ID) ||
                    Boolean.parseBoolean(getSettings().get(USE_CUSTOMER_ID))) {
                Map<String, String> map = new HashMap<>(1);
                map.put(identityType.name(), id);
                Tracker.setIdentityLink(new Tracker.IdentityLink().add(map));
            }
        } else {
            if (Boolean.parseBoolean(getSettings().get(INCLUDE_ALL_IDS))) {
                Map<String, String> map = new HashMap<>(1);
                map.put(identityType.name(), id);
                Tracker.setIdentityLink(new Tracker.IdentityLink().add(map));
            }
        }
    }

    @Override
    public void removeUserIdentity(MParticle.IdentityType identityType) {

    }

    @Override
    public List<ReportingMessage> logout() {
        return null;
    }

    @Override
    public List<ReportingMessage> setOptOut(boolean optOutStatus) {
        Tracker.setAppLimitAdTracking(optOutStatus);
        List<ReportingMessage> messageList = new LinkedList<>();
        messageList.add(
                new ReportingMessage(this, ReportingMessage.MessageType.OPT_OUT, System.currentTimeMillis(), null)
                        .setOptOut(optOutStatus)
        );
        return messageList;
    }

    public static void setIdentityLink(Map<String, String> identityLink) {
        KochavaKit.identityLink = identityLink;
    }

    private void setAttributionResultParameter(String key, JSONObject value) {
        try {
            JSONObject parameters = new JSONObject().put(key, value);
            AttributionResult result = new AttributionResult()
                    .setServiceProviderId(getConfiguration().getKitId())
                    .setParameters(parameters);
            getKitManager().onResult(result);
        } catch (JSONException e) {
            AttributionError error = new AttributionError()
                    .setServiceProviderId(getConfiguration().getKitId())
                    .setMessage(e.getMessage());
            getKitManager().onError(error);
        }
    }

    private AttributionUpdateListener mAttributionListener = new AttributionUpdateListener() {
        @Override
        public void onAttributionUpdated(String s) {
            try {
                JSONObject attributionJson = new JSONObject(s);
                setAttributionResultParameter(ATTRIBUTION_PARAMETERS, attributionJson);
            } catch (JSONException e) {
                AttributionError error = new AttributionError()
                        .setMessage("unable to parse attribution JSON:\n " + s)
                        .setServiceProviderId(getConfiguration().getKitId());
                getKitManager().onError(error);
            }
        }
    };

    private DeepLinkListener mDeepLinkListener = new DeepLinkListener() {
        @Override
        public void onDeepLink(Map<String, String> map) {
            if (!MPUtility.isEmpty(map)) {
                setAttributionResultParameter(DEEPLINK_PARAMETERS, MPUtility.mapToJson(map));
            }
        }
    };

    private DeeplinkProcessedListener mDeepLinkProcessedListener = new DeeplinkProcessedListener() {
        @Override
        public void onDeeplinkProcessed(Deeplink deeplink) {
            if (deeplink != null) {
                setAttributionResultParameter(ENHANCED_DEEPLINK_PARAMETERS, deeplink.toJson());
            }
        }
    };
}

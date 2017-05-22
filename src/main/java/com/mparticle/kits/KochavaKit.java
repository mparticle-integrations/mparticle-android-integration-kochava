package com.mparticle.kits;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.kochava.android.tracker.Feature;
import com.kochava.android.tracker.ReferralCapture;
import com.mparticle.MParticle;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class KochavaKit extends KitIntegration implements KitIntegration.AttributeListener {

    private static final String APP_ID = "appId";
    private static final String USE_CUSTOMER_ID = "useCustomerId";
    private static final String INCLUDE_ALL_IDS = "passAllOtherIdentities";
    private static final String LIMIT_ADD_TRACKING = "limitAdTracking";
    private static final String RETRIEVE_ATT_DATA = "retrieveAttributionData";
    private static final String ENABLE_LOGGING = "enableLogging";
    private Feature feature;

    private static Map<String, String> identityLink;

    @Override
    public Object getInstance() {
        return feature;
    }

    @Override
    public String getName() {
        return "Kochava";
    }

    @Override
    protected List<ReportingMessage> onKitCreate(Map<String, String> settings, Context context) {
        createKochava();
        if (feature != null) {
            feature.setAppLimitTracking(Boolean.parseBoolean(getSettings().get(LIMIT_ADD_TRACKING)));
            Feature.enableDebug(Boolean.parseBoolean(getSettings().get(ENABLE_LOGGING)));
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
        new ReferralCapture().onReceive(getContext(), intent);
    }

    @Override
    public void setUserIdentity(MParticle.IdentityType identityType, String id) {
        if (feature != null) {
            if (identityType == MParticle.IdentityType.CustomerId) {
                if (!getSettings().containsKey(USE_CUSTOMER_ID) ||
                        Boolean.parseBoolean(getSettings().get(USE_CUSTOMER_ID))) {
                    Map<String, String> map = new HashMap<String, String>(1);
                    map.put(identityType.name(), id);
                    feature.linkIdentity(map);
                }
            } else {
                if (Boolean.parseBoolean(getSettings().get(INCLUDE_ALL_IDS))) {
                    Map<String, String> map = new HashMap<String, String>(1);
                    map.put(identityType.name(), id);
                    feature.linkIdentity(map);
                }
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

    private void createKochava() {
        if (feature == null) {
            HashMap<String, Object> datamap = new HashMap<String, Object>();
            datamap.put(Feature.INPUTITEMS.KOCHAVA_APP_ID, getSettings().get(APP_ID));
            datamap.put(Feature.INPUTITEMS.APP_LIMIT_TRACKING, getSettings().get(LIMIT_ADD_TRACKING));
            datamap.put(Feature.INPUTITEMS.DEBUG_ON, Boolean.parseBoolean(getSettings().get(ENABLE_LOGGING)));
            datamap.put(Feature.INPUTITEMS.REQUEST_ATTRIBUTION, Boolean.parseBoolean(getSettings().get(RETRIEVE_ATT_DATA)));
            if (identityLink != null) {
                datamap.put(Feature.INPUTITEMS.IDENTITY_LINK, identityLink);
            }
            feature = new Feature(getContext(), datamap);

        }
    }

    @Override
    public List<ReportingMessage> setOptOut(boolean optOutStatus) {
        if (feature != null) {
            feature.setAppLimitTracking(optOutStatus);
            List<ReportingMessage> messageList = new LinkedList<ReportingMessage>();
            messageList.add(
                    new ReportingMessage(this, ReportingMessage.MessageType.OPT_OUT, System.currentTimeMillis(), null)
                            .setOptOut(optOutStatus)
            );
            return messageList;
        }else {
            return null;
        }
    }

    public static void setIdentityLink(Map<String, String> identityLink) {
        KochavaKit.identityLink = identityLink;
    }
}

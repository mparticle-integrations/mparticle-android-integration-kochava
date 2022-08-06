package com.mparticle.kits

import android.content.Context
import com.mparticle.kits.KitIntegration.AttributeListener
import com.kochava.base.Tracker
import com.kochava.base.Tracker.IdentityLink
import android.content.Intent
import android.location.Location
import com.kochava.base.ReferralReceiver
import com.mparticle.MParticle.IdentityType
import java.util.HashMap
import org.json.JSONObject
import com.mparticle.AttributionResult
import org.json.JSONException
import com.mparticle.AttributionError
import com.kochava.base.AttributionUpdateListener
import com.kochava.base.DeepLinkListener
import com.mparticle.internal.MPUtility
import com.kochava.base.DeeplinkProcessedListener

class KochavaKit : KitIntegration(), AttributeListener {
    override fun getName(): String = NAME

    override fun onKitCreate(
        settings: Map<String, String>,
        context: Context
    ): List<ReportingMessage>? {
        val attributionEnabled = java.lang.Boolean.parseBoolean(getSettings()[RETRIEVE_ATT_DATA])
        var logLevel = Tracker.LOG_LEVEL_NONE
        if (java.lang.Boolean.parseBoolean(getSettings()[ENABLE_LOGGING])) {
            logLevel = Tracker.LOG_LEVEL_DEBUG
        }
        val configuration = getSettings()[APP_ID]?.let {
            Tracker.Configuration(context.applicationContext)
                .setLogLevel(logLevel)
                .setAppGuid(it)
                .setAppLimitAdTracking(java.lang.Boolean.parseBoolean(getSettings()[LIMIT_ADD_TRACKING]))
        }
        if (configuration != null) {
            identityLink?.let {
                configuration.setIdentityLink(IdentityLink().add(it))
            }
            if (attributionEnabled) {
                configuration.setAttributionUpdateListener(mAttributionListener)
            }
            Tracker.configure(configuration)
            if (attributionEnabled) {
                Tracker.setDeepLinkListener(kitManager.launchUri, mDeepLinkListener)
                Tracker.processDeeplink(kitManager.launchUri.toString(), mDeepLinkProcessedListener)
            }
        }
        return null
    }

    override fun setLocation(location: Location) {}
    override fun setUserAttribute(attributeKey: String, attributeValue: String) {}
    override fun setUserAttributeList(s: String, list: List<String>) {}
    override fun supportsAttributeLists(): Boolean = true
    override fun setAllUserAttributes(map: Map<String, String>, map1: Map<String, List<String>>) {}
    override fun removeUserAttribute(key: String) {}
    override fun setInstallReferrer(intent: Intent) {
        ReferralReceiver().onReceive(context, intent)
    }

    override fun setUserIdentity(identityType: IdentityType, id: String) {
        if (identityType == IdentityType.CustomerId) {
            if (!settings.containsKey(USE_CUSTOMER_ID) ||
                (settings[USE_CUSTOMER_ID].toBoolean())
            ) {
                val map = HashMap<String, String>(1)
                map[identityType.name] = id
                Tracker.setIdentityLink(IdentityLink().add(map))
            }
        } else {
            if ((settings[INCLUDE_ALL_IDS]).toBoolean()) {
                val map = HashMap<String, String>(1)
                map[identityType.name] = id
                Tracker.setIdentityLink(IdentityLink().add(map))
            }
        }
    }

    override fun removeUserIdentity(identityType: IdentityType) {}
    override fun logout(): List<ReportingMessage> = emptyList()

    override fun setOptOut(optOutStatus: Boolean): List<ReportingMessage> {
        Tracker.setAppLimitAdTracking(optOutStatus)

        return listOf(
            ReportingMessage(
                this,
                ReportingMessage.MessageType.OPT_OUT,
                System.currentTimeMillis(),
                null
            ).setOptOut(optOutStatus)
        )
    }

    private fun setAttributionResultParameter(key: String, value: JSONObject) {
        try {
            val parameters = JSONObject().put(key, value)
            val result = AttributionResult()
                .setServiceProviderId(configuration.kitId)
                .setParameters(parameters)
            kitManager.onResult(result)
        } catch (e: JSONException) {
            val error = AttributionError()
                .setServiceProviderId(configuration.kitId)
                .setMessage(e.message)
            kitManager.onError(error)
        }
    }

    private val mAttributionListener = AttributionUpdateListener { s ->
        try {
            val attributionJson = JSONObject(s)
            setAttributionResultParameter(ATTRIBUTION_PARAMETERS, attributionJson)
        } catch (e: JSONException) {
            val error = AttributionError()
                .setMessage("unable to parse attribution JSON:\n $s")
                .setServiceProviderId(configuration.kitId)
            kitManager.onError(error)
        }
    }
    private val mDeepLinkListener = DeepLinkListener { map ->
        if (!MPUtility.isEmpty(map)) {
            setAttributionResultParameter(DEEPLINK_PARAMETERS, MPUtility.mapToJson(map))
        }
    }
    private val mDeepLinkProcessedListener = DeeplinkProcessedListener { deeplink ->
        setAttributionResultParameter(ENHANCED_DEEPLINK_PARAMETERS, deeplink.toJson())
    }

    companion object {
        const val ATTRIBUTION_PARAMETERS = "attribution"
        const val DEEPLINK_PARAMETERS = "deeplink"
        const val ENHANCED_DEEPLINK_PARAMETERS = "enhancedDeeplink"
        private const val APP_ID = "appId"
        private const val USE_CUSTOMER_ID = "useCustomerId"
        private const val INCLUDE_ALL_IDS = "passAllOtherIdentities"
        private const val LIMIT_ADD_TRACKING = "limitAdTracking"
        private const val RETRIEVE_ATT_DATA = "retrieveAttributionData"
        private const val ENABLE_LOGGING = "enableLogging"
        const val NAME = "Kochava"
        private var identityLink: Map<String, String>? = null
        fun setIdentityLink(identityLink: Map<String, String>?) {
            Companion.identityLink = identityLink
        }
    }
}

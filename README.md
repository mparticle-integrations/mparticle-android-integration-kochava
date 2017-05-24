## Kochava Kit Integration

This repository contains the [Kochava](https://www.kochava.com) integration for the [mParticle Android SDK](https://github.com/mParticle/mparticle-android-sdk).

### Adding the integration

1. Add the kit dependency to your app's build.gradle:

    ```groovy
    dependencies {
        compile 'com.mparticle:android-kochava-kit:4+'
    }
    ```
2. Follow the mParticle Android SDK [quick-start](https://github.com/mParticle/mparticle-android-sdk), then rebuild and launch your app, and verify that you see `"Kochava detected"` in the output of `adb logcat`.
3. Reference mParticle's integration docs below to enable the integration.

### Documentation

[Kochava integration](http://docs.mparticle.com/?java#kochava)

###IdentityLink

If you would like to associate the Account Identity with a custom Identifier that is not the Device Identity, you can include the data in the Kochava kit's initialization, by calling `KochavaKit.setIdentityLink` before `MParticle.start()`

```
Map<String, String> identityLink = new HashMap<String, String>();
identityLink.put("key1", "identity1");
identityLink.put("key2", "identity2");
KochavaKit.setIdentityLink(identityLink);
```


### License

[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)
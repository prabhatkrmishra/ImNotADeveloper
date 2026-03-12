#include <cstdlib>
#include "LSPosed.h"
#include <sys/system_properties.h>
#include "Logger.h"
#include <jni.h>
#include <dlfcn.h>

static HookFunType hook_func = nullptr;

extern "C" JNIEXPORT void JNICALL
Java_io_github_auag0_imnotadeveloper_xposed_NativeFun_initHooks(JNIEnv *env, jobject thiz) {
    LOGD("initHooks called from Kotlin");
}

#include <map>
#include <jni.h>

static std::map<std::string, std::string> propOverrides;

extern "C"
JNIEXPORT void JNICALL
Java_io_github_auag0_imnotadeveloper_xposed_NativeFun_setProps(JNIEnv *env, jobject thiz,
                                                               jobject props) {
    jclass mapClass = env->FindClass("java/util/Map");
    jmethodID entrySetMethod = env->GetMethodID(mapClass, "entrySet", "()Ljava/util/Set;");
    jobject entrySet = env->CallObjectMethod(props, entrySetMethod);

    jclass setClass = env->FindClass("java/util/Set");
    jmethodID iteratorMethod = env->GetMethodID(setClass, "iterator", "()Ljava/util/Iterator;");
    jobject iterator = env->CallObjectMethod(entrySet, iteratorMethod);

    jclass iteratorClass = env->FindClass("java/util/Iterator");
    jmethodID hasNextMethod = env->GetMethodID(iteratorClass, "hasNext", "()Z");
    jmethodID nextMethod = env->GetMethodID(iteratorClass, "next", "()Ljava/lang/Object;");

    jclass entryClass = env->FindClass("java/util/Map$Entry");
    jmethodID getKeyMethod = env->GetMethodID(entryClass, "getKey", "()Ljava/lang/Object;");
    jmethodID getValueMethod = env->GetMethodID(entryClass, "getValue", "()Ljava/lang/Object;");

    while (env->CallBooleanMethod(iterator, hasNextMethod)) {
        jobject entry = env->CallObjectMethod(iterator, nextMethod);
        jobject keyObj = env->CallObjectMethod(entry, getKeyMethod);
        jstring keyString = (jstring) keyObj;
        jobject valueObj = env->CallObjectMethod(entry, getValueMethod);
        jstring valueString = (jstring) valueObj;
        const char *key = env->GetStringUTFChars(keyString, nullptr);
        const char *value = env->GetStringUTFChars(valueString, nullptr);

        propOverrides[std::string(key)] = std::string(value);
        propOverrides["sys.usb.config"] = "mtp";
        propOverrides["sys.usb.state"] = "mtp";
        propOverrides["init.svc.adbd"] = "stopped";
        propOverrides["ro.debuggable"] = "0";
        propOverrides["ro.build.tags"] = "release-keys";
        propOverrides["ro.build.type"] = "user";

        env->ReleaseStringUTFChars(keyString, key);
        env->ReleaseStringUTFChars(valueString, value);
        env->DeleteLocalRef(entry);
        env->DeleteLocalRef(keyObj);
        env->DeleteLocalRef(valueObj);
    }

    env->DeleteLocalRef(mapClass);
    env->DeleteLocalRef(entrySet);
    env->DeleteLocalRef(setClass);
    env->DeleteLocalRef(iterator);
    env->DeleteLocalRef(iteratorClass);
    env->DeleteLocalRef(entryClass);
}

static int (*orig_system_property_get)(const char *name, char *value);

int hooked_system_property_get(const char *name, char *value) {
    auto it = propOverrides.find(name);
    if (it != propOverrides.end()) {
        const std::string &fakeValue = it->second;
        LOGD("Spoof property: %s -> %s", name, fakeValue.c_str());
        strncpy(value, fakeValue.c_str(), PROP_VALUE_MAX - 1);
        value[PROP_VALUE_MAX - 1] = '\0';
        return strlen(value);
    }

    return orig_system_property_get(name, value);
}

extern "C"
[[gnu::visibility("default")]]
[[gnu::used]]
NativeOnModuleLoaded native_init(const NativeAPIEntries *entries) {
    LOGD("native_init");

    hook_func = entries->hook_func;

    if (!hook_func) {
        LOGE("Hook function not initialized");
        return nullptr;
    }

    void *system_property_get_addr = dlsym(RTLD_DEFAULT, "__system_property_get");
    LOGD("__system_property_get addr = 0x%x", (uintptr_t) system_property_get_addr);

    hook_func(system_property_get_addr, (void *) hooked_system_property_get,
              (void **) &orig_system_property_get);

    return nullptr;
}
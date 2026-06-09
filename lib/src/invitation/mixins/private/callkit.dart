part of 'package:zego_uikit_prebuilt_call/src/invitation/service.dart';

/// @nodoc
mixin ZegoCallInvitationServiceCallKitPrivate {
  final _callkitImpl = ZegoCallInvitationServiceCallKitPrivateImpl();

  /// Don't call that
  ZegoCallInvitationServiceCallKitPrivateImpl get callkit => _callkitImpl;
}

/// @nodoc
/// Here are the APIs related to invitation.
class ZegoCallInvitationServiceCallKitPrivateImpl {
  bool _callKitServiceInit = false;

  ZegoCallInvitationPageManager? _myPageManager;

  Future<void> _initCallKit({
    required ZegoCallInvitationPageManager pageManager,
    ZegoCallAndroidNotificationConfig? androidNotificationConfig,
  }) async {
    if (_callKitServiceInit) {
      ZegoLoggerService.logInfo(
        'callkit service had been init',
        tag: 'call-invitation',
        subTag: 'callkit',
      );

      return;
    }

    ZegoLoggerService.logInfo(
      'callkit service init',
      tag: 'call-invitation',
      subTag: 'callkit',
    );

    _callKitServiceInit = true;

    ZegoCallKitBackgroundService.instance.register(
      pageManager: pageManager,
    );

    final callKitCallID = await ZegoUIKitCallCache().offlineCallKit.getCallID();
    ZegoLoggerService.logInfo(
      'offline callkit call id: $callKitCallID',
      tag: 'call-invitation',
      subTag: 'callkit',
    );

    /// Close the offline incoming call pop-up window
    ZegoCallPluginPlatform.instance.dismissAllNotifications();

    _myPageManager = pageManager;

    _setCallKitVariables({
      CallKitInnerVariable.callIDVisibility:
          androidNotificationConfig?.callIDVisibility ?? true,
      CallKitInnerVariable.showFullLockedScreen:
          androidNotificationConfig?.showOnLockedScreen ?? false,
      CallKitInnerVariable.ringtonePath:
          androidNotificationConfig?.callChannel.sound,
      CallKitInnerVariable.backgroundUrl:
          androidNotificationConfig?.fullScreenBackgroundAssetURL ?? ''
    });

    ZegoLoggerService.logInfo(
      'request permission',
      tag: 'call-invitation',
      subTag: 'callkit',
    );

    ZegoLoggerService.logInfo(
      'register callkit incoming event listener',
      tag: 'call-invitation',
      subTag: 'callkit',
    );
    if (Platform.isIOS) {
      FlutterCallkitIncoming.onEvent.listen(_onIOSCallKitIncomingEvent);
    } else if (Platform.isAndroid) {
      ZegoCallPluginPlatform.instance.setPersistentCallNotificationCallbacks(
        onAccepted: () async {
          ZegoLoggerService.logInfo(
            'persistent acceptCallback',
            tag: 'call-invitation',
            subTag: 'callkit',
          );
          ZegoCallInvitationNotificationManager.hasInvitation = false;
          await ZegoUIKit().activeAppToForeground();
          await ZegoUIKit().requestDismissKeyguard();
          ZegoCallKitBackgroundService().acceptInvitationInBackground();
        },
        onRejected: () async {
          ZegoLoggerService.logInfo(
            'persistent rejectCallback',
            tag: 'call-invitation',
            subTag: 'callkit',
          );
          ZegoCallInvitationNotificationManager.hasInvitation = false;
          ZegoCallKitBackgroundService().refuseInvitationInBackground();
        },
        onCancelled: () async {
          ZegoLoggerService.logInfo(
            'persistent cancelCallback',
            tag: 'call-invitation',
            subTag: 'callkit',
          );
          ZegoCallInvitationNotificationManager.hasInvitation = false;
        },
      );
    }
  }

  Future<void> _uninitCallKit() async {
    if (!_callKitServiceInit) {
      ZegoLoggerService.logInfo(
        'callkit service had not been init',
        tag: 'call-invitation',
        subTag: 'callkit',
      );

      return;
    }

    ZegoLoggerService.logInfo(
      'callkit service uninit',
      tag: 'call-invitation',
      subTag: 'callkit',
    );

    _callKitServiceInit = false;

    FlutterCallkitIncoming.onEvent.listen(null);

    ZegoUIKitCallCache().offlineCallKit.clearCallID();
    ZegoUIKitCallCache().offlineCallKit.clearCacheParams();
  }

  void _setCallKitVariables(Map<CallKitInnerVariable, dynamic> variables) {
    ZegoLoggerService.logInfo(
      'set callkit variables:$variables',
      tag: 'call-invitation',
      subTag: 'callkit',
    );

    SharedPreferences.getInstance().then((prefs) {
      variables.forEach((key, value) {
        switch (key) {
          case CallKitInnerVariable.callIDVisibility:
          case CallKitInnerVariable.showFullLockedScreen:
            prefs.setBool(key.cacheKey, value as bool? ?? key.defaultValue);
            break;
          case CallKitInnerVariable.textAccept:
          case CallKitInnerVariable.textDecline:
          case CallKitInnerVariable.textMissedCall:
          case CallKitInnerVariable.textCallback:
          case CallKitInnerVariable.backgroundColor:
          case CallKitInnerVariable.backgroundUrl:
          case CallKitInnerVariable.actionColor:
          case CallKitInnerVariable.textAppName:
          case CallKitInnerVariable.ringtonePath:
            prefs.setString(key.cacheKey, value as String? ?? key.defaultValue);
            break;
        }
      });
    });
  }

  /// for popup top notify window if app in background — iOS only (v3.x API)
  Future<void> _onIOSCallKitIncomingEvent(CallEvent? event) async {
    if (!Platform.isIOS || event == null) return;

    ZegoLoggerService.logInfo(
      'iOS callkit incoming event: ${event.eventName}',
      tag: 'call-invitation',
      subTag: 'callkit',
    );

    if (event is CallEventActionCallAccept) {
      ZegoUIKitCallCache().offlineCallKit.getCallID().then((callKitCallID) async {
        await ZegoUIKit().setAdvanceConfigs({'support_apple_callkit': 'true'});
        await ZegoCallKitBackgroundService()
            .acceptCallKitIncomingCauseInBackground(callKitCallID);
      });
    } else if (event is CallEventActionCallDecline) {
      await ZegoCallKitBackgroundService().refuseInvitationInBackground();
    } else if (event is CallEventActionCallEnded) {
      await ZegoUIKit().setAdvanceConfigs({'support_apple_callkit': 'false'});
      if (ZegoUIKitPrebuiltCallInvitationService().isInCall) {
        await ZegoCallKitBackgroundService().handUpCurrentCallByCallKit();
      } else {
        await ZegoCallKitBackgroundService().refuseInvitationInBackground();
      }
      _myPageManager?.hasCallkitIncomingCauseAppInBackground = false;
    } else if (event is CallEventActionCallToggleMute) {
      ZegoUIKit().turnMicrophoneOn(!event.isMuted);
    }

    /// update ios callkit pop-up display state
    if (event is CallEventActionCallIncoming) {
      ZegoCallKitBackgroundService().setIOSCallKitCallingDisplayState(true);
    } else if (event is CallEventActionCallDecline ||
        event is CallEventActionCallTimeout ||
        event is CallEventActionCallEnded ||
        event is CallEventActionCallAccept) {
      ZegoCallKitBackgroundService().setIOSCallKitCallingDisplayState(false);
    }
  }
}

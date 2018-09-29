//
//  RNKin.m
//  RNKinDevPlatform
//
//  Created by Yossi Rizgan on 15/09/2018.
//  Copyright © 2018 Facebook. All rights reserved.
//

#import "React/RCTBridgeModule.h"

@interface RCT_EXTERN_MODULE(RNKin, NSObject)

    RCT_EXTERN_METHOD(start:(NSString)userID
                      jwt:(NSString)jwt
                      environment:(NSDictionary)environment
                      resolver:(RCTPromiseResolveBlock)resolver
                      rejecter:(RCTPromiseRejectBlock)rejecter)

    RCT_EXTERN_METHOD(enableLogs:(BOOL)enable)
@end

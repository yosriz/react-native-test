//
//  RNKin.swift
//  RNKinDevPlatform
//
//  Created by Yossi Rizgan on 15/09/2018.
//  Copyright © 2018 Facebook. All rights reserved.
//

import Foundation
import KinDevPlatform

@objc(RNKin)
class RNKin: NSObject {
    
    @objc
    func constantsToExport() -> [AnyHashable : Any]! {
        return [
            "ENV_PLAYGROUND_NETWORK_URL": Environment.playground.blockchainURL,
            "ENV_PLAYGROUND_BI_URL": Environment.playground.BIURL,
            "ENV_PLAYGROUND_BLOCKCHAIN_PASSPHRASE": Environment.playground.blockchainPassphrase,
            "ENV_PLAYGROUND_ISSUER": Environment.playground.kinIssuer,
            "ENV_PLAYGROUND_SERVER_URL": Environment.playground.marketplaceURL,
            "ENV_PLAYGROUND_WEB_FRONT": Environment.playground.webURL
        ]
    }
    
    @objc(start:jwt:environment:resolver:rejecter:)
    func start(userId : NSString, jwt : NSString, environment : NSDictionary, resolver : RCTPromiseResolveBlock, rejecter : RCTPromiseRejectBlock) {
        let envProp = EnvironmentProperties(
            blockchainURL: (environment["blockchainNetworkUrl"] as? String)!,
            blockchainPassphrase: (environment["blockchainPassphrase"] as? String)!,
            kinIssuer: (environment["issuer"] as? String)!,
            marketplaceURL: (environment["ecosystemServerUrl"] as? String)!,
            webURL: (environment["ecosystemWebFront"] as? String)!,
            BIURL: (environment["biUrl"] as? String)!)
        do {
            try Kin.shared.start(userId: userId as String, jwt: jwt as String, environment: .custom(envProp))
            resolver(nil)
        }
        catch {
            rejecter("", error.localizedDescription, error)
        }
    }
    
    @objc(enableLogs:)
    func enableLogs(enable : ObjCBool){
        if (enable).boolValue{
            Kin.shared.setLogLevel(.verbose)
        }
        else{
            Kin.shared.setLogLevel(.mute)
        }
    }
}

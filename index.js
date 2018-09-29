import { NativeModules } from 'react-native';
// module.exports = NativeModules.RNKin;

const { RNKin } = NativeModules;

export class Environment {
    constructor(blockchainNetworkUrl: string,
        blockchainPassphrase: string,
        issuer: string,
        ecosystemServerUrl: string,
        ecosystemWebFront: string,
        biUrl: string) {
        console.log("Environment ctor");
        this.blockchainNetworkUrl = blockchainNetworkUrl;
        this.blockchainPassphrase = blockchainPassphrase;
        this.issuer = issuer;
        this.ecosystemServerUrl = ecosystemServerUrl;
        this.ecosystemWebFront = ecosystemWebFront;
        this.biUrl = biUrl;
    }

    static playground() {
        return new Environment(RNKin.ENV_PLAYGROUND_NETWORK_URL,
            RNKin.ENV_PLAYGROUND_BLOCKCHAIN_PASSPHRASE,
            RNKin.ENV_PLAYGROUND_ISSUER,
            RNKin.ENV_PLAYGROUND_SERVER_URL,
            RNKin.ENV_PLAYGROUND_WEB_FRONT,
            RNKin.ENV_PLAYGROUND_BI_URL
        );
    }
}

export class Kin {

    static start = async (userID: string, jwt: string, env: Environment) => {
        return await RNKin.start(userID, jwt, env);
    }

    static enableLogs(enable : boolean){
        RNKin.enableLogs(enable);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

import com.pubnub.api.PNConfiguration;

/**
 *
 * @author General Steinacoz
 */
public class PubnubConfig {
    //create an instance of PNConfiguration
    PNConfiguration pnConfiguration;
    
    public PNConfiguration pConfig(){
        
        //set the subscribe and publish key
        pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("sub-c-a8169944-3833-11e7-9361-0619f8945a4f");
        pnConfiguration.setPublishKey("pub-c-6e287234-f550-4a51-a25a-5fe510f91dd6");
        
        return pnConfiguration;
    }
    
}

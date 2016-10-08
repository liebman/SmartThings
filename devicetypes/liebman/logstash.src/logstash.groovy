/**
 *  LogStash
 *
 *  Copyright 2016 Chris Liebman
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "LogStash", namespace: "liebman", author: "Chris Liebman") {
		capability "Switch"

		command "log", ["string", "string"]
	}

    input("logstashIP", "string", title:"LogStash IP Address", description:"Please enter your LogStash I.P. address", defaultValue:"192.168.0.42" , required: true, displayDuringSetup: true)
    input("logstashPort", "string", title:"LogStash Port", description:"Port LogStash listens on", defaultValue:"14242", required: true, displayDuringSetup: true)
 
	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
//		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
//			state "off", label: 'Push', action: "momentary.push", backgroundColor: "#ffffff", nextState: "on"
//			state "on", label: 'Push', action: "momentary.push", backgroundColor: "#53a7c0"
//		}
//		main "switch"
//		details "switch"
		valueTile("address", "device.address", decoration: "flat") {
        	state "address", label:'${currentValue}'
    	}
    }
}

// parse events into attributes
def parse(String description) {
    //log.debug "Parsing '${description}'"
    def map = parseLanMessage(description);
    //log.debug "As LAN: " + map;
    if (!map.body?.equals('ok')) {
        log.error "bad response: " + map;
    }
    // TODO: handle 'switch' attribute
}

// handle commands
def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def log(level, message) {
    //log.debug "Executing 'log' level:${level} message:${message}"

    def hosthex = convertIPtoHex(logstashIP)
    def porthex = convertPortToHex(logstashPort)
    device.deviceNetworkId = "$hosthex:$porthex"
    
    def dateNow = new Date()
    def isoDateNow = dateNow.format("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    def json = new groovy.json.JsonBuilder()
    json.call("isoDate":isoDateNow,
              "level":level,
              "message":message)
    
    def logstash = "${logstashIP}:${logstashPort}"
        
    def headers = [:] 
    headers.put("HOST", logstash)
    headers.put("Content-Type", "application/json")
    headers.put("Connection", "close")
    
    try {
        //log.debug "creating hubAction with logstash address: ${logstash} (${isoDateNow})"
        def hubAction = new physicalgraph.device.HubAction(method:"POST", path:"/", body:json.content, headers:headers)
        //log.debug "hubAction:" + hubAction
        return hubAction
    } catch (Exception e) {
        log.error "Hit Exception ${e} on ${hubAction}"
    }
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
//    log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
    String hexport = port.toString().format( '%04X', port.toInteger() )
//    log.debug hexport
    return hexport
}
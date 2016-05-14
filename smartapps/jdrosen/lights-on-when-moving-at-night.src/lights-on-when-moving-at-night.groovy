/**
 *  Lights On When Moving at Night
 *
 *  Copyright 2016 Jonathan Rosenberg
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
definition(
    name: "Lights On When Moving at Night",
    namespace: "jdrosen",
    author: "Jonathan Rosenberg",
    description: "Turn lights on when motion is detected and its after dark",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Choose your Motion Detector") {
		input "mymotion", "capability.motionSensor", required:true, title: "where?"
	}
    
    section("Choose your Switch") {
    	input "myswitch", "capability.switch", required:true
    }	
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(mymotion, "motion.active", motionDetectedHandler)
    subscribe(mymotion, "motion.inactive", motionInactiveHandler)
    
}


def motionDetectedHandler(evt) {
	log.debug "Motion Detected. The event was ${evt}"
    def rightNow = new Date()
    log.debug "Current date is $rightNow"
    log.debug "Sunrise is ${getSunriseAndSunset(zipCode: "07728").sunrise}"
    log.debug "Sunset is ${getSunriseAndSunset(zipCode: "07728").sunset}"
    
    if(timeOfDayIsBetween(getSunriseAndSunset(zipCode: "07728").sunrise, 
    					getSunriseAndSunset(zipCode: "07728").sunset, 
                        rightNow, 
                        location.timeZone)) {
    	log.debug("Current time is between sunrise and sunset, so not turning on the light.")
    } else {
    	log.debug("Current time is not between sunrise and sunset, so its dark out. light on!")
        myswitch.on()
    }
    
    
}

def motionInactiveHandler(evt) {
	log.debug "Inactive Detected. the event was ${evt}"
    runIn(3600, checkMotion)
    
}

def checkMotion() {
	log.debug "In checkMotion scheduled method"

    // get the current state object for the motion sensor
    def motionState = mymotion.currentState("motion")

    if (motionState.value == "inactive") {
            // get the time elapsed between now and when the motion reported inactive woot
        def elapsed = now() - motionState.date.time

        // elapsed time is in milliseconds, so the threshold must be converted to milliseconds too
        def threshold = 1000 * 60 * 60

            if (elapsed >= threshold) {
            log.debug "Motion has stayed inactive long enough since last check ($elapsed ms):  turning switch off"
            myswitch.off()
            } else {
            log.debug "Motion has not stayed inactive long enough since last check ($elapsed ms):  doing nothing"
        }
    } else {
            // Motion active; just log it and do nothing
            log.debug "Motion is active, do nothing and wait for inactive"
    }
}
 
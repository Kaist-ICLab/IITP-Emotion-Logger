 # IITP Emotion Logger 
IITP Emotion Logger is a physiological data logger for in-the-wild data collection. The app is developed based on GalaxyPPG Logger.

 # Overview
 * logger-structure: code module that includes common structure for the wearable app and the mobile app
 * smartphone: Smartphone app responsible for collecting step data and environment data. Receives data from the wearable and sends to server
 * wearable: Wearable (Galaxy Watch) app responsible for collecting physiological data. Sends the data periodically to smartphone app.

 # Requirements
 * put Samsung Health Data SDK under `smartphone/libs`
 * put Samsung Health Sensor SDK under `wearable/libs`

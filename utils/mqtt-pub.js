var mqtt    = require('mqtt');
var client  = mqtt.connect('mqtt://localhost:8884');
 
client.on('connect', function () {
  client.publish('/mqtt/test', 'Hello!', {retain: false, qa: 1});
client.end();
});

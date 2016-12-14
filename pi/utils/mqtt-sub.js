var mqtt    = require('mqtt');
var client  = mqtt.connect('mqtt://localhost:8884');
 
client.on('connect', function () {
  client.subscribe('presence');
 
client.on('message', function (topic, message) {
  console.log(message.toString());
client.end();
  });
});

var mqtt = require('mqtt');

var client = mqtt.connect('mqtt://localhost:8884');

client.on('connect', function() { // connected

  client.subscribe('#', function() {
    // message arrived
    client.on('message', function(topic, message, packet) {
      console.log("Received '" + message + "' on '" + topic + "'");
      var t = topic.split('/');
      console.log(t[2]);
    });
  });

  client.publish('/mqtt/test/listener', 'I am up ..', function() {
    console.log("Message is published");
  });
});

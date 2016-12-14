/**
 * This application controls movement of l298n based two wheel robot over the internet.
 * We are using MQTT framework for controlling.
 *
 * RPi Model A 1 (2011 model with total of 26 expension pins on board) was ued for this project.
 * For any change in RPi board variant, below pin numbers needs to be updated.
 *
 */

var speed = 100
var in1Pin = 21
var in2Pin = 22
var enable1Pin = 17
var in3Pin = 9
var in4Pin = 10
var enable2Pin = 11
var direction = 0

function changeDirection () {
  direction = 1 - direction
  if (direction == 1) {
    console.log('>>>  FORWARD')
    l298n.forward(l298nModule.LEFT)
    l298n.forward(l298nModule.RIGHT)
  } else {
    console.log('>>>  BACKWARD')
    l298n.backward(l298nModule.LEFT)
    l298n.backward(l298nModule.RIGHT)
  }
}

function moveRight () {
  console.log('>>>  RIGHT')
  l298n.stop(l298nModule.LEFT)
}

function moveLeft () {
  console.log('>>>  LEFT')
  l298n.stop(l298nModule.RIGHT)
}

function moveForward () {
  console.log('>>>  FORWARD')
  l298n.forward(l298nModule.LEFT)
  l298n.forward(l298nModule.RIGHT)
}

function moveBackward () {
  console.log('>>>  BACKWARD')
  l298n.backward(l298nModule.LEFT)
  l298n.backward(l298nModule.RIGHT)
}

function moveStop () {
  console.log('>>>  STOP')
  l298n.stop(l298nModule.LEFT)
  l298n.stop(l298nModule.RIGHT)
}

var l298nModule = require('./l298n-controller')
var l298n = l298nModule.setup(in1Pin, in2Pin, enable1Pin, in3Pin, in4Pin, enable2Pin)

l298n.setSpeed(speed)

// MQTT ----------------

var mqtt = require('mqtt');

var client = mqtt.connect('mqtt://192.168.0.101:1883');

client.on('connect', function() {
  
  client.publish('from/l298n/rovar/state', 'listening', function() {
    console.log("server listening at to/l298n/rovar/<cmd>");
    console.log("cmd = forward|backward|stop|right|left");
  });

  client.subscribe('to/l298n/rovar/#', function() {
    client.on('message', function(topic, message, packet) {
      console.log("Received '" + message + "' on '" + topic + "'");
      var t = topic.split('/');
      if(t.length >= 4) {
        console.log("Command = " + t[3]);
  	switch (t[3]) {
    	case 'forward':
      	    return moveForward()
    	case 'backward':
            return moveBackward()
    	case 'stop':
            return moveStop()
   	case 'right':
            return moveRight()
    	case 'left':
            return moveLeft()
        default:
            return console.log("Invalied command topic: " + topic);
  }
      } else {
        console.log("ERROR: Invalied command!!");
      }
    });
  });
});


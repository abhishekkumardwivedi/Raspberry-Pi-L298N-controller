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

var l298nModule = require('./src/l298n-controller')
var l298n = l298nModule.setup(in1Pin, in2Pin, enable1Pin, in3Pin, in4Pin, enable2Pin)

l298n.setSpeed(speed)

// MQTT ----------------

const mqtt = require('mqtt')
const client = mqtt.connect('mqtt://192.168.0.100')

client.on('connect', function () {
  client.subscribe('to/l298n/rovar/#')

  client.publish('status/l298n/rovar/connection', 'true')
  sendStateUpdate()
})

client.on('message', function (topic, message) {
  console.log('received message %s %s', topic, message)
  switch (topic) {
    case 'to/l298n/rovar/forward':
      return moveForward()
    case 'to/l298n/rovar/backward':
      return moveBackward()
    case 'to/l298n/rovar/stop':
      return moveStop()
    case 'to/l298n/rovar/right':
      return moveRight()
    case 'to/l298n/rovar/left':
      return moveLeft()
  }
})

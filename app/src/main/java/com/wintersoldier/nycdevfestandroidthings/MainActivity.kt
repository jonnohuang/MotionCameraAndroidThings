package com.wintersoldier.nycdevfestandroidthings

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
class MainActivity : Activity() {
    private lateinit var buttonGpio: Gpio
    private lateinit var motionSensorGpio: Gpio
    private lateinit var ledGpio: Gpio
    private lateinit var camera: CustomCamera

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // instantiate the button - first part of the demo
//        buttonGpio = PeripheralManagerService().openGpio("GPIO6_IO15")
//        buttonGpio.setDirection(Gpio.DIRECTION_IN)
//        buttonGpio.setActiveType(Gpio.ACTIVE_LOW)
//        buttonGpio.setEdgeTriggerType(Gpio.EDGE_BOTH)
//
//        // callback
//        buttonGpio.registerGpioCallback(object: GpioCallback() {
//            override fun onGpioEdge(gpio: Gpio): Boolean {
//                if (gpio.value) {
//                    Log.d( "MainActivity",  "button pressed")
//                } else {
//                    Log.d( "MainActivity",  "button lifted")
//                }
//
//                // turn on led
//                ledGpio.value = gpio.value
//
//                // return
//                return super.onGpioEdge(gpio)
//            }
//        })
//

        // part 2
        // instantiate the motion sensor
        motionSensorGpio = PeripheralManagerService().openGpio("GPIO2_IO03")
        motionSensorGpio.setDirection(Gpio.DIRECTION_IN)
        motionSensorGpio.setActiveType(Gpio.ACTIVE_LOW)
        motionSensorGpio.setEdgeTriggerType(Gpio.EDGE_BOTH)

        // callback
        motionSensorGpio.registerGpioCallback(object: GpioCallback() {
            override fun onGpioEdge(gpio: Gpio): Boolean {
                if (gpio.value) {
                    camera.takePicture()
                    Log.d( "MainActivity",  "motion sensor tripped")
                } else {
                    Log.d( "MainActivity",  "motion sensor back on")
                }

                // turn on led
                ledGpio.value = gpio.value

                // return
                return super.onGpioEdge(gpio)
            }
        })

        // instance the led
        ledGpio = PeripheralManagerService().openGpio("GPIO6_IO14")
        ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

        // setup the camera
        this.setupCamera()

    }

    private fun setupCamera() {
        camera = CustomCamera.getInstance()
        camera.initializeCamera(this, Handler(), imageAvailableListener)
    }

    private val imageAvailableListener = object: CustomCamera.ImageCapturedListener {
        override fun onImageCaptured(bitmap: Bitmap) {
            motionImageView.setImageBitmap(bitmap)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        buttonGpio.close()
        motionSensorGpio.close()
        camera.close()
    }
}

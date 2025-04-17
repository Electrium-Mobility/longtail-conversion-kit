#include <stdio.h>
#include "Arduino.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "driver/gpio.h"
#include "driver/adc.h"
#include "esp_log.h"
#include "esp_wifi.h"
#include "esp_now.h"
#include "esp_err.h"
#include "nvs_flash.h"

#include "display.h"
#include "ble.h"
#include "telemetry.h"

#include "init.h"

void initialize() {
    initArduino();
    init_display();
    initBLE();
    initUart();
    vTaskDelete(NULL);
}
